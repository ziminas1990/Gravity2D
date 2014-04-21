package com.example.gravity2d.Activities.Common;

import java.util.Iterator;
import java.util.Set;
import java.lang.Math;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.gravity2d.ModelObjects.ModelPlanet;
import com.example.gravity2d.ModelObjects.ModelTarget;
import com.example.gravity2d.ModelObjects.SceneModel;
import com.example.gravity2d.PhxEngine.Coordinate;

/**
 * Класс для отображения модели. Данный класс реализует отображения планет,
 * мишеней и точки запуска. Все дополнительные элементы (вектора скорости,
 * траектория, запущенный объект) отображаются наследниками, так как их
 * описание не предусмотрено базовой моделью
 * @author ZiminAS
 * @version 1.0
 */
public class SceneView extends View {
	private SceneModel mScene;

	// Площадь, покрываемая областью отсечения (км^2)
	private double mS;
	
	// Преобразователь координат ( Физические <-> Логические )
	protected SurfaceConverter mConverter;

    // Набор полей, используемых для отслеживания жестов:
    private Coordinate mMiddlePoint;
    private double mLength;

	public SceneView(Context context) {
		super(context);
		mS = 40000 * 40000;
		mConverter = new SurfaceConverter();
		mScene = new SceneModel();
        mMiddlePoint = new Coordinate();
	}
	
	public SceneView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mS = 40000 * 40000;
		mConverter = new SurfaceConverter();
		mScene = new SceneModel();
        mMiddlePoint = new Coordinate();
	}
	
	/**
	 * Устанавливает новую сцену для отображения
	 * @param scene Отображаемая сцена
	 */
	public void setScene(SceneModel scene) {
		mScene = scene;
		invalidate();
	}
	
	/**
	 * Функция рассчитывет логическую координатную сетку, исходя из соотношения
	 * сторон экрана и площади сетки
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		double ratio = (double)h/(double)w;
		// Находим высоты и ширину области отсечения
		double logW = Math.sqrt(mS / ratio);
		double logH = mS / logW;
		
		Coordinate logicGrid[] = mConverter.getLogicGrid();
		Coordinate phxGrid[] = mConverter.getPhxGrid();
		
		double tempX = mScene.getLaunchPoint().x() - logW / 2;
		double tempY = mScene.getLaunchPoint().y() + logH / 2;
		logicGrid[0].setPosition(tempX, tempY);
		logicGrid[1].setPosition(tempX + logW, tempY - logH);
		
		phxGrid[0].setPosition(0, 0);
		phxGrid[1].setPosition(w, h);
		
		mConverter.setPhxGrid(phxGrid);
		mConverter.setLogicGrid(logicGrid);
		onConverterChanged();

		super.onSizeChanged(w, h, oldw, oldh);
	}

    /**
     * Данная реализация реагирует только на жесты, которые делаются двумя пальцами. При этом,
     * вычисляется средняя точка между точками касания, и:
     * 1. При перемещении средней точки, перемещается и сцена
     * 2. При увеличении/уменьшении расстояния между точками касания, увеличивается или уменьшается
     *    масштаб изображения (пока не реализовано)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if(event.getPointerCount() != 2)
            return true;

        double middleX = (event.getX(0) + event.getX(1)) * 0.5;
        double middleY = (event.getY(0) + event.getY(1)) * 0.5;
        Coordinate middle = new Coordinate(middleX, middleY);
        double length = Math.sqrt(Math.pow(event.getX(0) - event.getX(1), 2) +
                                  Math.pow(event.getY(0) - event.getY(1), 2));

        if(event.getActionMasked() != MotionEvent.ACTION_POINTER_DOWN) {
            // Смещение:
            double dx = mMiddlePoint.x() - middle.x();
            double dy = mMiddlePoint.y() - middle.y();
            mConverter.lgcTranslate(dx, dy);
            // Масштабирование:
            double scale = mLength / length;
            mConverter.lgcTranslate(middle.x(), middle.y());
            mConverter.lgcScale(scale, scale);
            mConverter.lgcTranslate(-middle.x(), -middle.y());
            // Обновление отображения сцены:
            onConverterChanged();
            invalidate();
        }

        mMiddlePoint.setPosition(middle);
        mLength = length;
        return true;
    }

    /**
     * Функция вызывается тогда, когда имело место изменение mConverter'а
     */
    protected void onConverterChanged() {}

	protected void DrawPlanet(ModelPlanet planet, Canvas canvas, Paint paint) {
		if(planet == null)
			return;
		Coordinate center = mConverter.getPhxPoint(planet.Position());
		canvas.drawCircle((float)center.x(), (float)center.y(),
				          (float)mConverter.convertToPhx(planet.Radius()),
				          paint);
	}
	
	protected void DrawTarget(ModelTarget target, Canvas canvas, Paint paint) {
		if(target == null)
			return;
        paint.setStrokeWidth(3);
        paint.setColor((target.isStrucked()) ? Color.rgb(128, 0, 0) : Color.rgb(255, 0, 0));
        Coordinate phxStart = mConverter.getPhxPoint(target.FirstPoint());
        Coordinate phxEnd = mConverter.getPhxPoint(target.SecondPoint());
        canvas.drawCircle((float) phxStart.x(), (float) phxStart.y(), 3, paint);
        canvas.drawCircle((float)phxEnd.x(), (float)phxEnd.y(), 3, paint);
        canvas.drawLine((float)phxStart.x(), (float)phxStart.y(),
                        (float)phxEnd.x(), (float)phxEnd.y(), paint);
	}
	
	/**
	 * Занимается отрисовкой планет, мишеней и точки запуска
	 */
	@Override  // View
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

        if(mScene == null)
            return;

		// Черный фон:
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		
		paint.setColor(Color.BLACK);
		canvas.drawPaint(paint);

        if(isInEditMode())
            return;

		// Планеты:
		paint.setColor(Color.GREEN);
		Set<ModelPlanet> allPlanets = mScene.getAllPlanets();
		Iterator<ModelPlanet> itPlanet = allPlanets.iterator();
		while(itPlanet.hasNext())
			DrawPlanet(itPlanet.next(), canvas, paint);
		
		// Мишени:
		paint.setColor(Color.YELLOW);
		Set<ModelTarget> allTargets = mScene.getAllTargets();
		Iterator<ModelTarget> itTarget = allTargets.iterator();
		while(itTarget.hasNext())
			DrawTarget(itTarget.next(), canvas, paint);
		
		// Точка запуска:
		paint.setColor(Color.RED);
		Coordinate lpoint = mConverter.getPhxPoint(mScene.getLaunchPoint());
		canvas.drawCircle((float)lpoint.x(), (float)lpoint.y(),
				          5 /*точка запуска не имеет физич. размера*/, paint);
	}


    /**
     * Вспомогательная функция для отрисовки отрезка, описанного в логической системе координат
     * @param start Начало отрезка
     * @param end Конец отрезка
     */
    protected void drawLine(Canvas canvas, Paint paint, Coordinate start, Coordinate end) {
        Coordinate phxStart = mConverter.getPhxPoint(start);
        Coordinate phxEnd = mConverter.getPhxPoint(end);
        canvas.drawLine((float)phxStart.x(), (float)phxStart.y(),
                        (float)phxEnd.x(), (float)phxEnd.y(), paint);
    }

    /**
     * Вспомогательная функция для отрисовки вектора, описанного в логической системе координат
     * @param start Начальная точка вектора
     * @param vector Координаты вектора
     * @param k Множитель длины вектора
     */
    protected void drawVector(Canvas canvas, Paint paint, Coordinate start,
                              Coordinate vector, double k) {
        Coordinate phxStart = mConverter.getPhxPoint(start);
        Coordinate phxVector =
                mConverter.getPhxPoint(new Coordinate(vector.x() * k, vector.y() * k));
        canvas.drawLine((float)phxStart.x(), (float)phxStart.y(),
                        (float)phxVector.x(), (float)phxVector.y(), paint);
    }
}
