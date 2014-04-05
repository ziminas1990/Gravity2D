package com.example.gravity2d.Activities.PlayingActivityClasses;

import com.example.gravity2d.Activities.Common.AbstractStateMachine;
import com.example.gravity2d.Activities.Common.StateMachineClient;
import com.example.gravity2d.ModelObjects.SceneModel;
import com.example.gravity2d.PhxEngine.Coordinate;

import java.util.Vector;

/**
 * Модель сцены расширяется данными о траекториях текущего и предыдущих запусков
 */
public class ScenePlayingModel extends SceneModel
                               implements StateMachineClient {

    private Vector<Coordinate> mCurrentLaunch;
    private Vector<Vector<Coordinate>> mPreviousLaunches;
    private PlayingMachine mMachine;

    private void initData() {
        mCurrentLaunch = new Vector<Coordinate>();
        mPreviousLaunches = new Vector<Vector<Coordinate>>();
        mMachine = null;
    }

    public ScenePlayingModel() {
        initData();
    }

    public ScenePlayingModel(long sceneId, String sceneName) {
        super(sceneId, sceneName);
        initData();
    }

    /**
     * Функцию необходимо вызывать тогда, когда пользователь запросил новый запуск. В этом случае,
     * последний запуск записывается в историю и создаётся новый запуск
     * @return Функция возвращает ссылку на текущий (новый) запуск, аналогично getCurrentLaunch
     */
    public Vector<Coordinate> onNewLaunchStarted() {
        mPreviousLaunches.addElement(mCurrentLaunch);
        mCurrentLaunch = new Vector<Coordinate>();
        return mCurrentLaunch;
    }

    /**
     * @return Возвращает траекторию текущего запуска
     */
    public Vector<Coordinate> getCurrentLaunch() { return  mCurrentLaunch; }

    /**
     * Функция для получения траектории одного из прошлых запусков
     * @param index Порядковый номер запуска (0 - самый первый запуск)
     * @return Возвращает траекторию запуска с порядковым номером index, либо null, если указанного
     * запуска не существует
     */
    public Vector<Coordinate> getHistoricalLaunch(int index) {
        if(index < mPreviousLaunches.size() && index > 0)
            return mPreviousLaunches.get(index);
        return null;
    }

    /**
     * @return Возвращает количество запусков, которые были сделаны до текущего
     */
    public int getLaunchesCount() { return mPreviousLaunches.size(); }

    @Override //StateMachineClient
    public boolean onAttaching(AbstractStateMachine machine) {
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
        if(mMachine == null)
            // Вряд ли такое возможно, но на всякий случай...
            mMachine = (PlayingMachine)machine;

        if(newState == PlayingMachine.stateOnLaunched) {
            mCurrentLaunch = new Vector<Coordinate>();
        } else if(newState == PlayingMachine.stateOnPositionUpdate) {
            mCurrentLaunch.add(new Coordinate(mMachine.getUpdatedPosition()));
        } else if(newState == PlayingMachine.stateOnFinished) {
            //Помещаем траекторию завершённого запуска в историю
            if(mCurrentLaunch != null && mCurrentLaunch.size() != 0)
                mPreviousLaunches.add(mCurrentLaunch);
        }
    }

}
