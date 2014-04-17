package com.example.gravity2d.Activities.PlayingActivityClasses;

import android.os.Bundle;

import com.example.gravity2d.Activities.Common.StateMachine;
import com.example.gravity2d.ModelObjects.ModelPlanet;
import com.example.gravity2d.PhxEngine.Coordinate;

/**
 * Конечный автомат для управления запусками
 * @author ZiminAS
 * @version 1.0
 */
public class PlayingMachine extends StateMachine {
    public static long MACHINE_TYPE_ID = 1;

    // Исходное состояние (reset)
    public static long stateDefault;
    // Режим "Подготовка к запуску" (onPreparing)
    public static long statePreparation = 1;
    // Настройки запуска (onParamsUpdate
    public static long stateOnParamsUpdate = 2;
    // Запуск, обновление позиции, окончание запуска
    public static long stateOnLaunched = 3;
    public static long stateOnPositionUpdate = 4;
    public static long stateOnAllTargetsHited = 4;
    public static long stateOnFinished = 6;

    private Coordinate mCurrentPosition;
    private Coordinate mLauncherVector;

    public PlayingMachine () {
        super(MACHINE_TYPE_ID);
        super.mTag = "[PlayingMachine]";
        super.setState(stateDefault);
        mCurrentPosition = null;
        mLauncherVector = null;
    }

    @Override
    public void saveToBundle(Bundle data, String prefix) {
        super.saveToBundle(data, prefix + "[StateMachine]");
        data.putSerializable(prefix + "mCurrentPosition", mCurrentPosition);
        data.putSerializable(prefix + "mLauncherVector", mLauncherVector);
    }

    @Override
    public void loadFromBundle(Bundle data, String prefix) {
        super.loadFromBundle(data, prefix + "[StateMachine]");
        mCurrentPosition = (Coordinate)data.getSerializable(prefix + "mCurrentPosition");
        mLauncherVector = (Coordinate)data.getSerializable(prefix + "mLauncherVector");
    }

    public void onPreparing() {
        super.setState(statePreparation);
    }

    /**
     * Функция вызывается, когда пользователь изменил параметры запуска (вектор скорости)
     * @param launcherVector Вектор скорости
     */
    public void onParamsUpdate(Coordinate launcherVector) {
        mLauncherVector = launcherVector;
        super.setState(stateOnParamsUpdate);
    }

    /**
     * Функция вызывается, когда пользователь запросил запуск
     */
    public void onLaunched(Coordinate launcherVector) {
        mLauncherVector = launcherVector;
        super.setState(stateOnLaunched);
    }

    /**
     * @return Возвращает текущий вектор скорости, либо null, если текущее состояние машины
     * отличается от stateOnParamsUpdate или stateOnLaunched
     */
    public Coordinate getLaunchVelocity() {
        if(getState() == stateOnParamsUpdate || getState() == stateOnLaunched)
            return mLauncherVector;
        return null;
    }

    /**
     * Функция для оповещения о том, что текущая позиция объекта изменилась
     * @param newPosition Новая позиция объекта
     */
    public void onPositionUpdate(Coordinate newPosition) {
        mCurrentPosition = newPosition;
        super.setState(stateOnPositionUpdate);
    }

    /**
     * @return Возвращает обновлённую позицию объекта (переданную в onPositionUpdate), либо
     * null, если текущее состояние машины отличается от stateOnPositionUpdate
     */
    public Coordinate getUpdatedPosition() {
        if(getState() != stateOnPositionUpdate)
            return null;
        return mCurrentPosition;
    }

    public boolean onAllTargetsAreHited() {
        return super.setState(stateOnAllTargetsHited);
    }

    /**
     * Функция вызывается, когда расчёт траектории прекращается (например, столкновение с планетой)
     */
    public void onFinished() {
        super.setState(stateOnFinished);
    }

    @Override  // AbstractStateMachine
    public boolean reset() {
        return super.setState(stateDefault);
    }

    @Override  // AbstractStateMachine
    public boolean setState(long state) {
        // Нельзя напрямую устанавливать состояние машины (только через интерфейсные функции)
        return false;
    }
}
