package com.example.gravity2d.Activities.SceneListClasses;

import com.example.gravity2d.Activities.Common.StateMachine;
import com.example.gravity2d.ModelObjects.SceneModel;

/**
 * Конечный автомат для управление activity ScenesList
 * @author ZiminAS
 * @version 1.0
 */
public class SceneListMachine extends StateMachine {
	public static long MACHINE_TYPE_ID = 1;
	
	// Какую сцену (и какой элемент) выбрал пользователь
	private SceneModel mSelectedScene;
    // Какую сцену выбрал пользователь для осуществления запусков
    private SceneModel mLaunchingScene;
	// Для какой сцены запрошено редактирование
	private SceneModel mEditingScene;
    // Для какой сцены запрошено удаление
    private SceneModel mDeletingScene;
	
	public static long stateStart = 0;
	// Состояние, когда пользователь запросил создание новой сцены
	public static long stateOnNewSceneCreating = 1;
	// Состояние, когда пользователь выбрал некоторый элемент списка
	public static long stateOnElementSelected = 2;
	// Состояние, когда пользователь запросил запустить сцену (кнопка play)
	public static long stateOnLaunch = 3;
	// Состояние, когда пользователь запросил редактирование сцены
	public static long stateOnEditing = 4;
	// Состояние, когда пользователь запросил удаление сцены
	public static long stateOnRemove = 5;
	// Состояние, когда пользователь отменил выделение
	public static long stateOnElementDeselected = 6;
	
	public SceneListMachine() {
        super(MACHINE_TYPE_ID);
        super.tag = "[PlayingMachine]";
        super.setState(stateStart);
	}
	
	private void resetData() {
		mSelectedScene = null;
        mLaunchingScene = null;
		mEditingScene = null;
        mDeletingScene = null;
	}
	
	@Override  // AbstractStateMachine
	public boolean reset() {
		resetData();
		return super.setState(stateStart);
	}
	
	@Override  // AbstractStateMachine
	public boolean setState(long state) {
		// В качестве эксперимента: попробуем запретить пользователю класса
		// изменять состояние напрямую, переопределив эту функцию таким
		// образом: 
		return false;
	}
	
	/**
	 * Пользователь переходит в состояние "Создание новой сцены"
	 * @return Возвращает true при успешном переключении в новое состояние
	 */
	public boolean onNewSceneCreating() {
		resetData();
		return super.setState(stateOnNewSceneCreating);
	}
	
	/**
	 * Функция должна вызываться, когда пользователь выбрал некоторую сцену
	 * @param scene Описание сцены
	 */
	public boolean onItemSelected(SceneModel scene) {
		resetData();
		mSelectedScene = scene;
		return super.setState(stateOnElementSelected);
	}
	
	/**
	 * @return Возвращает сцену, выбранную пользователем, либо null, если сцена
	 * не выбрана
	 */
	public SceneModel getSelectedScene() {
		if(getState() == stateOnElementSelected)
			return mSelectedScene;
		return null;
	}
	
	public boolean onItemDeselected() {
		resetData();
		return super.setState(stateOnElementDeselected);
	}

    /**
     * Машина переводится в это состояние когда пользователь запросил удаление выбранной сцены
     * @param scene Удаляемая сцена
     */
    public boolean onSceneDelete(SceneModel scene) {
        resetData();
        mDeletingScene = scene;
        return super.setState(stateOnRemove);
    }

    /**
     * @return Функция возвращает удаляемую сцену
     */
    public SceneModel getDeletingScene() {
        if(getState() != stateOnRemove)
            return null;
        return mDeletingScene;
    }

	/**
	 * Функция вызывается, когда пользователь запросил редактирование сцены
	 * @param scene Сцена, для которой запрошено редактирование
	 */
	public boolean onSceneEdit(SceneModel scene) {
		resetData();
		mEditingScene = scene;
		return super.setState(stateOnEditing);
	}
	
	/**
	 * @return Возвращает сцену, для которой запущен процесс редактирования
	 */
	public SceneModel getEditingScene() {
		if(getState() == stateOnEditing)
			return mEditingScene;
		return null;
	}

    /**
     * Функция вызывается, когда пользователь запросил запуск сцены
     * @param scene Сцена, для которой запрошен запуск
     */
    public boolean onSceneLaunch(SceneModel scene) {
        resetData();
        mLaunchingScene = scene;
        return super.setState(stateOnLaunch);
    }

    /**
     * @return Функция возвращает сцену, для которая была запущена пользователем
     */
    public SceneModel getLaunchiedScene() {
        if(getState() == stateOnLaunch)
            return mLaunchingScene;
        return null;
    }
}
