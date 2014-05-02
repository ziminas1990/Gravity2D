package com.example.gravity2d.PhxEngine;

import java.util.HashSet;
import java.util.Set;

public class NewtonEngine extends KinematicsEngine
                          implements PhxEngineInterface
{
	public static final double G = 6.67384e-11;
	private Set<NewtonObject> mObjects;
	
	// Вектора, который создаются в конструкторе и используется при расчётах
	// (чтобы не создавать их каждый раз заного)
	Coordinate forceVector;
	
	public NewtonEngine() {
        mObjects = new HashSet<NewtonObject>();
		forceVector = new Coordinate();
	}
	
	public void SimulationCircle(double interval)
	{
		for(NewtonObject object : mObjects)
			if(object.isStatic() == false)
                addGravityForcesToObject(object);
		super.SimulationCircle(interval);
        for(NewtonObject object : mObjects)
            object.ExternalForces().setPosition(0, 0);
	}
	
	/**
	 * Функция вычисляет равнодействующую силу для некоторого объекта. Функция возвращает
	 * результат через параметр (мне кажется, так будет быстрее, чем создавать
	 * для результата новый объект)
	 * @param object Объект
	 */
	private void addGravityForcesToObject(NewtonObject object)
	{
        Coordinate netForce = object.ExternalForces();
        double objectWeight = object.Weight();
        for(NewtonObject gravityObject : mObjects) {
            if(gravityObject == object)
                continue;
			// Вначале вычислим модуль силы, а потом приведём её к вектору
			Coordinate.initializeVector(forceVector, object.Position(), gravityObject.Position());
            double sqDistance = Coordinate.normilizeVector(forceVector) * 1000;
            double forceModule =
                    G * objectWeight * gravityObject.Weight() / (sqDistance * sqDistance);
			forceVector.setPosition(forceVector.x() * forceModule, forceVector.y() * forceModule);
			netForce.setPosition(netForce.x() + forceVector.x(),
					             netForce.y() + forceVector.y());
		}
	}
	
	public void addObject(NewtonObject object) {
		super.addObject(object);
		mObjects.add(object);
	}

    public void removeObject(NewtonObject object) {
        super.removeObject(object);
        mObjects.remove(object);
    }
}
