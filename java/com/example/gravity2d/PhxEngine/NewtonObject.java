package com.example.gravity2d.PhxEngine;

/**
 * Класс реализует объект Ньютоновой физики. Он расширяет кинематический объект
 * параметром "Масса".
 * @author ZiminAS
 * @version 1.0
 */
public class NewtonObject extends KinematicsObject
{	
	// Масса
	protected double mWeight;
	public double Weight() { return mWeight; }
	public void setWeight(double weight) { mWeight = weight; }
		
	// Является ли объект статичным (действуют ли на него внешние силы)
	protected boolean mIsStatic;
	public boolean isStatic() { return mIsStatic; }
	public void setIsStatic(boolean isStatic) { mIsStatic = isStatic; }
	
	public NewtonObject(double weight) {
		super();
		mWeight = weight;
        mIsStatic = false;
	}
	
	public NewtonObject(double weight, Coordinate position) {
		super(position);
		mWeight = weight;
        mIsStatic = false;
	}
	
	public NewtonObject(double weight, Coordinate position,
			            Coordinate velocity) {
		super(position, velocity);
		mWeight = weight;
        mIsStatic = false;
	}
}
