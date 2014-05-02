package com.example.gravity2d.PhxEngine;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Класс реализует законы кинематики
 * @author ZiminAS
 * @version 1.0
 */
public class KinematicsEngine implements PhxEngineInterface {
	private Set<KinematicsObject> mObjects;
	void addObject(KinematicsObject object) { mObjects.add(object); }
    public void removeObject(KinematicsObject object) { mObjects.remove(object); }

	public KinematicsEngine() {
        mObjects = new HashSet<KinematicsObject>();
    }

	@Override // PhxEngineInterface
	public void SimulationCircle(double interval) {
		double seconds = interval / 1000.0;
        for(KinematicsObject object : mObjects)
			object.Alive(seconds);
	}
}
