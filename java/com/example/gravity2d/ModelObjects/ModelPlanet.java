package com.example.gravity2d.ModelObjects;

import com.example.gravity2d.PhxEngine.NewtonObject;
import com.example.gravity2d.PhxEngine.Coordinate;

/**
 * Класс для описания планеты. Планета является физическим объектом (наследует
 * NewtonObject), дополняя его радиусом. В текущей реализации игры на планету не
 * действуют силы притяжения других планет.
 * @author ZiminAS
 * @version 1.0
 */
public class ModelPlanet extends NewtonObject {
	private double mRadius;
	public double Radius() { return mRadius; }
	public void setRadius(double r) { mRadius = r; } 
	
	public ModelPlanet() {
		super(0, new Coordinate());
		super.mIsStatic = true;
	}
	
	public ModelPlanet(double weight, double r, Coordinate position) {
		super(weight, position);
		mRadius = r;
		// В текущей реализации планеты неподвижны и не притягивают друг друга
		super.mIsStatic = true;
	}
}
