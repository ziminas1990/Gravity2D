package com.example.gravity2d.Activities.PlayingActivityClasses;

import android.os.Bundle;

import com.example.gravity2d.Activities.Common.SurfaceConverter;
import com.example.gravity2d.PhxEngine.Coordinate;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Класс создан в рамках решения задачи оптимизации механизмов отображения сцены в игровом
 * активити. Задача класса - сопоставления траекторий, описанных в логической системе координат
 * (в которой траектории рассчитываются физическим движком) с этими же траекториями, но
 * описанными в физической системе координат (которая используется для отображения траекторий
 * на экране устройства). Кроме хранения, класс отвечает за конвертирование траекторий
 * из логической СК в физическую СК. Класс так же учитывает, что несколько точек траектории
 * могут быть соотнесены с одним физическим пикселем экрана (например, если объект двигается
 * очень медленно).
 * @author ZiminAS
 * @version 1.0
 */
public class TrajectoryConverter {

    // TODO: заменить на одну карту, где значение - пара из двух траекторий
    /// Карта для сопоставления уникального идентификатора с траекторией в логической системе
    /// координат
    private HashMap<Integer, Vector<Coordinate>> mTrajectories;
    /// Карта для сопоставления уникального идентификатора с траекторией в системе координат экрана
    private HashMap<Integer, Trajectory> mConvertedTrajectories;

    /// Карта для сопоставления уникального идентификатора с доп. информацией о траектории
    class ExtraData {
        // Единичный вектор, направленый от N-1 к N-2 точки траектории, причём N - последняя точка
        // Используется для оптимизации
        Coordinate reverseVector;
    }

    /// Конвертер координат из логической СК в физическую СК
    private SurfaceConverter mConverter;

    /**
     * Создание хранилища траекторий
     * @param converter Конвертер координат из логической СК в физическую СК
     */
    public TrajectoryConverter(SurfaceConverter converter) {
        mConverter = converter;
        mConvertedTrajectories = new HashMap<Integer, Trajectory>();
        mTrajectories = new HashMap<Integer, Vector<Coordinate>>();
    }

    public void saveToBundle(Bundle data, String prefix) {
        data.putSerializable(prefix + "mTrajectories", mTrajectories);
    }

    public void loadFromBundle(Bundle data, String prefix) {
        mTrajectories =
                (HashMap<Integer, Vector<Coordinate>>)
                        data.getSerializable(prefix + "mTrajectories");
    }

    /**
     * Добавляет траекторию к хранилищу
     * @param id Уникальный идентификатор траектории
     * @param trajectory Траектория, описанная в логической системе координат
     * @return Возвращает преобразованную в физическую систему координат траекторию
     */
    public Trajectory addTrajectory(Integer id, Vector<Coordinate> trajectory) {
        Trajectory convertedTrajectory = mConvertedTrajectories.get(id);
        if(convertedTrajectory == null) {
            convertedTrajectory = new Trajectory();
            mConvertedTrajectories.put(id, convertedTrajectory);
        }
        convertTrajectory(trajectory, convertedTrajectory);
        mTrajectories.put(id, trajectory);
        return convertedTrajectory;
    }

    public boolean trajectoryIsExist(Integer id) {
        return mTrajectories.containsKey(id);
    }

    /**
     * Функция для удаления траектории из хранилища
     * @param id Уникальный идентификатор траектории
     */
    public void removeTrajectory(Integer id) {
        mConvertedTrajectories.remove(id);
        mTrajectories.remove(id);
    }

    /**
     * Функция для получения сконвертированной траектории
     * @param id Идентификатор траектории
     * @return Возвращает сконвертированную траекторию, либо null, если с траекторией trajectory
     * не сопоставлена сконвертированная траектория
     */
    public Trajectory getConvertedTrajectory(Integer id) {
        return mConvertedTrajectories.get(id);
    }

    /**
     * @return Возвращает все траектории, описанные в системе координат экрана
     */
    public HashMap<Integer, Trajectory> getAllConverterTrajectories() {
        return mConvertedTrajectories;
    }

    /**
     * Функция реализует обновление всех траекторий (все траектории конвертируются заново). Это
     * очень ёмкая (в плане вычислений) процедура, вызывать которую есть смысл только в случае,
     * если изменились параметры объект SurfaceConverter, который был передан в конструктор
     */
    public void updateAllTrajectories() {
        for (Map.Entry<Integer, Vector<Coordinate>> entry : mTrajectories.entrySet()) {
            Integer id = entry.getKey();
            Trajectory converted = mConvertedTrajectories.get(id);
            if (converted == null) {
                converted = new Trajectory();
                mConvertedTrajectories.put(id, converted);
            }
            convertTrajectory(entry.getValue(), converted);
        }
    }

    /**
     * Производит конвертирование траектории trajectory
     * @param trajectory Траектория, требующая конвертации
     * @param convertedTrajectory Результат конвертации
     */
    private void convertTrajectory(Vector<Coordinate> trajectory,
                                   Trajectory convertedTrajectory) {
        Coordinate prevPoint = null;
        if (convertedTrajectory.getLength() != 0)
            convertedTrajectory.clear();

        for (Coordinate point : trajectory) {
            Coordinate phxPoint = mConverter.getPhxPoint(point);

            if (prevPoint == null || Math.floor(phxPoint.x()) != Math.floor(prevPoint.x()) ||
                Math.floor(phxPoint.y()) != Math.floor(prevPoint.y())) {
                convertedTrajectory.addPoint(phxPoint);
                prevPoint = phxPoint;
            }
        }
    }

    /**
     * Функция для добавления новой точки к траектории
     * @param id Уникальный идентификатор траектории
     * @param point Добавляемая точка
     * @return Возвращает true, если добавленная точка была добавлена к конвертированной
     * траектории (описанной в системе координат экрана). Иными словами, возвращаемое значение
     * отвечает на вопрос "Есть ли смысл перерисовывать траекторию на экране?"
     */
    public boolean addPoint(Integer id, Coordinate point) {
        Vector<Coordinate> trajectory = mTrajectories.get(id);
        trajectory.add(new Coordinate(point));

        Trajectory convertedTrajectory = mConvertedTrajectories.get(id);
        if(convertedTrajectory == null)
            // Траектория ещё не была зарегистрирована ранее
            return false;

        Coordinate newPosition = mConverter.getPhxPoint(point);
        if(convertedTrajectory.getLength() == 0)
            return convertedTrajectory.addPoint(newPosition);

        if(Math.floor(newPosition.x()) != Math.floor(convertedTrajectory.getX(-1)) ||
           Math.floor(newPosition.y()) != Math.floor(convertedTrajectory.getY(-1)))
            return convertedTrajectory.addPoint(newPosition);

        return false;
    }
}
