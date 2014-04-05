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
    private Handler mHandler;
    TextView mStatus;

    private NewtonObject mLaunchedObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        mStatus = (TextView)findViewById(R.id.playLblStatus);

        mMachine = new PlayingMachine();
        mMachine.attachClient(this);
        mMachine.reset();

        mHandler = null;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // Пока что предполагается только одно возможное сообщение от таймера,
                // который занимается расчётом позиции запущенного объекта
                mMachine.onPositionUpdate(mLaunchedObject.Position());
            }
        };

        // Загружаем сцену:
        mDbHelper = new DataBaseHelper(this);
        Intent intent = getIntent();
        long sceneId = intent.getLongExtra(argSceneID, -1);
        String sceneName = intent.getStringExtra(argSceneName);

        mScene = new ScenePlayingModel(sceneId, sceneName);
        mDbHelper.loadScene(mScene);
        mMachine.attachClient(mScene);

        LaunchingView viewer = (LaunchingView)findViewById(R.id.playViewer);
        viewer.setScene(mScene);

        mPhxEngine = new NewtonEngine();
        mPhxTimer = new Timer();
        mPhxEvent = null;
        mLaunchedObject = new NewtonObject(1);
        loadSceneToPhxEngine(mScene);

        bindActivityViewsWithMachine();
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

        Button btnStart = (Button)findViewById(R.id.playBtnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMachine.onPreparing();
            }
        });

        Button btnStop = (Button)findViewById(R.id.playBtnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMachine.onFinished();
            }
        });

        LaunchingView viewer = (LaunchingView)findViewById(R.id.playViewer);
        mMachine.attachClient(viewer);
    }

    @Override // StateMachineClient
    public boolean onAttaching(AbstractStateMachine machine) { return machine == mMachine; }
    @Override // StateMachineClient
    public void onDeattached(AbstractStateMachine machine) { mMachine = null; }

    private String stateAsString(long state) {
        if(state == PlayingMachine.stateDefault)
            return new String("Debug: state is stateDefault");
        if(state == PlayingMachine.statePreparation)
            return new String("Debug: state is statePreparation");
        if(state == PlayingMachine.stateOnParamsUpdate)
            return new String("Debug: state is stateOnParamsUpdate");
        if(state == PlayingMachine.stateOnLaunched)
            return new String("Debug: state is stateOnLaunched");
        if(state == PlayingMachine.stateOnPositionUpdate)
            return new String("Debug: state is stateOnPositionUpdate");
        if(state == PlayingMachine.stateOnFinished)
            return new String("Debug: state is stateOnFinished");
        return new String("Debug: wtf?!");
    }

    private void onLaunching() {
        // Красота да и только! :)
        final int interval = 10;
        final int timeWrap = 100;
        mLaunchedObject.Position().setPosition(mScene.getLaunchPoint());
        mLaunchedObject.Velocity().setPosition(mMachine.getLaunchVelocity());
        mPhxEvent = new TimerTask() {
            @Override
            public void run() {
                mPhxEngine.SimulationCircle(interval * timeWrap);
                mHandler.sendMessage(mHandler.obtainMessage());
            }
        };
        mPhxTimer.schedule(mPhxEvent, 500, interval);
    }


    @Override // StateMachineClient
    public void onStateChanged(long oldState, long newState,
                               AbstractStateMachine machine) {
        mStatus.setText(stateAsString(newState));
        if(newState == PlayingMachine.stateOnLaunched) {
            // запускаем вычисление
            onLaunching();
        } else if (newState == PlayingMachine.stateOnFinished) {
            mPhxEvent.cancel();
        }
    }
}
