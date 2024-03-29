package com.example.gravity2d.PhxEngine;

/**
 * Класс для общего описания объекта физического движка. Интерфейс и его смысл очень близок
 * к интерфейсу PhxEngineInterface
 * Весь жизненный цикл объекта делится на временные интервалы. Интервалы могут иметь разную длину.
 * В начале каждого временного интервала вызывается фукция prepare().
 * В процессе прохождения временного интервала, один или несколько раз вызывается функция Alive()
 * В конце временного интервала вызывается функция relax.
 * Т.е. один интервал можно представить так:
 * prepare(T) -> N * Alive(T/N) -> relax(T)
 * , где T - длительность интервала, а N - сколько раз на протяжении интервала будет вызвана
 * функция Alive()
 * @author ZiminAS
 * @version 1.0
 */
public interface PhxObjectInterface {

    /**
     * Функция сообщает объекту, чтобы он подготовился к следующему циклу симуляции.
     * Функция вызывается перед PhxEngineInterface.simulationCircle(). Реализуя эту функцию, объект
     * может повлиять на последующую симуляцию (например, добавить к объекту внешних сил,
     * увеличить его начальную скорость, изменить массу объекта и т.п.)
     * @param interval Длина предстоящего цикла симуляции, выраженная в секундах
     */
    void prepare(double interval);

    /**
     * Функция сообщает объекту, что он просуществовал на протяжении interval секунд. Функция
     * вызывается после PhxEngineInterface.simulationCircle().
     * @param interval Сколько секунд(!) прошло с момента последнего вызова функции Alive
     */
    void Alive(double interval);

    /**
     * Функция сообщает объекту, что симуляция временного интервала interval завершена, т.е.
     * перед следующим вызовом Alive() будет вызвана функция prepare()
     * @param interval Длительность прошедшего интервала симуляции (то же число, которое было
     *                 передано в prepare())
     */
    void relax(double interval);

}
