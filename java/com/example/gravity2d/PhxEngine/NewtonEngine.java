package com.example.gravity2d.PhxEngine;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NewtonEngine extends KinematicsEngine
                          implements PhxEngineInterface
{
	public static final double G = 6.67384e-11;
	private Set<NewtonObject> mObjects;
	
	// Вектора, который создаются в конструкторе и используется при расчётах
	// (чтобы не создавать их каждый раз заного)
	Coordinate forceVector;
	Coordinate netForce;
	Coordinate acceleration;
	
	public NewtonEngine() {
        mObjects = new HashSet<NewtonObject>();
		forceVector = new Coordinate();
		netForce = new Coordinate();
		acceleration = new Coordinate();
	}
	
	public void SimulationCircle(int interval)
	{
		Iterator<NewtonObject> itObject = mObjects.iterator();
		while(itObject.hasNext()) {
			NewtonObject object = itObject.next();
			if(object == null || object.isStatic() == true)
				continue;
			findAccelerationForObject(object, acceleration);
			object.Acceleration().setPosition(acceleration);
		}
		
		super.SimulationCircle(interval);
	}
	
	/**
	 * Функция вычисляет ускорение для некоторого объекта. Функция возвращает
	 * результат через параметр (мне кажется, так будет быстрее, чем создавать
	 * для результата новый объект)
	 * @param object Объект
	 * @param acceleration Вектор, в который будет записано ускорение
	 */
	private void findAccelerationForObject(NewtonObject object,
			                               Coordinate acceleration)
	{
		netForce.setPosition(0, 0);
		Iterator<NewtonObject> itObject = mObjects.iterator();
		while(itObject.hasNext()) {
			NewtonObject gravityObject = itObject.next();
            if(gravityObject == object)
                continue;
			// Вначале вычислим модуль силы, а потом приведём её к вектору
			Coordinate.initializeVector(forceVector, object.Position(), gravityObject.Position());
            double sqDistance = Coordinate.normilizeVector(forceVector) * 1000;
            double forceModule =
                    G * object.Weight() * gravityObject.Weight() / (sqDistance * sqDistance);
			forceVector.setPosition(forceVector.x() * forceModule, forceVector.y() * forceModule);
			netForce.setPosition(netForce.x() + forceVector.x(),
					             netForce.y() + forceVector.y());
		}

        // Домножение на 0.001, так как нам нужно ускорение, выраженное в км/с^2, а не в метрах
		acceleration.setPosition(netForce.x() * 0.001 / object.Weight(),
			                     netForce.y() * 0.001 / object.Weight());
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
