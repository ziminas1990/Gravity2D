package com.example.gravity2d.Activities;

import com.example.gravity2d.Activities.Common.*;

import com.example.gravity2d.Activities.SceneEditorClasses.PlanetEditMachine;
import com.example.gravity2d.Activities.SceneEditorClasses.TargetEditMachine;
import com.example.gravity2d.ModelObjects.ModelPlanet;
import com.example.gravity2d.ModelObjects.ModelTarget;
import com.example.gravity2d.R;
import com.example.gravity2d.Activities.SceneEditorClasses.SceneEditMachine;
import com.example.gravity2d.Activities.SceneEditorClasses.EditingViewer;
import com.example.gravity2d.Database.DataBaseHelper;
import com.example.gravity2d.ModelObjects.SceneModel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SceneEditorActivity extends Activity
                                 implements StateMachineClient {

	public static String argSceneID = "SCENE_ID";
	public static String argSceneName = "SCENE_NAME";

	private SceneEditMachine mStateMachine;
	private SceneModel mScene;
	private DataBaseHelper mDbHelper;
	
	private Button mBtnPlanet;
	private Button mBtnTarget;
	private Button mBtnLauncher;
	private Button mBtnCancel;
	private Button mBtnApply;
	private TextView mTextStatus;
	private EditingViewer mViewer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scene_editor);
		mStateMachine = new SceneEditMachine();
		mStateMachine.attachClient(this);
		mDbHelper = new DataBaseHelper(this);
		
		Intent intent = getIntent();
		long sceneId = intent.getLongExtra(argSceneID, -1);
		String sceneName = intent.getStringExtra(argSceneName);
		mScene = new SceneModel(sceneId, sceneName);
		mDbHelper.loadScene(mScene);
		
		mViewer = (EditingViewer)findViewById(R.id.editorViewer);
		mViewer.setScene(mScene);
		mStateMachine.attachClient(mViewer);
		
		mTextStatus = (TextView)findViewById(R.id.editorLblStatus);
		mTextStatus.setText(mStateMachine.getStateAsString());
		
		mBtnPlanet = (Button)findViewById(R.id.editorBtnPlanet);
		mBtnTarget = (Button)findViewById(R.id.editorBtnTarget);
		mBtnLauncher = (Button)findViewById(R.id.editorBtnLauncher);
		mBtnCancel = (Button)findViewById(R.id.editorBtnCancel);
		mBtnApply = (Button)findViewById(R.id.editorBtnApply);
		bindButtonsWithActions();
	}

    @Override
    protected void onDestroy() {
        super.onStop();
        mDbHelper.storeScene(mScene);
    }

    @Override
    public void onSaveInstanceState(Bundle data) {
        mStateMachine.saveToBundle(data, "mStateMachine.");
    }

    @Override
    public void onRestoreInstanceState(Bundle data) {
        mStateMachine.loadFromBundle(data, "mStateMachine.");
        mStateMachine.notifyEverybodyAgain();
    }

	// Связывает клики по кнопкам с обработчиками
	private void bindButtonsWithActions() {
		mBtnPlanet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { onPlanetEditing(); }
		});
		mBtnTarget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { onTargetEditing(); }
		});
		mBtnLauncher.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { onLauncherEditing(); }
		});
		mBtnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { onCancelEditing(); }
		});
		mBtnApply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { onApply(); }
		});
	}
	
	private void onPlanetEditing() {
		mStateMachine.setState(SceneEditMachine.stateEditPlanet);
	}
	
	private void onTargetEditing() {
		mStateMachine.setState(SceneEditMachine.stateEditTarget);
	}
	
	private void onLauncherEditing() {
		mStateMachine.setState(SceneEditMachine.stateEditLauncher);
	}
	
	private void onCancelEditing() {
		mStateMachine.setState(SceneEditMachine.stateStart);
	}
	
	private void onApply() {
		mStateMachine.setState(SceneEditMachine.stateApply);
        mStateMachine.reset();
	}
	
	@Override // StateMachineClient
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scene_editor, menu);
		return true;
	}
	
	@Override // StateMachineClient
	public boolean onAttaching(AbstractStateMachine machine)
	{
		// Подключаемся только к созданному в активити автомату
		return machine == mStateMachine;
	}
	
	@Override // StateMachineClient
	public void onDeattached(AbstractStateMachine machine)
	{
		mStateMachine = null;
	}

    private void onPlanetApply(PlanetEditMachine machine) {
        ModelPlanet planet = machine.getPlanet();
        mScene.addPlanet(planet);
    }

    private void onTargetApply(TargetEditMachine machine) {
        ModelTarget target = machine.getTarget();
        mScene.addTarget(target);
    }

	@Override
	public void onStateChanged(long oldState, long newState,
                               AbstractStateMachine machine)
	{
		// При изменении состояния автомата, нужно обновлять текст с состоянием,
		// а так же блокировать/разблокировать кнопки
		mTextStatus.setText(mStateMachine.getStateAsString());

        if(newState == SceneEditMachine.stateApply) {
            if (oldState == SceneEditMachine.stateEditPlanet)
                onPlanetApply(mStateMachine.getPlanetMachine());
            else if(oldState == SceneEditMachine.stateEditTarget)
                onTargetApply(mStateMachine.getTargetMachine());
        }
	}
}
