package com.example.gravity2d.Activities.Common;
import com.example.gravity2d.PhxEngine.Coordinate;

/**
 * Класс, реализующий переход между поверхностями, имеющими разные системы
 * координат
 * @author ZiminAS
 */
public class SurfaceConverter {
    // Физическая сетка
    private Coordinate mPhxGrid[];
    private double mPhxW;
    private double mPhxH;
    // Логическая сетка
    private Coordinate mLogicGrid[];
    private double mLogicW;
    private double mLogicH;
    // Оптимизации ради:
    private double mQuotients[];
    private void calculateQuotients() {
        mQuotients[0] = mPhxW / mLogicW;
        mQuotients[1] = mPhxH / mLogicH;
    }

    public SurfaceConverter() {
        mPhxGrid = new Coordinate[2];
        mLogicGrid = new Coordinate[2];
        for(int i = 0; i < 2; i++) {
            mPhxGrid[i] = new Coordinate();
            mLogicGrid[i] = new Coordinate();
        }
        mQuotients = new double[2];
    }

    public void setPhxGrid(Coordinate phxGrid[]) {
        for(int i = 0; i < 2; i++)
            mPhxGrid[i].setPosition(phxGrid[i]);
        mPhxW = mPhxGrid[1].x() - mPhxGrid[0].x();
        mPhxH = mPhxGrid[1].y() - mPhxGrid[0].y();
        calculateQuotients();
    }

    public Coordinate[] getPhxGrid() { return mPhxGrid; }


    public void setLogicGrid(Coordinate logicGrid[]) {
        for(int i = 0; i < 2; i++)
            mLogicGrid[i].setPosition(logicGrid[i]);
        mLogicW = mLogicGrid[1].x() - mLogicGrid[0].x();
        mLogicH = mLogicGrid[1].y() - mLogicGrid[0].y();
        calculateQuotients();
    }

    public Coordinate[] getLogicGrid() { return mLogicGrid; }

    public Coordinate convertToPhx(Coordinate logicPoint) {
        double dx = logicPoint.x() - mLogicGrid[0].x();
        double dy = logicPoint.y() - mLogicGrid[0].y();
        return new Coordinate(mPhxGrid[0].x() + dx * mQuotients[0],
                mPhxGrid[0].y() + dy * mQuotients[1]);
    }

    public void convertToLogic(Coordinate phxPoint) {
        double dx = phxPoint.x() - mPhxGrid[0].x();
        double dy = phxPoint.y() - mPhxGrid[0].y();
        phxPoint.setPosition(mLogicGrid[0].x() + dx / mQuotients[0],
                             mLogicGrid[0].y() + dy / mQuotients[1]);
    }

    public double convertToPhx(double metric) {
        // Здесь предполагается, что соотношение как высоты так и ширины
        // логической и физической системы координат одинаковое
        // (т.е. координатная сетка состоит из "квадратов"). Это позволяет
        // преобразовывать не только координаты, но и метрики
        return metric * mQuotients[0];
    }
}