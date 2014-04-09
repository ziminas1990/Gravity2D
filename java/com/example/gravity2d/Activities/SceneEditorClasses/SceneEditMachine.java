package com.example.gravity2d.Activities.SceneEditorClasses;


import android.os.Bundle;

import com.example.gravity2d.Activities.Common.StateMachine;

import java.util.Vector;

/**
 * Класс реализует конечный автомат для управления процессом редактирования
 * сцены
 * @author ZiminAS
 * @version 1.0
 */
public class SceneEditMachine extends StateMachine {

	public static long MACHINE_TYPE_ID = 1;
	
	static public long stateStart = 0;
	static public long stateEditPlanet = 1;
	static public long stateEditTarget = 2;
	static public long stateEditLauncher = 3;
	static public long stateApply = 4;

	// Вложенные конечные автоматы для редактирования разных элементов
	private PlanetEditMachine mPlanetMachine;
	
	public SceneEditMachine() {
        super(MACHINE_TYPE_ID);
		mPlanetMachine = new PlanetEditMachine();
		
		super.mTag = "[EditorMachine]";
		super.setState(stateStart);
	}

    @Override  // StateMachine
    public void saveToBundle(Bundle data, String prefix) {
        super.saveToBundle(data, prefix + "[StateMachine]");
        mPlanetMachine.saveToBundle(data, prefix + "mPlanetMachine.");
    }

    @Override  // StateMachine
    public void loadFromBundle(Bundle data, String prefix) {
        super.loadFromBundle(data, prefix + "[StateMachine]");
        mPlanetMachine.loadFromBundle(data, prefix + "mPlanetMachine.");
    }

	public PlanetEditMachine getPlanetMachine() {
		return mPlanetMachine;
	}
	
	@Override  // AbstractStateMachine
	public boolean reset() {
		return setState(stateStart);
	}
	
	@Override  // StateMachine
	public boolean setState(long state) {
		if(state == stateEditPlanet)
			if(mPlanetMachine.reset() != true)
				return reset();
		
		return super.setState(state);
	}
	
	/**
	 * @return Возвращает состояние автомата в виде строки
	 */
	public String getStateAsString() {
		long state = super.getState();
		if(state == stateStart)
			return "Choose an object for editing!";
		else if(state == stateEditPlanet)
			return "Editing planet: " + mPlanetMachine.getStateAsString();
		else if(state == stateEditTarget)
			return "Editing target: ";
		else if(state == stateEditLauncher)
			return "Editing launcher: ";
		return "";
	}
	
}
