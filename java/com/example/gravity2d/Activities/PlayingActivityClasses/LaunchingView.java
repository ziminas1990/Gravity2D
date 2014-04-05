package com.example.gravity2d.Activities.PlayingActivityClasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.gravity2d.Activities.Common.AbstractStateMachine;
import com.example.gravity2d.Activities.Common.SceneView;
import com.example.gravity2d.Activities.Common.StateMachineClient;
import com.example.gravity2d.PhxEngine.Coordinate;

import java.util.Map;
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
    static public double VelocityK = 3000;

    private PlayingMachine mMachine;
    private ScenePlayingModel mScene;
    private int mCounter;

    // Карта, сопоставляющая траектории в mScene с их вариантом, сконвертированным для
    // отображения в LaunchingView. Введена с целью оптимизации процесса отрисовки
    private Map<Coordinate, Coordinate> mConvertedTrajectory;

    public LaunchingView(Context context) {
        super(context);
        mMachine = null;
        mScene = null;
        mCounter = 0;
    }

    public LaunchingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMachine = null;
        mScene = null;
        mCounter = 0;
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
        // Счётчик mCounter нужен для того, чтобы отрисовывать изменения не при каждом перерасчёте
        // позиции объекта, а каждые N перерасчётов. Например, если позиция объекта вычисляется
        // 50 раз в секунду, то обновление кадра будет 50/N раз в секунду.
        if(newState == PlayingMachine.stateOnPositionUpdate) {
            mCounter++;
            if (mCounter == 20 /*N = 20*/) {
                invalidate();
                mCounter = 0;
            }
        } else if(newState == PlayingMachine.stateOnParamsUpdate) {
            invalidate();

        } else if(newState == PlayingMachine.stateOnFinished) {
            invalidate();
            mCounter = 0;
        }
    }

    @Override  //View
    public boolean onTouchEvent(MotionEvent event) {
        if (mMachine.getState() == PlayingMachine.statePreparation ||
            mMachine.getState() == PlayingMachine.stateOnParamsUpdate) {

            int action = event.getAction();
            if (event.getAction() == MotionEvent.ACTION_DOWN ||
                event.getAction() == MotionEvent.ACTION_MOVE) {
                // Редактирование параметров запуска
                return onParametersEditing(event);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // Редактирование завершено и производится запуск
                mMachine.onLaunched(mMachine.getLaunchVelocity());
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * Функция, исходя из касания к экрану, вычисляет новые параметры запуска объекта
     * @param event Данные о касании к экрану
     * @return Возвращает true. Всегда.
     */
    private boolean onParametersEditing(MotionEvent event) {
        Coordinate touchPoint = new Coordinate(event.getX(), event.getY());
        touchPoint = mConverter.convertToLogic(touchPoint);
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

        if(mScene == null)
            return;

        // Отобразим предыдущие запуски
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.rgb(64, 0, 0));
        canvas.drawPaint(paint);
        long historySize = mScene.getLaunchesCount();
        for(int i = 0; i <= historySize; i++) {
            // Выбираем траекторию для отрисовки:
            Vector<Coordinate> launch = null;
            if(i < historySize) {
                launch = mScene.getHistoricalLaunch(i);
            } else if(i == historySize) {
                //Текущий запуск:
                launch = mScene.getCurrentLaunch();
                paint.setColor(Color.rgb(255, 255, 0));
            }
            // Отрисовываем траекторию:
            if(launch != null)
                drawMultiLine(canvas, paint, launch);
        }

        // Отобразим вектор скорости:
        if(mMachine.getState() == PlayingMachine.stateOnParamsUpdate) {
            paint.setColor(Color.rgb(0, 0, 255));
            Coordinate v = mMachine.getLaunchVelocity();
            Coordinate start = mScene.getLaunchPoint();
            drawVector(canvas,paint, start, v, VelocityK);
        }
    }
}
