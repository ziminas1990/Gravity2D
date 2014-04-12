package com.example.gravity2d.Activities.SceneEditorClasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.gravity2d.Activities.Common.*;
import com.example.gravity2d.Activities.Common.AbstractStateMachine;
import com.example.gravity2d.Activities.Common.StateMachineClient;
import com.example.gravity2d.ModelObjects.ModelPlanet;
import com.example.gravity2d.ModelObjects.ModelTarget;
import com.example.gravity2d.PhxEngine.Coordinate;

/**
 * Класс, реализующий Viewer, который может не только отображать текущую сцену,
 * но и позволяет редактировать объекты и добавлять их к сцене. При
 * редактировании используются конечные автоматы из EditorMachine
 * @author ZiminAS
 * @version 1.0
 */
public class EditingViewer extends SceneView
                           implements StateMachineClient  {

	SceneEditMachine mStateMachine;
	
	public EditingViewer(Context context) {
		super(context);
		mStateMachine = null;
	}
	
	public EditingViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		mStateMachine = null;
	}

	private boolean onAttachingToSceneMachine(SceneEditMachine machine)
	{
    	if(mStateMachine != null)
			// Нельзя подключать к двум автоматам сцены одновременно
			return false;
    	mStateMachine = machine;
    	if(machine.getPlanetMachine().attachClient(this) == 0 ||
           machine.getTargetMachine().attachClient(this) == 0)
    		return false;
    	return true;
	}
	
	@Override  // StateMachineClient
    public boolean onAttaching(AbstractStateMachine machine)
	{
		if(machine == null)
			return false;
		
		if(machine.getType() == SceneEditMachine.MACHINE_TYPE_ID)
			return onAttachingToSceneMachine((SceneEditMachine)machine);
        
		if(machine.getType() == PlanetEditMachine.MACHINE_TYPE_ID ||
           machine.getType() == TargetEditMachine.MACHINE_TYPE_ID)
        	return true;
        
		return false;
	}
	
	@Override  // StateMachineClient
	public void onDeattached(AbstractStateMachine machine)
	{
		mStateMachine = null;
	}
	
	@Override  // StateMachineClient
	public void onStateChanged(long oldState, long newState,
                               AbstractStateMachine machine)
	{
		// Всё что нужно - заставить объект перерисоваться
		invalidate();
	}
	
	@Override  // SceneView
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mStateMachine == null)
			return;
		
		long state = mStateMachine.getState();
		Paint paint = new Paint();
		
		if(state == SceneEditMachine.stateEditPlanet) {
            paint.setStyle(Style.STROKE);
            paint.setColor(Color.WHITE);
            DrawPlanet(mStateMachine.getPlanetMachine().getPlanet(), canvas, paint);
        } else if(state == SceneEditMachine.stateEditTarget) {
            DrawTarget(mStateMachine.getTargetMachine().getTarget(), canvas, paint);
        }
	}

	@Override  //View
	public boolean onTouchEvent(MotionEvent event) {
		long state = mStateMachine.getState();
		if(state == SceneEditMachine.stateEditPlanet)
			return onPlanetEditingEvent(event);
		else if(state == SceneEditMachine.stateEditTarget)
            return onTargetEditingEvent(event);
        else
			// Если ничего не редактируется, то SceneView ведёт себя
			// как обычно, например, перемещает сцену
			return super.onTouchEvent(event);
	}

    private boolean onTargetEditingEvent(MotionEvent event) {
        TargetEditMachine machine = mStateMachine.getTargetMachine();
        ModelTarget target = machine.getTarget();
        // Определяем, какую точку редактировать:
        Coordinate point = null;
        long state = machine.getState();
        if(state == TargetEditMachine.stateStart || state == TargetEditMachine.stateFirstPoint)
            point = target.FirstPoint();
        else
            point = target.SecondPoint();

        point.setPosition(event.getX(), event.getY());
        mConverter.convertToLogic(point);
        if(state == TargetEditMachine.stateStart)
            target.SecondPoint().setPosition(point);

        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(state != TargetEditMachine.stateSecondPoint) {
                machine.setState(TargetEditMachine.stateSecondPoint);
            } else {
                machine.setState(TargetEditMachine.stateFirstPoint);
            }
        } else {
            // Переключения н адругую точку не происходит, но нужно оповестить клиентов о том,
            // что имели место изменения в положении мишени
            machine.notifyEverybodyAgain();
        }
        return true;
    }

	private boolean onPlanetEditingEvent(MotionEvent event) {
		// Note: теоретически, в этой функции не должно быть invalidate(), так
		// как EditingViewer должен обновлять своё изображение при изменении
		// состояния машины
		PlanetEditMachine machine = mStateMachine.getPlanetMachine();
		ModelPlanet planet = machine.getPlanet();
		// Первое касание определяет центр планеты, а последующие - радиус
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			Coordinate pos = new Coordinate(event.getX(), event.getY());
			mConverter.convertToLogic(pos);
			planet.setPosition(pos);
			machine.setState(PlanetEditMachine.stateRadius);
			
		} else if(event.getAction() == MotionEvent.ACTION_MOVE) {
			Coordinate center = planet.Position();
			Coordinate edge = new Coordinate(event.getX(), event.getY());
			mConverter.convertToLogic(edge);
			double R = Coordinate.calculateLength(center, edge);
			planet.setRadius(R);
            // Вычисляем массу (если указать радиус Земли, то результат будет равен массе Земли)
            planet.setWeight(4.68 * Math.pow(10, 10) * Math.PI * (R * R * 1000000));
			// Не смотря на то, что состояние машины не изменилось,
			// радиус планеты изменился, и нужно оповестить об этом подписчиков
            machine.notifyEverybodyAgain();
		} else if(event.getAction() == MotionEvent.ACTION_UP) {
			machine.setState(PlanetEditMachine.stateCenter);
		} else if(event.getAction() == MotionEvent.ACTION_CANCEL) {
			machine.setState(PlanetEditMachine.stateCenter);
		}
		return true;
	}
}
