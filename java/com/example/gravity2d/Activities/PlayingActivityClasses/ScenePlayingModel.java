package com.example.gravity2d.Activities.PlayingActivityClasses;

import android.os.Bundle;

import com.example.gravity2d.Activities.Common.AbstractStateMachine;
import com.example.gravity2d.Activities.Common.StateMachineClient;
import com.example.gravity2d.ModelObjects.ModelTarget;
import com.example.gravity2d.ModelObjects.SceneModel;
import com.example.gravity2d.PhxEngine.Coordinate;

import java.util.HashMap;
import java.util.Vector;

/**
 * Модель сцены расширяется данными о траекториях текущего и предыдущих запусков
 */
public class ScenePlayingModel extends SceneModel
                               implements StateMachineClient {

    // Идентификатор  траектория текущего запуска
    private Integer mCurrentLaunchId;
    private Vector<Coordinate> mCurrentLaunch;
    // Идентификатор, который будет присвоен следующему запуску
    private Integer mNextLaunchId;
    // Карта для сопоставления идентификаторов с траекториями запусков
    private HashMap<Integer, Vector<Coordinate>> mLaunches;
    private PlayingMachine mMachine;

    private void initData() {
        mCurrentLaunchId = 0;
        mCurrentLaunch = null;
        mNextLaunchId = 1;
        mLaunches = new HashMap<Integer, Vector<Coordinate>>();
        mMachine = null;
    }

    public ScenePlayingModel() {
        initData();
    }

    public ScenePlayingModel(long sceneId, String sceneName) {
        super(sceneId, sceneName);
        initData();
    }

    public void saveToBundle(Bundle data, String prefix) {
        data.putInt(prefix + "mNextLaunchId", mNextLaunchId.intValue());
        data.putInt(prefix + "mCurrentLaunchId", mCurrentLaunchId.intValue());
        data.putSerializable(prefix + "mCurrentLaunch", mCurrentLaunch);
    }

    public void loadFromBundle(Bundle data, String prefix) {
        mNextLaunchId = data.getInt(prefix + "mNextLaunchId");
        mCurrentLaunchId = data.getInt(prefix + "mCurrentLaunchId");
        mCurrentLaunch = (Vector<Coordinate>)data.getSerializable(prefix + "mCurrentLaunch");
    }

    /**
     * Подготавливает сцену к следующему запуску
     */
    public void prepareToNewLaunch() {
        // Необходимо пометить все мишени как "Не поражённые"
        for(ModelTarget target : getAllTargets())
            target.setStruckState(false);
    }

    /**
     * Функция для получения траектории некоторого запуска
     * @param id Идентификатор запуска, траектория которого запрашивается
     * @return Возвращает траекторию запуска, либо null, если запуска id не существует
     */
    public Vector<Coordinate> getTrajectory(Integer id) {
        return mLaunches.get(id);
    }

    /**
     * @return Возвращает идентификатор текущего запуска
     */
    public Integer getCurrentLaunch() { return mCurrentLaunchId; }

    /**
     * @return Возвращает траекторию текущего запуска
     */
    public Vector<Coordinate> getCurrentLaunchTrajectory() {
        return mLaunches.get(mCurrentLaunchId);
    }

    /**
     * @return Возвращает количество запусков, включая текущий
     */
    public int getLaunchesCount() { return mLaunches.size(); }

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
        if(newState == PlayingMachine.stateOnLaunched) {
            mCurrentLaunch = new Vector<Coordinate>();
            mLaunches.put(mNextLaunchId, mCurrentLaunch);
            mCurrentLaunchId = mNextLaunchId++;
        } else if(newState == PlayingMachine.stateOnPositionUpdate) {
            if(mCurrentLaunch != null)
                mCurrentLaunch.add(new Coordinate(mMachine.getCurrentPosition()));
        } else if(newState == PlayingMachine.stateOnFinished) {
            mCurrentLaunch = null;
            mCurrentLaunchId = 0;
        }
    }
}
