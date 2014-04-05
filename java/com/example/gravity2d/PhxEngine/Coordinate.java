package com.example.gravity2d.PhxEngine;
import java.lang.Math;

/**
 * Класс для описания  позиции точки, либо вектора с началом в точке (0, 0)
 * @author ZiminAS
 */
public class Coordinate {
	private double mX;
	private double mY;
	
	public Coordinate() {
		mX = 0;
		mY = 0;
	}

    public Coordinate(Coordinate point) {
        mX = point.mX;
        mY = point.mY;
    }

	public Coordinate(double x, double y) {
		mX = x;
		mY = y;
	}
	
	public double x() { return mX; }
	public double y() { return mY; }
	public void setPosition(double x, double y) {
		mY = y;
		mX = x;
	}
	public void setPosition(Coordinate pos) {
		mX = pos.x();
		mY = pos.y();
	}
	
	/**
	 * Функция инициализации координат вектора по двум точкам
	 * @param vector Инициализируемый вектор
	 * @param a Начало вектора
	 * @param b Конец вектора
	 */
	static public void initializeVector(Coordinate vector, Coordinate a, Coordinate b)
	{
		vector.setPosition(b.x() - a.x(), b.y() - a.y());
	}
	
	/**
	 * Функция осуществляет нормализацию вектора (приведение к единичной длине)
	 * @param v Нормализуемый вектор
	 */
	static public double normilizeVector(Coordinate v)
	{
		double length = Math.sqrt(v.mX * v.mX + v.mY * v.mY);
        if(length > 0)
		    v.setPosition(v.mX / length, v.mY / length);
        return length;
	}
	
	/**
	 * Функция вычисляет расстояние между двумя точками
	 * @param a Первая точка
	 * @param b Вторая точка
	 * @return Возвращает расстояние между точками
	 */
	static public double calculateLength(Coordinate a, Coordinate b)
	{
		double diffX = b.mX - a.mX;
		double diffY = b.mY - a.mY;
		return Math.sqrt(diffX * diffX + diffY * diffY);
	}
}
