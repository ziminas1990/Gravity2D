package com.example.gravity2d.Activities.Common;

/**
 * Интерфейс для описания абстрактного конечного автомата
 * @author ZiminAS
 * @version 1.0
 */
public interface AbstractStateMachine {
	
	/**
	 * @return Возвращает тип машины (тип машины определяется наследником)
	 */
	public long getType();
	
	/**
	 * Функция сбрасывает состояние машины в исходное состояние
	 * @return Возвращает true, если машина переведена в исходное состояние
	 */
	public boolean reset();
	
	/**
	 * Устанавливает новое состояние для машины. При этом, все клиенты будут
	 * оповещены о смене состояния
	 * @param state
	 * @return Возвращает true, если состояние успешно изменено
	 */
	public boolean setState(long state);
	
	/**
	 * Функция для получения состояния конечного автомата
	 * @return Возвращает текущее состояние
	 */
	public long getState();
	
	/**
	 * Функция подключает клиента к конечному автомату. Клиент будет оповещаться
	 * об изменении состояния конечного автомата
	 * @param client Подключаемый клиент
	 * @return Возвращает идентификатор, который был выдан клиенту. Если в
	 * качестве идентификатор был передан 0, значит подключение не удалось
	 */
	public long attachClient(StateMachineClient client);
	
	/**
	 * Отключает клиента от конечного автомата
	 * @param clientId Идентифкатор клиента (выдаётся функцией attachClient)
	 * @return Возвращает true, если клиент был успешно отключён
	 */
	public boolean deattachClient(long clientId);
}