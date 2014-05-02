package com.example.gravity2d.PhxEngine;

import java.io.Serializable;

/**
 * Класс реализует объект Ньютоновой физики. Он расширяет кинематический объект
 * параметром "Масса".
 * @author ZiminAS
 * @version 1.0
 */
public class NewtonObject extends KinematicsObject
                          implements Serializable
{	
	// Масса
	protected double mWeight;
	public double Weight() { return mWeight; }
	public void setWeight(double weight) { mWeight = weight; }

    // Дополнительные внешние силы
    private Coordinate mExternalForces;
    public Coordinate ExternalForces() { return  mExternalForces; }
    public void addExternalForces(Coordinate force) {
        mExternalForces.addVector(force);
    }


	// Является ли объект статичным (действуют ли на него внешние силы)
	protected boolean mIsStatic;
	public boolean isStatic() { return mIsStatic; }
	public void setIsStatic(boolean isStatic) { mIsStatic = isStatic; }
	
	public NewtonObject(double weight) {
		super();
		mWeight = weight;
        mExternalForces = new Coordinate();
        mIsStatic = false;
	}
	
	public NewtonObject(double weight, Coordinate position) {
		super(position);
		mWeight = weight;
        mExternalForces = new Coordinate();
        mIsStatic = false;
	}
	
	public NewtonObject(double weight, Coordinate position,
			            Coordinate velocity) {
		super(position, velocity);
		mWeight = weight;
        mIsStatic = false;
	}

    @Override //PhxObjectInterface
    public void Alive(double second) {
        super.Alive(second);
        // Домножение на 0.001, так как нам нужно ускорение, выраженное в км/с^2, а не в метрах
        mAcceleration.setPosition(mExternalForces.x() * 0.001 / mWeight,
                mExternalForces.y() * 0.001 / mWeight);
    }
}
