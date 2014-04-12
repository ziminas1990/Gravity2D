package com.example.gravity2d.Activities.Common;

/**
 * Этот интерфейс наследуют те классы, объекты которых должны оповещаться
 * об изменении состояния конечного автомата
 * @author ZiminAS
 * @version 1.0
 */
public interface StateMachineClient {
	
	/**
	 * Функция вызывается, когда клиент подключается к автомату
	 * @param machine Авомат, к которому подключается клиент
	 * @return Возвращает true, если клиент согласился с подключением к
	 * автомату. Если функция вернула false, то подключение клиента к автомату
	 * отменяется (при этом ф-я onDeattached() НЕ вызывается)
	 */
	public boolean onAttaching(AbstractStateMachine machine);
	
	/**
	 * Функция вызывается, когда клиент отключен от автомата
	 * @param machine Автомат, от которого клиент был отключен
	 */
	public void onDeattached(AbstractStateMachine machine);
	
	/**
	 * Функция вызывается, когда изменилось состояние конечного автомата.
	 * Изменение состояния машины из этой функции невозможно
	 * @param oldState Предыдущее состояние
	 * @param newState Новое состояние
	 * @param machine Конечный автомат, изменивший своё состояние
	 */
	public void onStateChanged(long oldState, long newState, AbstractStateMachine machine);
}
