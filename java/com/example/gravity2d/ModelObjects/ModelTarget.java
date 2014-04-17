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
	public boolean isCrossedBy(Coordinate a, Coordinate b) {
		// Решение находилось через матрицы при помощи метода Крамера
		// Пусть отрезок ab - это траектория, cd - это мишень. Далее для
		// удобства введём ряд переменных
		// И ещё: "Premature optimization is the root of all evil" Donald Knuth
		double xba = b.x() - a.x();
		double yba = b.y() - a.y();
		double xdc = mFirstPoint.x() - mSecondPoint.x();
		double ydc = mFirstPoint.y() - mSecondPoint.y();
		double xac = a.x() - mSecondPoint.x();
		double yac = a.y() - mSecondPoint.y();
		/* Уравнение через матрицы:
		 * / xdc  -xba \ * / m \ = / xac \
		 * \ ydc  -yba /   \ n /   \ yac /
		 * Используем метод Крамера:
		 */
		double det = xba * ydc -  xdc * yba;
		if(det == 0)
			// Отрезки параллельны
			return false;

        det = 1 / det;
		// s - расстояние от точки mFirstPoint до точки пересечения
		double n = (xdc * yac - xac * ydc) * det;
        if(n < 0 || n > 1)
            return false;
		double m = (xba * yac - xac * yba) * det;
        if(m < 0 || m > 1)
            return false;
		return true;

	}
}
