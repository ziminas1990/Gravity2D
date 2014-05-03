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
    public void startSimulation(double interval) {
        for(KinematicsObject object : mObjects)
            object.prepare(interval);
    }

    @Override // PhxEngineInterface
    public void simulationCircle(double interval, int count) {
        double subInterval = interval / count;
        for(int i = 0; i < count; i++)
            for(KinematicsObject object : mObjects)
                object.Alive(subInterval);
    }

    @Override // PhxEngineInterface
    public void endSimulation(double interval) {
        for(KinematicsObject object : mObjects)
            object.relax(interval);
    }
}
