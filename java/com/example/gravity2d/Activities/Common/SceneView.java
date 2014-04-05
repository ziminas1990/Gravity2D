package com.example.gravity2d.Activities.Common;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.gravity2d.ModelObjects.ModelPlanet;
import com.example.gravity2d.ModelObjects.ModelTarget;
import com.example.gravity2d.ModelObjects.SceneModel;
import com.example.gravity2d.PhxEngine.Coordinate;
import com.example.gravity2d.Activities.Common.SurfaceConverter;

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
	
	public SceneView(Context context) {
		super(context);
		mS = 40000 * 40000;
		mConverter = new SurfaceConverter();
		mScene = new SceneModel();
	}
	
	public SceneView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mS = 40000 * 40000;
		mConverter = new SurfaceConverter();
		mScene = new SceneModel();
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
		double ratio = h/w;
		// Находим высоты и ширину области отсечения
		double logW = Math.sqrt(mS * ratio);
		double logH = Math.sqrt(mS / ratio);
		
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
		
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	protected void DrawPlanet(ModelPlanet planet, Canvas canvas, Paint paint) {
		if(planet == null)
			return;
		Coordinate center = mConverter.convertToPhx(planet.Position());
		canvas.drawCircle((float)center.x(), (float)center.y(),
				          (float)mConverter.convertToPhx(planet.Radius()),
				          paint);
	}
	
	protected void DrawTarget(ModelTarget target, Canvas canvas, Paint paint) {
		if(target == null)
			return;
        drawLine(canvas, paint, target.FirstPoint(), target.SecondPoint());
//		Coordinate first = mConverter.convertToPhx(target.FirstPoint());
//		Coordinate second = mConverter.convertToPhx(target.SecondPoint());
//		canvas.drawLine((float)first.x(), (float)first.y(),
//				        (float)second.x(), (float)second.y(), paint);
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
		Coordinate lpoint = mConverter.convertToPhx(mScene.getLaunchPoint());
		canvas.drawCircle((float)lpoint.x(), (float)lpoint.y(),
				          5 /*точка запуска не имеет физич. размера*/, paint);
	}


    /**
     * Вспомогательная функция для отрисовки отрезка, описанного в логической системе координат
     * @param start Начало отрезка
     * @param end Конец отрезка
     */
    protected void drawLine(Canvas canvas, Paint paint, Coordinate start, Coordinate end) {
        Coordinate phxStart = mConverter.convertToPhx(start);
        Coordinate phxEnd = mConverter.convertToPhx(end);
        canvas.drawLine((float)phxStart.x(), (float)phxStart.y(),
                        (float)phxEnd.x(), (float)phxEnd.y(), paint);
    }

    /**
     * Вспомогательная функция для отрисовки ломаной линии, описанной в логической
     * системе координат
     * @param multiLine вершины ломаной линии
     */
    protected void drawMultiLine(Canvas canvas, Paint paint, Vector<Coordinate> multiLine) {
        int size = multiLine.size();
        if(size < 2)
            return;
        Coordinate first = mConverter.convertToPhx(multiLine.get(0));
        for(int i = 1; i < size; i++) {
            Coordinate second = mConverter.convertToPhx(multiLine.get(i));
            canvas.drawLine((float)first.x(), (float)first.y(),
                            (float)second.x(), (float)second.y(), paint);
            first = second;
        }
    }

    /**
     * Вспомогательная функция для отрисовки вектора, описанного в логической системе координат
     * @param start Начальная точка вектора
     * @param vector Координаты вектора
     * @param k Множитель длины вектора
     */
    protected void drawVector(Canvas canvas, Paint paint, Coordinate start,
                              Coordinate vector, double k) {
        Coordinate phxStart = mConverter.convertToPhx(start);
        Coordinate phxVector =
                mConverter.convertToPhx(new Coordinate(vector.x() * k, vector.y() * k));
        canvas.drawLine((float)phxStart.x(), (float)phxStart.y(),
                        (float)phxVector.x(), (float)phxVector.y(), paint);
    }
    protected void drawVector(Canvas canvas, Paint paint, Coordinate start, Coordinate vector) {
        drawVector(canvas, paint, start, vector, 1);
    }
}
