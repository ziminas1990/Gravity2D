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
    // Логическая сетка
    private Coordinate mLogicGrid[];
    /*
        Матрицы имеют вид:
        / a 0 c \
       |  0 e f  |
        \ 0 0 1 /
        Поскольку пока что повороты не предполагаются, используются только 4 коэффициента, но
        память выделяется под шесть (когда добавлю повороты, они пригодятся)
    */
    // Матрица преобразования из логической СК в физическую СК
    double phxMatrix[] = {1, 0, 1, 0, 1, 1, 0, 0, 1};
    // Матрица преобразования из физической СК в логическую СК
    double lgcMatrix[] = {1, 0, 1, 0, 1, 1, 0, 0, 1};

    /**
     * Рассчитывает матрицу перехода, для преобразования координат из плоскости from в полскость to,
     * при условии, что плоскости "наложены" друг на друга (и без поворота)
     * @param matrix Сюда будет записана матрица перехода (размер: 6)
     * @param from Плоскость, из которой осуществляется переход (размер: 2)
     * @param to Плоскость, в которую осуществляется переход (размер: 2)
     */
    private void calculateMatrix(double matrix[], Coordinate from[], Coordinate to[]) {
        // Коэффициент a:
        matrix[0] = (to[1].x() - to[0].x()) / (from[1].x() - from[0].x());
        // Коэффициент e:
        matrix[4] = (to[1].y() - to[0].y()) / (from[1].y() - from[0].y());
        // Коэффициент c:
        matrix[2] = to[0].x() - matrix[0] * from[0].x();
        // Коэффициент f:
        matrix[5] = to[0].y() - matrix[4] * from[0].y();
    }



    public SurfaceConverter() {
        mPhxGrid = new Coordinate[2];
        mLogicGrid = new Coordinate[2];
        for(int i = 0; i < 2; i++) {
            mPhxGrid[i] = new Coordinate();
            mLogicGrid[i] = new Coordinate();
        }
    }

    public void setPhxGrid(Coordinate phxGrid[]) {
        for(int i = 0; i < 2; i++)
            mPhxGrid[i].setPosition(phxGrid[i]);
        calculateMatrix(phxMatrix, mLogicGrid, mPhxGrid);
        calculateMatrix(lgcMatrix, mPhxGrid, mLogicGrid);
    }

    public Coordinate[] getPhxGrid() { return mPhxGrid; }

    public void setLogicGrid(Coordinate logicGrid[]) {
        for(int i = 0; i < 2; i++)
            mLogicGrid[i].setPosition(logicGrid[i]);
        calculateMatrix(phxMatrix, mLogicGrid, mPhxGrid);
        calculateMatrix(lgcMatrix, mPhxGrid, mLogicGrid);
    }

    public Coordinate[] getLogicGrid() { return mLogicGrid; }

    /**
     * Выполняет перемещение логической плоскости на физической плоскость
     * @param x Смещение по оси X физической (!) плоскости
     * @param y Смещение по оси Y физической (!) плоскости
     */
    public void lgcTranslate(double x, double y) {
        /**
         Преобразование свдоится к перемножению матриц:
         | a 0 c |   | 1 0 x |   | a 0 ax+c |
         | 0 e f | * | 0 1 y | = | 0 b ey+f |
         | 0 0 1 |   | 0 0 1 |   | 0 0 1    |
         lgcMatrix
         */
        lgcMatrix[2] += lgcMatrix[0] * x;
        lgcMatrix[5] += lgcMatrix[4] * y;

        // Теперь нужно изменить координаты лог. плоскости и вычислить матрицу phxMatrix
        convertToLogic(mPhxGrid[0], mLogicGrid[0]);
        convertToLogic(mPhxGrid[1], mLogicGrid[1]);
        calculateMatrix(phxMatrix, mLogicGrid, mPhxGrid);
    }

    /**
     * Выполняет масштабирование логической плоскости
     * @param x Масштабирование по оси X
     * @param y Масштабирование по оси Y
     */
    public void lgcScale(double x, double y) {
        /**
         Преобразование свдоится к перемножению матриц:
         | a 0 c |   | x 0 0 |   | ax 0  c |
         | 0 e f | * | 0 y 0 | = | 0  ey f |
         | 0 0 1 |   | 0 0 1 |   | 0  0  1 |
         lgcMatrix
         */
        lgcMatrix[0] *= x;
        lgcMatrix[4] *= y;

        // Теперь нужно изменить координаты лог. плоскости и вычислить матрицу phxMatrix
        convertToLogic(mPhxGrid[0], mLogicGrid[0]);
        convertToLogic(mPhxGrid[1], mLogicGrid[1]);
        calculateMatrix(phxMatrix, mLogicGrid, mPhxGrid);
    }

    public void convertToPhx(Coordinate logicPoint, Coordinate phxPoint) {
        phxPoint.setPosition(logicPoint.x() * phxMatrix[0] + phxMatrix[2],
                             logicPoint.y() * phxMatrix[4] + phxMatrix[5]);
    }

    public Coordinate getPhxPoint(double x, double y) {
        return new Coordinate(x * phxMatrix[0] + phxMatrix[2], y * phxMatrix[4] + phxMatrix[5]);
    }

    public Coordinate getPhxPoint(Coordinate logicPoint) {
        return new Coordinate(logicPoint.x() * phxMatrix[0] + phxMatrix[2],
                              logicPoint.y() * phxMatrix[4] + phxMatrix[5]);
    }

    public void convertToLogic(Coordinate phxPoint, Coordinate lgcPoint) {
        lgcPoint.setPosition(phxPoint.x() * lgcMatrix[0] + lgcMatrix[2],
                             phxPoint.y() * lgcMatrix[4] + lgcMatrix[5]);
    }

    public Coordinate getLogicPoint(double x, double y) {
        return new Coordinate(x * lgcMatrix[0] + lgcMatrix[2], y * lgcMatrix[4] + lgcMatrix[5]);
    }

    public Coordinate getLogicPoint(Coordinate phxPoint) {
        return new Coordinate(phxPoint.x() * lgcMatrix[0] + lgcMatrix[2],
                              phxPoint.y() * lgcMatrix[4] + lgcMatrix[5]);
    }

    public double convertToPhx(double metric) {
        // Здесь предполагается, что соотношение как высоты так и ширины
        // логической и физической системы координат одинаковое
        // (т.е. координатная сетка состоит из "квадратов"). Это позволяет
        // преобразовывать не только координаты, но и метрики
        return metric * phxMatrix[0];
    }
}