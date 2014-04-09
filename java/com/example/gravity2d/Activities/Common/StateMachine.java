package com.example.gravity2d.Activities.Common;
import java.util.HashMap;

import android.os.Bundle;
import android.util.Log;

/**
 * Класс, реализующий абстрактный конечный автомат с механизмами оповещения
 * клиентов об изменении состояний
 * @author ZiminAS
 * @version 1.0
 */
public class StateMachine implements AbstractStateMachine {
	// Состояние машины
	private long mState;
	private boolean mOnStateChanging;
    private long mMachineId;
	
	// Клиенты машины
	private HashMap<Long, StateMachineClient> mClientsMap;
	private long mNextClientId;

	protected String mTag;

    // Строки для сохранения в Bundle
    static private String KEY_STATE = "mState";
    static private String KEY_MACHINE_ID = "mMachineId";
    static private String KEY_TAG = "mTag";

    public StateMachine(long machineId) {
		mState = 0;
		mOnStateChanging = false;
		mNextClientId = 1;
		mClientsMap = new HashMap<Long, StateMachineClient>();
		mTag = "[StateMachine]";
        mMachineId = machineId;
	}

    /**
     * Сохраняет состояние машины, чтобы его можно было восстановить
     * @param data Объект, в которй необходимо сохранить состояние
     */
    public void saveToBundle(Bundle data, String prefix) {
        data.putLong(prefix + KEY_STATE, mState);
        data.putLong(prefix + KEY_MACHINE_ID, mMachineId);
        data.putString(prefix + KEY_TAG, mTag);
    }

    /**
     * Восстанавливает состояние машины
     * @param data Объект, в которй было сохранено состояние машины
     */
    public void loadFromBundle(Bundle data, String prefix) {
        mState = data.getLong(prefix + KEY_STATE);
        mMachineId = data.getLong(prefix + KEY_MACHINE_ID);
        mTag = data.getString(prefix + KEY_TAG);
    }

	@Override  // AbstractStateMachine
	public long getType() {
		return mMachineId;
	}
	
	@Override  // AbstractStateMachine
	public boolean reset() {
		return true;
	}

    /**
     * Вызов функции приводит к повторному оповещению всех клиентов о текущем состоянии машины,
     * при этом в качестве прыдудещго состояния будет указано текущее состояние (так как
     * изменения состояния не происходит)
     */
    public void notifyEverybodyAgain() {
        notifyEverybody(mState, mState);
    }

    /**
     * Функция производит оповещение всех клиентов о том, что состояние изменилось
     * @param oldState Предыдущее состояние
     * @param newState Новое состояние
     */
    private synchronized void notifyEverybody(long oldState, long newState) {
        mOnStateChanging = true;
        for(StateMachineClient client : mClientsMap.values()) {
            // Даже если какой-то из клиентов отработал неверно, всё-равно необходимо продолжить
            // оповещение остальных клиентов
            try {
                client.onStateChanged(oldState, mState, this);
            } catch (Exception exception) {
                continue;
            }
        }
        mOnStateChanging = false;
    }

	@Override  // AbstractStateMachine
	public synchronized boolean setState(long state) {
        if(mOnStateChanging == true) {
            Log.w(mTag, "Unable to change state while changing state!");
            return false;
        }

        long oldState = mState;
		mState = state;
		// Оповещаем всех клиентов о том, что состояние машины изменилось
		notifyEverybody(oldState, mState);
		return true;
	}
	
	@Override  // AbstractStateMachine
	public long getState() { return mState; }
		
	@Override  // AbstractStateMachine
	public long attachClient(StateMachineClient client) {
		if(!client.onAttaching(this)) {
			// Клиент не согласен с подключением к машине!
			Log.i(mTag, "Client refused connection to machine!");
			return 0;
		}
		long client_id = mNextClientId++;
		mClientsMap.put(client_id, client);
		return client_id++;
	}
	
	@Override  // AbstractStateMachine
	public boolean deattachClient(long clientId) {
		StateMachineClient client = mClientsMap.get(clientId);
		if(client == null) {
			Log.w(mTag, "Client with id=" + clientId + " doesn't exist!");
			return false;
		}
		client.onDeattached(this);
		return mClientsMap.remove(clientId) != null;
	}
}
