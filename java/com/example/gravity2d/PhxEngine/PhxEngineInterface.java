package com.example.gravity2d.PhxEngine;

/**
 * Очень большой и сложный интерфейс, присущий примитивному физическому движку (разумеется, он не
 * имеет ничего общего с методом Монте-Карло)
 * @author ZiminAS
 * @version 1.0
 */
public interface PhxEngineInterface {

	/**
	 * Данная функция должна изменить текущее состояние модели на состояние,
	 * в котором она будет находится через interval миллисекунд относительно
	 * текущего. Важно понимать имеющееся допущение: на всём интервале interval,
	 * некоторые параметры модели, которые должны изменяться в реальной мире,
	 * здесь остаются неизменными. Например, если объект A притягивает объект
	 * B с некоторой силой F, то эта сила не будет изменяться на протяжении
	 * всего интервала, не смотря на то, что расстояние между A и B изменяется.
	 * Поэтому, чем меньше значение интервала, тем выше точность симуляции.
	 * @param interval Временной интервал симуляции, выраженный в миллисекундах
	 */
	void SimulationCircle(double interval);
	
}
