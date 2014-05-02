package com.example.gravity2d.Activities.PlayingActivityClasses;

import android.os.Bundle;

import com.example.gravity2d.Activities.Common.StateMachine;
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
    // Управление кораблём
    public static long stateShipSelected = 7;
    public static long stateEngineForceChanged = 9;

    private Coordinate mCurrentPosition;
    private Coordinate mLauncherVector;
    private double mTimeWrap;

    // Космический корабль
    private SpaceShip mShip;

    // В данный момент проводится запуск?
    private boolean mIsLaunched;
    private void inLaunching() { mIsLaunched = true; }
    private void outOfLaunching() {
        mIsLaunched = false;
        mShip.EngineTurnOff();
    }
    // В данный момент производится подготовка к запуску?
    private boolean mIsPreparing;

    public PlayingMachine (SpaceShip ship) {
        super(MACHINE_TYPE_ID);
        super.mTag = "[PlayingMachine]";
        super.setState(stateDefault);
        mCurrentPosition = null;
        mLauncherVector = null;
        mTimeWrap = 1;

        mShip = ship;

        outOfLaunching();
        mIsPreparing = false;
    }

    @Override
    public void saveToBundle(Bundle data, String prefix) {
        super.saveToBundle(data, prefix + "[StateMachine]");
        data.putSerializable(prefix + "mCurrentPosition", mCurrentPosition);
        data.putSerializable(prefix + "mLauncherVector", mLauncherVector);
        data.putDouble(prefix + "mTimeWrap", mTimeWrap);

        data.putSerializable(prefix + "mShip", mShip);

        data.putBoolean(prefix + "mIsLaunched", mIsLaunched);
        data.putBoolean(prefix + "mIsPreparing", mIsPreparing);
    }

    @Override
    public void loadFromBundle(Bundle data, String prefix) {
        super.loadFromBundle(data, prefix + "[StateMachine]");
        mCurrentPosition = (Coordinate)data.getSerializable(prefix + "mCurrentPosition");
        mLauncherVector = (Coordinate)data.getSerializable(prefix + "mLauncherVector");
        mTimeWrap = data.getDouble(prefix + "mTimeWrap");

        mShip = (SpaceShip)data.getSerializable(prefix + "mShip");

        mIsLaunched = data.getBoolean(prefix + "mIsLaunched");
        mIsPreparing = data.getBoolean(prefix + "mIsPreparing");
    }

    public void onPreparing() {
        mIsPreparing = true;
        super.setState(statePreparation);
    }

    /**
     * @return Возвращает, проводится ли в данный момент подготовка к запуску?
     */
    public boolean isPreparing() { return mIsPreparing; }

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
        inLaunching();
        mIsPreparing = false;
        super.setState(stateOnLaunched);
    }

    /**
     * @return Возвращает, проводится ли в данный момент запуск объекта?
     */
    public  boolean isLaunched() { return mIsLaunched; }

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
     * null, если текущее состояние машины отличается от isLaunched
     */
    public Coordinate getCurrentPosition() {
        if(!mIsLaunched)
            return null;
        return mCurrentPosition;
    }

    public boolean onAllTargetsAreHited() {
        outOfLaunching();
        return super.setState(stateOnAllTargetsHited);
    }

    /**
     * Функция вызывается, когда расчёт траектории прекращается (например, столкновение с планетой)
     */
    public void onFinished() {
        outOfLaunching();
        mIsPreparing = false;
        super.setState(stateOnFinished);
    }

    /**
     * Устанавливает ускорение времени
     * @param wrap Коэффициент ускорения времени. Если wrap > 1, то течение времени ускоряется,
     *             если < 1, то замедляется
     */
    public void setTimeWrap(double wrap) { mTimeWrap = wrap; }

    /**
     * @return Возвращает текущий коэффициент ускорения времени
     */
    public double getTimeWrap() { return mTimeWrap; }

    /**
     * @return Возвращает коасмический корабль, которым управляет игрок
     */
    public SpaceShip getSpaceShip() { return mShip; }

    /**
     * Сообщает об изменении вектора ускорения, создаваемого двигателем
     */
    public void onEngineForceChanged() {
        super.setState(stateEngineForceChanged);
    }

    @Override  // AbstractStateMachine
    public boolean reset() {
        outOfLaunching();
        mIsPreparing = false;
        return super.setState(stateDefault);
    }

    @Override  // AbstractStateMachine
    public boolean setState(long state) {
        // Нельзя напрямую устанавливать состояние машины (только через интерфейсные функции)
        return false;
    }
}
