package com.example.gravity2d.Activities.Common;
import java.util.HashMap;
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

	protected String tag;
	
	public StateMachine(long machineId) {
		mState = 0;
		mOnStateChanging = false;
		mNextClientId = 1;
		mClientsMap = new HashMap<Long, StateMachineClient>();
		tag = "[StateMachine]";
        mMachineId = machineId;
	}
	
	@Override  // AbstractStateMachine
	public long getType() {
		return mMachineId;
	}
	
	@Override  // AbstractStateMachine
	public boolean reset() {
		return true;
	}
	
	@Override  // AbstractStateMachine
	public synchronized boolean setState(long state) {
        if(mOnStateChanging == true) {
            Log.w(tag, "Unable to change state while changing state!");
            return false;
        }

        long oldState = mState;
		mState = state;
		// Оповещаем всех клиентов о том, что состояние машины изменилось
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
		return true;
	}
	
	@Override  // AbstractStateMachine
	public long getState() { return mState; }
		
	@Override  // AbstractStateMachine
	public long attachClient(StateMachineClient client) {
		if(!client.onAttaching(this)) {
			// Клиент не согласен с подключением к машине!
			Log.i(tag, "Client refused connection to machine!");
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
			Log.w(tag, "Client with id=" + clientId + " doesn't exist!");
			return false;
		}
		client.onDeattached(this);
		return mClientsMap.remove(clientId) != null;
	}
}
