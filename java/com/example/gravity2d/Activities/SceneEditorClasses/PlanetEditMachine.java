package com.example.gravity2d.Activities.SceneEditorClasses;

import com.example.gravity2d.Activities.Common.StateMachine;
import com.example.gravity2d.ModelObjects.ModelPlanet;
import com.example.gravity2d.PhxEngine.Coordinate;

public class PlanetEditMachine extends StateMachine {
	public static long MACHINE_TYPE_ID = 2;
	static public long stateCenter = 1;
	static public long stateRadius = 2;
	
	ModelPlanet mPlanet;
	
	public PlanetEditMachine() {
        super(MACHINE_TYPE_ID);

        super.tag = "[PlanetEditMachine]";
		setState(stateCenter);
		mPlanet = null;
	}
	
	/**
	 * Возвращает планету, которая редактируется пользователями конечного
	 * автомата
	 * @return
	 */
	public ModelPlanet getPlanet() {
		return mPlanet;
	}
	
	@Override  // AbstractStateMachine
	public boolean reset() {
		// Начинаем редактировать новую планету
		mPlanet = new ModelPlanet(6 * Math.pow(10, 24), 0, new Coordinate(0, 0));
		return setState(stateCenter);
	}
	
	public String getStateAsString() {
		long state = super.getState();
		if(state == stateCenter)
			return "set center of planet";
		else if(state == stateRadius)
			return "set radius of planet";
		return "";
	}
};
