package com.example.gravity2d.PhxEngine;

/**
 * Класс, реализующий объект кинематической физики. Такой объект является
 * материальной точкой, которая имеет позицию на плоскости, а так же вектор
 * скорости и вектор ускорения.
 * @author ZiminAS
 * @version 1.0
 */
public class KinematicsObject implements PhxObjectInterface {
	// Позиция в пространстве
	protected Coordinate mPosition;
	// Вектор скорости
	protected Coordinate mVelocity;
	// Вектор ускорения
	protected Coordinate mAcceleration;
	
	public KinematicsObject() {
		initDefault();
	}
	
	public KinematicsObject(Coordinate position) {
		initDefault();
		mPosition = position;
	}
	
	public KinematicsObject(Coordinate position, Coordinate velocity) {
		initDefault();
		mPosition = position;
		mVelocity = velocity;
	}
	
	private void initDefault() {
		mPosition = new Coordinate(0, 0);
		mVelocity = new Coordinate(0, 0);
		mAcceleration = new Coordinate(0, 0);
	}
	
	public Coordinate Position() { return mPosition; }
	public void setPosition(Coordinate position) {
        mPosition = (position != null) ? position : new Coordinate();
    }
	
	public Coordinate Velocity() { return mVelocity; }
	public Coordinate Acceleration() { return mAcceleration; }

    @Override   //PhxObjectInterface
    public void Alive(double interval) {
        double x = mPosition.x();
        double y = mPosition.y();
        // Для оптимизации: умножим ускорение на время
        double multAccXInterval = mAcceleration.x() * interval;
        double multAccYInterval = mAcceleration.y() * interval;
        // Вычисляем новую позицию
        x += interval * (mVelocity.x() + multAccXInterval * interval / 2);
        y += interval * (mVelocity.y() + multAccYInterval * interval / 2);
        mPosition.setPosition(x, y);
        // Вычисляем новое значение скорости
        mVelocity.setPosition(mVelocity.x() + multAccXInterval,
                mVelocity.y() + multAccYInterval);
    }
}
