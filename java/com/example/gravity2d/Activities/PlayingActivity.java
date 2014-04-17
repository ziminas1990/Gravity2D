package com.example.gravity2d.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gravity2d.Activities.Common.AbstractStateMachine;
import com.example.gravity2d.Activities.Common.StateMachineClient;
import com.example.gravity2d.Activities.PlayingActivityClasses.LaunchingView;
import com.example.gravity2d.Activities.PlayingActivityClasses.PlayingMachine;
import com.example.gravity2d.Activities.PlayingActivityClasses.ScenePlayingModel;
import com.example.gravity2d.Database.DataBaseHelper;
import com.example.gravity2d.ModelObjects.ModelPlanet;
import com.example.gravity2d.ModelObjects.ModelTarget;
import com.example.gravity2d.ModelObjects.SceneModel;
import com.example.gravity2d.PhxEngine.Coordinate;
import com.example.gravity2d.PhxEngine.NewtonEngine;
import com.example.gravity2d.PhxEngine.NewtonObject;
import com.example.gravity2d.R;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class PlayingActivity extends Activity
                             implements StateMachineClient {

    public static String argSceneID = "SCENE_ID";
    public static String argSceneName = "SCENE_NAME";

    private PlayingMachine mMachine;
    private NewtonEngine mPhxEngine;
    private DataBaseHelper mDbHelper;
    private ScenePlayingModel mScene;

    private Timer mPhxTimer;
    private TimerTask mPhxEvent;

    private TextView mStatus;
    private LaunchingView mViewer;
    private Button mBtnStart;
    private Button mBtnStop;

    private Timer mGUIUpdateTimer;
    private TimerTask mGUIUpdateEvent;

    private NewtonObject mLaunchedObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        mStatus = (TextView)findViewById(R.id.playLblStatus);

        mMachine = new PlayingMachine();
        mMachine.attachClient(this);
        mMachine.reset();

        // Загружаем сцену:
        mDbHelper = new DataBaseHelper(this);
        Intent intent = getIntent();
        long sceneId = intent.getLongExtra(argSceneID, -1);
        String sceneName = intent.getStringExtra(argSceneName);

        mScene = new ScenePlayingModel(sceneId, sceneName);
        mDbHelper.loadScene(mScene);
        mMachine.attachClient(mScene);

        mViewer = (LaunchingView)findViewById(R.id.playViewer);
        mViewer.setScene(mScene);
        mBtnStart = (Button)findViewById(R.id.playBtnStart);
        mBtnStop = (Button)findViewById(R.id.playBtnStop);
        bindActivityViewsWithMachine();

        mPhxEngine = new NewtonEngine();
        mPhxTimer = new Timer();
        mPhxEvent = null;
        mLaunchedObject = new NewtonObject(1);
        loadSceneToPhxEngine(mScene);

        mGUIUpdateTimer = new Timer();
        mGUIUpdateEvent = null;
    }

    @Override
    public void onSaveInstanceState(Bundle data) {
        mScene.saveToBundle(data, "mScene.");
        mViewer.saveToBundle(data, "mViewer.");
        mMachine.saveToBundle(data, "mMachine.");
        data.putSerializable("mLaunchedObject", mLaunchedObject);
    }

    @Override
    public void onRestoreInstanceState(Bundle data) {
        mScene.loadFromBundle(data, "mScene.");
        mViewer.loadFromBundle(data, "mViewer.");
        mMachine.loadFromBundle(data, "mMachine.");

        //Подменяем запускаемый объект в физ. движке на тот, который использовался ранее:
        mPhxEngine.removeObject(mLaunchedObject);
        mLaunchedObject = (NewtonObject)data.getSerializable("mLaunchedObject");
        mPhxEngine.addObject(mLaunchedObject);

        if(mMachine.getState() != PlayingMachine.stateOnPositionUpdate) {
            mMachine.notifyEverybodyAgain();
        } else {
            // Экран был повёрнут в процессе полёта объекта
            runPhxEngineTimer();
            runGuiUpdateTimer();
        }
    }

    @Override
    public void onDestroy() {
        mPhxTimer.cancel();
        mGUIUpdateTimer.cancel();
        mPhxTimer.purge();
        mGUIUpdateTimer.purge();
        super.onDestroy();
    }

    /**
     * Функция загружает сцену в физический движок
     * @param scene Сцена, объекты которой будут загружены в физический движок
     */
    private void loadSceneToPhxEngine(SceneModel scene) {
        Set<ModelPlanet> planets = scene.getAllPlanets();
        Iterator<ModelPlanet> I = planets.iterator();
        while(I.hasNext())
            mPhxEngine.addObject(I.next());
        mPhxEngine.addObject(mLaunchedObject);
    }

    /**
     * Связывает элементы интерфейса активити с конечным автоматом
     */
    private void bindActivityViewsWithMachine() {
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMachine.onPreparing();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMachine.onFinished();
            }
        });
        mBtnStop.setEnabled(false);

        mMachine.attachClient(mViewer);
    }

    @Override // StateMachineClient
    public boolean onAttaching(AbstractStateMachine machine) { return machine == mMachine; }
    @Override // StateMachineClient
    public void onDeattached(AbstractStateMachine machine) { mMachine = null; }

    private String stateAsString(long state) {
        if(state == PlayingMachine.stateDefault)
            return new String("Нажмите кнопку \"Подготовить запуск!\"");
        if(state == PlayingMachine.statePreparation)
            return new String("Укажите вектор скорости");
        if(state == PlayingMachine.stateOnParamsUpdate)
            return new String("Скорость: " + mMachine.getLaunchVelocity().length() + " км/с");
        if(state == PlayingMachine.stateOnLaunched)
            return new String("Объект запущен! Нажмите \"Прервать!\", чтобы прервать запуск");
        if(state == PlayingMachine.stateOnPositionUpdate)
            return new String("Debug: state is stateOnPositionUpdate");
        if(state == PlayingMachine.stateOnFinished)
            return new String("Запуск завершён!");
        return new String("Debug: wtf?!");
    }

    /**
     * Проверяет, не пересёк ли объект ту или иную мишень
     * @param prev Предыдущая позиция объекта
     * @param current Текущая позиция объекта
     * @return
     */
    private boolean targetIsHited (Coordinate prev, Coordinate current) {
        for(ModelTarget target : mScene.getAllTargets()) {
            if(target.isCrossedBy(prev, current)) {
                target.setStruckState(true);
                return true;
            }
        }
        return false;
    }

    /**
     * @return Возвращает true, если все мишени в рамках текущего запуска поражены
     */
    private boolean allTargetsAreHited() {
        for(ModelTarget target : mScene.getAllTargets())
            if(!target.isStrucked())
                return false;
        return true;
    }

    /**
     * Проверяет, не столкнулся ли объект с планетой
     * @return Возвращает true, если объект столкнулся с планетой
     */
    private boolean checkForCrash() {
        Coordinate position = mLaunchedObject.Position();
        for(ModelPlanet planet : mScene.getAllPlanets()) {
            double dx = planet.Position().x() - position.x();
            double dy = planet.Position().y() - position.y();
            double r = planet.Radius();
            if(dx < r && dy < r && dx*dx + dy*dy < r * r)
                return true;
        }
        return false;
    }


    private void runPhxEngineTimer() {
        final int interval = 50; // Как часто отрабатывает таймер
        final int timeWrap = 300; // Ускорение времени относительно реального (должен ровно
                                  // делиться на precision
        final int precision = 5; // Эвристический коэфициент точности (чем больше, тем ниже
                                 // точность и выше производительность)
        mPhxEvent = new TimerTask() {
            @Override
            public void run() {
                int circles_count = timeWrap / precision;
                int circle_interval = interval * precision;
                Coordinate prevPosition = new Coordinate(mLaunchedObject.Position());
                for(int i = 0; i < circles_count; i++)
                    mPhxEngine.SimulationCircle(circle_interval);
                mMachine.onPositionUpdate(mLaunchedObject.Position());
                if(checkForCrash())
                    mMachine.onFinished();
                if(targetIsHited(prevPosition, mLaunchedObject.Position()) &&
                   allTargetsAreHited()) {
                   mMachine.onAllTargetsAreHited();
                   mMachine.onFinished();
                }
            }
        };
        mPhxTimer.schedule(mPhxEvent, 500, interval);
    }

    private void runGuiUpdateTimer() {
        mGUIUpdateEvent = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mViewer.invalidate();
                    }
                });
            }
        };
        mGUIUpdateTimer.schedule(mGUIUpdateEvent, 500, 100);
    }


    @Override // StateMachineClient
    public void onStateChanged(long oldState, final long newState,
                               AbstractStateMachine machine) {

        if((newState != PlayingMachine.stateOnPositionUpdate ||
           oldState != PlayingMachine.stateOnPositionUpdate)) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatus.setText(stateAsString(newState));
                }
            });
        }

        if(newState == PlayingMachine.stateOnLaunched) {
            // запускаем объект
            mScene.prepareToNewLaunch();
            mLaunchedObject.Position().setPosition(mScene.getLaunchPoint());
            mLaunchedObject.Velocity().setPosition(mMachine.getLaunchVelocity());
            runPhxEngineTimer();
            runGuiUpdateTimer();
        } else if (newState == PlayingMachine.statePreparation) {
            mBtnStart.setEnabled(false);
            mBtnStop.setEnabled(true);
        } else if (newState == PlayingMachine.stateOnFinished) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnStop.setEnabled(false);
                    mBtnStart.setEnabled(true);
                    mViewer.invalidate();
                }
            });
            mPhxEvent.cancel();
            mGUIUpdateEvent.cancel();
        }
    }
}
