package com.example.gravity2d.ModelObjects;

import java.util.HashSet;
import java.util.Set;

import com.example.gravity2d.ModelObjects.ModelPlanet;
import com.example.gravity2d.ModelObjects.ModelTarget;
import com.example.gravity2d.PhxEngine.Coordinate;

/**
 * Класс для хранения информации о сцене. Сцена может быть отображена
 * на экране с помощью SceneView и отредактирована с помощью SceneEdit.
 * Сцена содержит следующую информацию: название, все планеты, все цели,
 * которые должен пролететь объект, а так же точка запуска объекта.
 * @autor ZiminAS
 * @version 1.0
 */
public class SceneModel {
	
	private long mSceneId;
	private String mSceneName;
	private Set<ModelPlanet> mPlanets;
	private Set<ModelTarget> mTargets;
	private Coordinate mLaunchPoint;
	
	private void init(long id, String name) {
		mSceneId = id;
		mSceneName = name;
		mPlanets = new HashSet<ModelPlanet>();
		mTargets = new HashSet<ModelTarget>();
		mLaunchPoint = new Coordinate();
	}
	
	public SceneModel() {
		init(-1, "empty");
	}
	
	public SceneModel(long sceneId, String sceneName) {
		init(sceneId, sceneName);
	}
	
	public long getSceneId() { return mSceneId; }
	
	public void setName(String newName) { mSceneName = newName; }
	public String getName() { return mSceneName; }
	
	public void addPlanet(ModelPlanet planet) { mPlanets.add(planet); }
	public Set<ModelPlanet> getAllPlanets() { return mPlanets; }
	
	public void addTarget(ModelTarget target) { mTargets.add(target); }
	public Set<ModelTarget> getAllTargets() { return mTargets; }
	
	public void setLaunchPoint(Coordinate point) { mLaunchPoint = point; }
	public Coordinate getLaunchPoint() { return mLaunchPoint; }
}
