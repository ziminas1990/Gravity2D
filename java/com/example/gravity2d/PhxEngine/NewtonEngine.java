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

    @Override // KinematicEngine
    public void simulationCircle(double interval, int count) {
        double subInterval = interval / count;
        Coordinate gravityForce = new Coordinate(0, 0);
        for(int i = 0; i < count; i++) {
            // Добавляем ко всем объектам в качестве внешних сил силы гравитации
            for (NewtonObject object : mObjects)
                if (object.isStatic() == false) {
                    calculateGravityForces(object, gravityForce);
                    object.ExternalForces().addVector(gravityForce);
                    object.Alive(subInterval);
                    object.ExternalForces().subVector(gravityForce);
                }
        }
    }

	/**
	 * Функция вычисляет силу гравитации, действующую на объект
	 * @param object Объект, для которого высчитывается силя гравитации
     * @param force Объект, в которй будет записан результат
	 */
	private void calculateGravityForces(NewtonObject object, Coordinate force)
	{
        double objectWeight = object.Weight();
        force.setPosition(0, 0);
        for(NewtonObject gravityObject : mObjects) {
            if(gravityObject == object)
                continue;
			// Вначале вычислим модуль силы, а потом приведём её к вектору
			Coordinate.initializeVector(forceVector, object.Position(), gravityObject.Position());
            double sqDistance = Coordinate.normilizeVector(forceVector) * 1000;
            double forceModule =
                    G * objectWeight * gravityObject.Weight() / (sqDistance * sqDistance);
			forceVector.setPosition(forceVector.x() * forceModule, forceVector.y() * forceModule);
			force.setPosition(force.x() + forceVector.x(),
					          force.y() + forceVector.y());
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
