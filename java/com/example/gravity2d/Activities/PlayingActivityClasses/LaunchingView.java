package com.example.gravity2d.Activities.PlayingActivityClasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.gravity2d.Activities.Common.AbstractStateMachine;
import com.example.gravity2d.Activities.Common.SceneView;
import com.example.gravity2d.Activities.Common.StateMachineClient;
import com.example.gravity2d.PhxEngine.Coordinate;

import java.util.HashMap;
import java.util.Map;

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
    private int mCurrentLaunchId;
    private int mPreviousLaunchId;

    private void init() {
        mMachine = null;
        mScene = null;
        isInvalidated = true;
        mTrajectories = new TrajectoryConverter(super.mConverter);
        mCurrentLaunchId = 0;
        mPreviousLaunchId = 0;
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
        if(newState == PlayingMachine.stateOnLaunched) {
            mCurrentLaunchId = mScene.getCurrentLaunch();
            if (!mTrajectories.trajectoryIsExist(mCurrentLaunchId))
                mTrajectories.addTrajectory(mCurrentLaunchId);
        } else if(newState == PlayingMachine.stateOnPositionUpdate) {
            synchronized (mTrajectories) {
                mTrajectories.addPoint(mCurrentLaunchId, mMachine.getCurrentPosition());
            }
        } else if(newState == PlayingMachine.stateOnFinished) {
            if(mPreviousLaunchId != 0)
                mTrajectories.removeTrajectory(mPreviousLaunchId);
            mPreviousLaunchId = mCurrentLaunchId;
            mCurrentLaunchId = 0;
        } else if(newState == PlayingMachine.stateOnParamsUpdate) {
            invalidate();
        }
    }

    @Override  //View
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if(event.getPointerCount() != 1) {
            mMachine.engineTurnOff();
            return true;
        }

        int action = event.getAction();
        if(mMachine.isLaunched()) {
            // Редактирование вектора тяги
            if (action == MotionEvent.ACTION_DOWN) {
                mMachine.engineTurnOn();
                mConverter.convertToLogic(event.getX(), event.getY(),
                                          mMachine.EngineDirectionPoint());
            } else if (action == MotionEvent.ACTION_MOVE) {
                mConverter.convertToLogic(event.getX(), event.getY(),
                                          mMachine.EngineDirectionPoint());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // Редактирование завершено и производится запуск
                mMachine.engineTurnOff();
                return true;
            }

            return true;
        } else if(mMachine.isPreparing()) {
            // Редактирования параметра запуска
            if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_MOVE) {
                return onParametersEditing(event);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
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

    private void drawEngineForce(float engineX, float engineY, Canvas canvas, Paint paint) {
        Coordinate TmpPoint = new Coordinate();
        mConverter.convertVectorToPhx(mMachine.EngineForce(), TmpPoint);
        Coordinate.normilizeVector(TmpPoint);
        canvas.drawLine(engineX, engineY, (float) (engineX + TmpPoint.x() * 200),
                (float) (engineY + TmpPoint.y() * 200), paint);
    }

    private void drawTrajectory(Trajectory trajectory, Canvas canvas, Paint paint) {
        if(trajectory != null) {
            float arrX[] = trajectory.getAllX();
            float arrY[] = trajectory.getAllY();
            int length = trajectory.getLength();
            for (int i = 0; i < length - 1; i++)
                canvas.drawLine(arrX[i], arrY[i], arrX[i + 1], arrY[i + 1], paint);
        }
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

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawPaint(paint);

        synchronized (mTrajectories) {
            // Отрисовываем траекторию предыдущего запуска
            if(mPreviousLaunchId != 0) {
                paint.setColor(Color.rgb(128, 128, 0));
                drawTrajectory(mTrajectories.getConvertedTrajectory(mPreviousLaunchId),
                               canvas, paint);
            }

            // Отрисовываем траекторию текущего запуска:
            paint.setColor(Color.rgb(255, 255, 0));
            if(mCurrentLaunchId != 0) {
                Trajectory trajectory = mTrajectories.getConvertedTrajectory(mCurrentLaunchId);

                if (trajectory != null) {
                    synchronized (trajectory) {
                        drawTrajectory(trajectory, canvas, paint);
                    }
                    if (mMachine.engineIsOn() && trajectory.getLength() != 0) {
                        paint.setColor(Color.rgb(153, 217, 234));
                        paint.setStrokeWidth(3);
                        // отобразим вектор тяги двигателя (ускорение от двигателя):
                        drawEngineForce(trajectory.getX(-1), trajectory.getY(-1), canvas, paint);
                    }
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
