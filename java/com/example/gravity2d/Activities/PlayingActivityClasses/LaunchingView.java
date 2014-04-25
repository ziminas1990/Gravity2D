package com.example.gravity2d.Activities.PlayingActivityClasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.example.gravity2d.Activities.Common.AbstractStateMachine;
import com.example.gravity2d.Activities.Common.SceneView;
import com.example.gravity2d.Activities.Common.StateMachineClient;
import com.example.gravity2d.PhxEngine.Coordinate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * Класс, реализующий инструменты для настройки параметров запуска, а так же для отображения
 * траекторий текущего и предыдущих запусков
 * @author ZiminAS
 */
public class LaunchingView extends SceneView
                           implements StateMachineClient {

    // Во сколько раз длина вектора скорости на экране больше реальной скорости, которая требуется
    // пользователю. Грубо говоря - через сколько секунд запущенный объект достигнет на экране
    // конца вектора скорости при равномерном прямолинейном движении и при отсутствии ускорения
    // времени
    static public double VelocityK = 2000;

    private PlayingMachine mMachine;
    private ScenePlayingModel mScene;
    boolean isInvalidated;


    // Карта, сопоставляющая траектории в mScene с их вариантом, сконвертированным для
    // отображения в LaunchingView. Введена с целью оптимизации процесса отрисовки
    private TrajectoryConverter mTrajectories;

    private void init() {
        mMachine = null;
        mScene = null;
        mTrajectories = new TrajectoryConverter(super.mConverter);
        isInvalidated = true;
    }

    public LaunchingView(Context context) {
        super(context);
        init();
    }

    public LaunchingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void saveToBundle(Bundle data, String prefix) {
        synchronized (mTrajectories) {
            mTrajectories.saveToBundle(data, prefix + "mTrajectories");
        }
    }

    public void loadFromBundle(Bundle data, String prefix) {
        mTrajectories.loadFromBundle(data, prefix + "mTrajectories");
    }

    protected void onConverterChanged(double dx, double dy, double scale) {
        synchronized (mTrajectories) {
            mTrajectories.updateAllTrajectories();
        }
        mMachine.setTimeWrap(mMachine.getTimeWrap() * scale);
    }

    public void setScene(ScenePlayingModel scene) {
        mScene = scene;
        super.setScene(scene);
    }

    @Override //StateMachineClient
    public boolean onAttaching(AbstractStateMachine machine)
    {
        if(machine.getType() != PlayingMachine.MACHINE_TYPE_ID || mMachine != null)
            return false;

        mMachine = (PlayingMachine)machine;
        return true;
    }

    @Override //StateMachineClient
    public void onDeattached(AbstractStateMachine machine) {
        mMachine = null;
    }

    @Override //StateMachineClient
    public void onStateChanged(long oldState, long newState,
                               AbstractStateMachine machine)
    {
        if(newState == PlayingMachine.stateOnPositionUpdate) {
            Integer id = mScene.getCurrentLaunch();
            if (id != 0) {
                synchronized (mTrajectories) {
                    if (!mTrajectories.trajectoryIsExist(id))
                        mTrajectories.addTrajectory(id, new Vector<Coordinate>());
                    mTrajectories.addPoint(id, mMachine.getUpdatedPosition());
                }
            }

        } else if(newState == PlayingMachine.stateOnParamsUpdate) {
            invalidate();
        }
    }

    @Override  //View
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mMachine.getState() == PlayingMachine.statePreparation ||
            mMachine.getState() == PlayingMachine.stateOnParamsUpdate) {

            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_MOVE) {
                // Редактирование параметров запуска
                return onParametersEditing(event);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // Редактирование завершено и производится запуск
                mMachine.onLaunched(mMachine.getLaunchVelocity());
                return true;
            }
        }
        return true;
    }

    /**
     * Функция, исходя из касания к экрану, вычисляет новые параметры запуска объекта
     * @param event Данные о касании к экрану
     * @return Возвращает true. Всегда.
     */
    private boolean onParametersEditing(MotionEvent event) {
        Coordinate touchPoint = new Coordinate(event.getX(), event.getY());
        mConverter.convertToLogic(touchPoint, touchPoint);
        Coordinate launchPoint = mScene.getLaunchPoint();
        // Находим скорость:
        Coordinate velocity = new Coordinate(0, 0);
        Coordinate.initializeVector(velocity, launchPoint, touchPoint);
        double velocityValue = Coordinate.normilizeVector(velocity);
        velocityValue /= VelocityK;
        velocity.setPosition(velocity.x() * velocityValue, velocity.y() * velocityValue);
        // Оповещаем всех об изменении параметров запуска:
        mMachine.onParamsUpdate(velocity);
        return true;
    }

    /**
     * Занимается отрисовкой траекторий запуска
     */
    @Override  // SceneView
    protected void onDraw(Canvas canvas) {
        // NOTE: Отрисовку возможно существенно оптимизировать, кешируя координаты траекторий
        // в физической системе координат, и обновляя их при изменении логической сетки и при
        // добавлении новых траекторий
        super.onDraw(canvas);

        if(mScene == null) {
            isInvalidated = true;
            return;
        }

        // Отобразим все траектории
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawPaint(paint);

        Vector<Coordinate> trajectory = null;
        Coordinate prevPoint = null;

        // Отрисовываем траекторию предыдущих запусков
        paint.setColor(Color.rgb(128, 128, 0));
        synchronized (mTrajectories) {
            Map<Integer, Vector<Coordinate>> allTrajectories =
                    mTrajectories.getAllConverterTrajectories();
            for (Map.Entry<Integer, Vector<Coordinate>> entry : allTrajectories.entrySet()) {
                Integer id = entry.getKey();
                if (id.equals(mScene.getCurrentLaunch()))
                    continue;
                trajectory = entry.getValue();
                prevPoint = null;
                for (Coordinate point : trajectory) {
                    if (prevPoint != null)
                        canvas.drawLine((float) prevPoint.x(), (float) prevPoint.y(),
                                (float) point.x(), (float) point.y(), paint);
                    prevPoint = point;
                }
            }
        }

        // Отрисовываем траекторию текущего запуска:
        paint.setColor(Color.rgb(255, 255, 0));
        prevPoint = null;
        trajectory = mTrajectories.getConvertedTrajectory(mScene.getCurrentLaunch());

        if (trajectory != null) {
            synchronized (trajectory) {
                for (Coordinate point : trajectory) {
                    if (prevPoint != null)
                        canvas.drawLine((float) prevPoint.x(), (float) prevPoint.y(),
                                (float) point.x(), (float) point.y(), paint);
                    prevPoint = point;
                }
            }
        }

        // Отобразим вектор скорости:
        if(mMachine.getState() == PlayingMachine.stateOnParamsUpdate) {
            paint.setColor(Color.rgb(0, 0, 255));
            Coordinate v = mMachine.getLaunchVelocity();
            Coordinate start = mScene.getLaunchPoint();
            drawVector(canvas,paint, start, v, VelocityK);
        }

        isInvalidated = true;
    }
}
