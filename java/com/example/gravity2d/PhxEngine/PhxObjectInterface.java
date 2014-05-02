package com.example.gravity2d.PhxEngine;

/**
 * Created by Алесандр on 02.05.2014.
 */
public interface PhxObjectInterface {

    /**
     * Функция сообщает объекту, что он просуществовал на протяжении interval секунд
     * @param interval Сколько секунд(!) прошло с момента последнего вызова функции Alive
     */
    void Alive(double interval);

}
