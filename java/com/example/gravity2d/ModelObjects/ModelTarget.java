package com.example.gravity2d.ModelObjects;

import com.example.gravity2d.PhxEngine.Coordinate;

import java.io.Serializable;

/**
 * Класс для описание мишени, через которую должен пролететь объект. Мишень
 * представляет собой отрезок на плоскости (т.е. задаётся двумя точками)
 * @author ZiminAS
 * @version 1.0
 */
public class ModelTarget implements Serializable {
	private Coordinate mFirstPoint;
	private Coordinate mSecondPoint;
	private double length;
	private boolean mIsStruck;

    public ModelTarget() {
        mFirstPoint = new Coordinate(0, 0);
        mSecondPoint = new Coordinate(0, 0);
        length = Coordinate.calculateLength(mFirstPoint, mSecondPoint);
        mIsStruck = false;
    }

	public ModelTarget(Coordinate first, Coordinate second) {
		mFirstPoint = first;
		mSecondPoint = second;
		length = Coordinate.calculateLength(mFirstPoint, mSecondPoint);
		mIsStruck = false;
	}
	
	public boolean isStrucked() { return mIsStruck; }
	public void setStruckState(boolean isStruck) { mIsStruck = isStruck; }
	
	public Coordinate FirstPoint() { return mFirstPoint; }
	public Coordinate SecondPoint() { return mSecondPoint; }
	
	/**
	 * Функция определяет, была ли мишень пересечена некоторым отрезком
	 * @param a Первая точка отрезка
	 * @param b Вторая точка отрезка
	 * @return Возвращает true, если отрезок ab пересёк мишень
	 */
	boolean isCrossedBy(Coordinate a, Coordinate b) {
		// Решение находилось через матрицы при помощи метода Крамера
		// Пусть отрезок ab - это траектория, cd - это мишень. Далее для
		// удобства введём ряд переменных
		// И ещё: "Premature optimization is the root of all evil" Donald Knuth
		double xba = b.x() - a.x();
		double yba = b.y() - a.y();
		double xcd = mFirstPoint.x() - mSecondPoint.x();
		double ycd = mFirstPoint.y() - mSecondPoint.y();
		double xca = mFirstPoint.x() - a.x();
		double yca = mFirstPoint.y() - a.y();
		/* Уравнение через матрицы:
		 * / xba  xcd \ * / S1 \ = / xca \
		 * \ yba  ycd /   \ S2 /   \ yca /
		 * Используем метод Крамера:
		 */
		double det = xba * ycd -  yba * xcd;
		if(det == 0)
			// Отрезки параллельны
			return false;
		
		// s - расстояние от точки mFirstPoint до точки пересечения
		double s = (xba * yca - yba * xca) / det;
		
		// Условие пересечения - s2 > 0 и s2 < length
	    return s > 0 && s < length;
	}
}
