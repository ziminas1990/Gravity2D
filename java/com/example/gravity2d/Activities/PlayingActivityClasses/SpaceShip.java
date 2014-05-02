package com.example.gravity2d.Activities.PlayingActivityClasses;

import com.example.gravity2d.PhxEngine.Coordinate;
import com.example.gravity2d.PhxEngine.NewtonObject;

import java.io.Serializable;

/**
 * Класс для представления космического корабля
 * Created by Алесандр on 02.05.2014.
 */
public class SpaceShip extends NewtonObject implements Serializable {
    private boolean mEngineOn;                 // Определяет, включен ли двигатель
    private Coordinate mEngineDirectionPoint; // Точка, к которой направлена тяга двигателя
    private Coordinate mEngineForce;          // Сила, создаваемая двигателем

    private double mFuel;    //Сколько сейчас топлива в баках
    private double mMaxFuel; // Максимально возможное количество топлива


    public SpaceShip(double weight) {
        super(weight);
        mEngineOn = false;
        mEngineDirectionPoint = new Coordinate();
        mEngineForce = new Coordinate();
        mMaxFuel = 1000000;
        mFuel = 0;
    }

    // Функции для работы с двигателем корабля
    public Coordinate getEngineDirectionPoint() { return mEngineDirectionPoint; }
    public Coordinate getEngineForce() { return mEngineForce; }
    public void EngineTurnOn() { mEngineOn = true; }
    public void EngineTurnOff() { mEngineOn = false; }
    public boolean EngineIsOn() { return mEngineOn; }

    // Функции для работы с топливом корабля
    public double Fuel() { return mFuel; }
    public double MaxFuel() { return mMaxFuel; }
    public void setFuel(double fuel) { mFuel = fuel; }
    public void increaseFuel(double inc) {
        mFuel -= inc;
        if(mFuel < 0)
            mFuel = 0;
    }

}
