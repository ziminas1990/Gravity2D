package com.example.gravity2d.Activities.PlayingActivityClasses;

import com.example.gravity2d.PhxEngine.Coordinate;

import java.io.Serializable;
import java.util.Vector;

/**
 * Класс создан с целью оптимизации отображения траекторий. Раньше для хранения траекторий
 * использовался Vector<Coordinate>.
 *  @author ZiminAS
 *  @version 1.0
 */
public class Trajectory implements Serializable {
    private float x[];
    private float y[];
    // Длина массивов x и y (обычно больше длины траектории)
    private int size;
    // Полная длина траектории, т.е. длина массивов x и y
    private int length;
    // Вектор хранит в себе начало и конец видимой части траектории
    // элемент i (i кратно 2) содержит индекс первой точки видимого участка траектории, а i+1 -
    // индекс последней точки видимого участка траектории
    private Vector<Integer> mGaps;

    public Trajectory() {
        x = new float[100];
        y = new float[100];
        size = 100;
        length = 0;
        mGaps = new Vector<Integer>();
    }

    public Trajectory(int length) {
        x = new float[length];
        y = new float[length];
        size = length;
        this.length = 0;
        mGaps = new Vector<Integer>();
    }

    public float[] getAllX() { return x; }
    public float[] getAllY() { return y; }
    public float getX(int index) {
        if(index >= length && index < -length)
            return 0;
        return x[(index >= 0) ? index : length + index];
    }
    public float getY(int index) {
        if(index >= length && index < -length)
            return 0;
        return y[(index >= 0) ? index : length + index];
    }

    /// @return Возвращает полную длину траектории (количество точек в массива getAllX и getAllY
    public int getLength() { return length; }

    /// @return Возвращает вектор разрывов
    public Vector<Integer> getGaps() { return mGaps; }

    public void clear() {
        length = 0;
        mGaps.clear();
    }

    /**
     * Оптимизирует хранение траекторий в памяти (отрезает неиспользуемую часть массива)
     */
    public void optimize() {
        if(size == 0 || length == size)
            return;
        float newX[] = new float[length];
        float newY[] = new float[length];
        System.arraycopy(x, 0, newX, 0, length);
        System.arraycopy(y, 0, newY, 0, length);
        x = newX;
        y = newY;
    }

    /**
     * Расширяет массив таким образом, чтобы в него влезло ещё elementsCount элементов. Разумеется,
     * при расширении массива количество элементов будет выделено "с запасом"
     * @param elementsCount Количество элементов, которые будут добавлены в массив после расширения
     */
    public void expand(int elementsCount) {
        if(size >= length + elementsCount)
            // В массив и так влезает всё что хочется
            return;
        int newSize = size;
        while(newSize < length + elementsCount)
            newSize = (int)(newSize * 1.5 + 1);
        float newX[] = new float[newSize];
        float newY[] = new float[newSize];
        // Копируем данные по траектории в новый массив и подменяем им старый
        System.arraycopy(x, 0, newX, 0, length);
        System.arraycopy(y, 0, newY, 0, length);
        x = newX;
        y = newY;
    }

    public boolean addPoint(Coordinate point) {
        expand(1);
        x[length] = (float)point.x();
        y[length] = (float)point.y();
        length++;
        return true;
    }

    public boolean addPoint(float x, float y) {
        expand(1);
        this.x[length] = x;
        this.y[length] = y;
        length++;
        return true;
    }

    public boolean addPoints(float x[], float y[], int size) {
        expand(size);
        System.arraycopy(x, 0, x, length, size);
        System.arraycopy(y, 0, y, length, size);
        length += size;
        return true;
    }
}