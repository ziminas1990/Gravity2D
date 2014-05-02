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
    /// Карта для сопоставления уникального идентификатора с траекторией в логической системе
    /// координат
    private HashMap<Integer, Trajectory> mTrajectories;
    /// Карта для сопоставления уникального идентификатора с траекторией в системе координат экрана
    private HashMap<Integer, Trajectory> mConvertedTrajectories;

    private Trajectory mTemplTrajectory;

    /// Конвертер координат из логической СК в физическую СК
    private SurfaceConverter mConverter;

    /**
     * Создание хранилища траекторий
     * @param converter Конвертер координат из логической СК в физическую СК
     */
    public TrajectoryConverter(SurfaceConverter converter) {
        mConverter = converter;
        mConvertedTrajectories = new HashMap<Integer, Trajectory>();
        mTrajectories = new HashMap<Integer, Trajectory>();
        mTemplTrajectory = new Trajectory();
    }

    public void saveToBundle(Bundle data, String prefix) {
        data.putSerializable(prefix + "mTrajectories", mTrajectories);
    }

    public void loadFromBundle(Bundle data, String prefix) {
        mTrajectories =
                (HashMap<Integer, Trajectory>)data.getSerializable(prefix + "mTrajectories");
    }

    /**
     * Регистрирует новую траекторию в хранилище
     * @param id Уникальный идентификатор траектории
     * @return После вызова функции, к новой траектории можно будет добавлять точки
     */
    public void addTrajectory(Integer id) {
        Trajectory trajectory = new Trajectory();
        mTrajectories.put(id, trajectory);
        Trajectory convertedTrajectory = mConvertedTrajectories.get(id);
        if(convertedTrajectory == null) {
            convertedTrajectory = new Trajectory();
            mConvertedTrajectories.put(id, convertedTrajectory);
        }
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
        for (Map.Entry<Integer, Trajectory> entry : mTrajectories.entrySet()) {
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
    private void convertTrajectory(Trajectory trajectory, Trajectory convertedTrajectory) {
        if (convertedTrajectory.getLength() != 0)
            convertedTrajectory.clear();

        int length = trajectory.getLength();
        if(length == 0)
            return;

        mTemplTrajectory.clear();
        mTemplTrajectory.expand(length);
        float allX[] = mTemplTrajectory.getAllX();
        float allY[] = mTemplTrajectory.getAllY();
        mConverter.convertToPhx(trajectory.getAllX(), trajectory.getAllY(), allX, allY, length);

        Coordinate phxGrid[] = mConverter.getPhxGrid();
        boolean isOutOfBounder = true;
        Vector<Integer> gaps = convertedTrajectory.getGaps();
        gaps.clear();
        /*
            Условие для оптимизации. Точка добавляется только если:
            1. Расстояние от соседней точки на экране - не менее трёх пикселей (примерно)
            2. Точка находится на экране, а не за его границами
        */
        int lastAddedPoint = 0;
        for (int i = 0; i < length; i++) {
            if ((Math.abs(allX[i] - allX[lastAddedPoint]) > 15 ||
                Math.abs(allY[i] - allY[lastAddedPoint]) > 15)) {

                if(allX[i] < phxGrid[0].x() || allX[i] > phxGrid[1].x() ||
                   allY[i] < phxGrid[0].y() || allY[i] > phxGrid[1].y()) {
                    if(!isOutOfBounder) {
                        // Отмечаем разрыв
                        convertedTrajectory.addPoint(allX[i], allY[i]);
                        isOutOfBounder = true;
                        gaps.add(convertedTrajectory.getLength() - 1);
                    }
                } else {
                    if(isOutOfBounder) {
                        if(i > 0)
                            convertedTrajectory.addPoint(allX[i - 1], allY[i - 1]);
                        isOutOfBounder = false;
                    }
                    convertedTrajectory.addPoint(allX[i], allY[i]);
                    lastAddedPoint = i;
                }
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
        Trajectory trajectory = mTrajectories.get(id);
        trajectory.addPoint((float)point.x(), (float)point.y());

        Trajectory convertedTrajectory = mConvertedTrajectories.get(id);
        if(convertedTrajectory == null)
            // Траектория ещё не была зарегистрирована ранее
            return false;

        Coordinate newPosition = mConverter.getPhxPoint(point);
        if(convertedTrajectory.getLength() == 0)
            return convertedTrajectory.addPoint(newPosition);

        if (// Расстояние до предыдущей точки - не менее трёх пикселей
            convertedTrajectory.getLength() == 0 ||
            Math.abs(newPosition.x() - convertedTrajectory.getX(-1)) > 2 ||
            Math.abs(newPosition.y() - convertedTrajectory.getY(-1)) > 2) {
            return convertedTrajectory.addPoint(newPosition);
        }

        return false;
    }
}
