package com.example.gravity2d.Activities;

import com.example.gravity2d.Activities.Common.AbstractStateMachine;
import com.example.gravity2d.Activities.Common.StateMachineClient;
import com.example.gravity2d.Activities.SceneListClasses.SceneListMachine;
import com.example.gravity2d.Activities.SceneListClasses.ScenesListAdapter;

import com.example.gravity2d.R;
import com.example.gravity2d.Database.DataBaseHelper;
import com.example.gravity2d.ModelObjects.SceneModel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ScenesListActivity extends Activity
                                implements StateMachineClient {
	
	private SceneListMachine mStateMachine;
	private DataBaseHelper mDbHelper;
	private ScenesListAdapter mScenesAdapter;
	
	private View mPreviousSelectedView;
	
	private Button mBtnCreate;
	private Button mBtnPlay;
	private Button mBtnEdit;
	private Button mBtnDelete;
	
	private ListView mScenesList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new DataBaseHelper(this);
		mScenesAdapter = new ScenesListAdapter(this, mDbHelper);
		mStateMachine = new SceneListMachine();
		mStateMachine.attachClient(this);
		createInterface();
	}
	
	@Override //StateMachineClient
    public boolean onAttaching(AbstractStateMachine machine) {
    	return mStateMachine == machine;
    }
	
	@Override //StateMachineClient
	public void onDeattached(AbstractStateMachine machine) {
		mStateMachine = null;
	}
	
	@Override //StateMachineClient
	public void onStateChanged(long oldState, long newState,
			                   AbstractStateMachine machine) {
		if(machine.getType() == SceneListMachine.MACHINE_TYPE_ID) {
			//SceneListMachine sceneMachine = (SceneListMachine)machine;

			if(oldState == SceneListMachine.stateOnElementSelected)
				onSceneDeselected();

			if(newState == SceneListMachine.stateOnElementSelected)
				onSceneSelected(mStateMachine.getSelectedScene());
            else if(newState == SceneListMachine.stateOnLaunch)
                onSceneLaunch(mStateMachine.getLaunchiedScene());
			else if(newState == SceneListMachine.stateOnEditing)
				onSceneEditing(mStateMachine.getEditingScene());
			else if(newState == SceneListMachine.stateOnNewSceneCreating)
				onSceneCreating();
            else if(newState == SceneListMachine.stateOnRemove)
                onSceneDeleting(mStateMachine.getDeletingScene());
		}
	}
	
	private void onSceneSelected(SceneModel scene) {
		if(scene == null)
			return;
		
		mBtnEdit.setEnabled(true);
		mBtnPlay.setEnabled(true);
		mBtnDelete.setEnabled(true);
	}
	
	private void onSceneDeselected() {
		mBtnEdit.setEnabled(false);
		mBtnPlay.setEnabled(false);
		mBtnDelete.setEnabled(false);
	}

    private void onSceneLaunch(SceneModel scene) {
        if(scene == null)
            return;
        Intent intent = new Intent(this, PlayingActivity.class);
        intent.putExtra(PlayingActivity.argSceneID, scene.getSceneId());
        intent.putExtra(PlayingActivity.argSceneName, scene.getName());
        startActivity(intent);
    }

	private void onSceneEditing(SceneModel scene) {
		if(scene == null)
			return;
		Intent intent = new Intent(this, SceneEditorActivity.class);
		intent.putExtra(SceneEditorActivity.argSceneID, scene.getSceneId());
		intent.putExtra(SceneEditorActivity.argSceneName, scene.getName());
		startActivity(intent);
	}

	private void onSceneCreating() {
		SceneModel scene = mDbHelper.createNewScene("New scene");
		scene.setName("Scene №" + scene.getSceneId());
		mDbHelper.storeScene(scene);
		mScenesAdapter.updateData();
		//mScenesList.invalidateViews();
	}

    private void  onSceneDeleting(SceneModel scene) {
        if(scene == null)
            return;
        mDbHelper.deleteScene(scene);
        mScenesAdapter.updateData();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scenes_list, menu);
		return true;
	}

	
	private ListView createScenesListView() {
		ListView scenesList = new ListView(this);
		OnItemClickListener scenesClickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(mPreviousSelectedView != null)
					mPreviousSelectedView.setBackgroundColor(Color.WHITE);
				if(mPreviousSelectedView == view) {
					view.setBackgroundColor(Color.WHITE);
					mPreviousSelectedView = null;
					mStateMachine.onItemDeselected();
				} else {
					view.setBackgroundColor(Color.rgb(232, 242, 254));
					mPreviousSelectedView = view;
					SceneModel scene = (SceneModel)mScenesAdapter.getItem(position);
					mStateMachine.onItemSelected(scene);
				}
			}
		};
		scenesList.setOnItemClickListener(scenesClickListener);
		return scenesList;
	}
	
	// Мега-функция. Пусть лежит в конце файла, чтобы не мусолила глаза
	private void createInterface() {
		LinearLayout ltMain = new LinearLayout(this);
		ltMain.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout ltButtons = new LinearLayout(this);
		ltButtons.setOrientation(LinearLayout.HORIZONTAL);
		
		OnClickListener buttonsListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v == mBtnPlay)
                    mStateMachine.onSceneLaunch(mStateMachine.getSelectedScene());
                else if(v == mBtnEdit)
					mStateMachine.onSceneEdit(mStateMachine.getSelectedScene());
				else if(v == mBtnCreate)
					mStateMachine.onNewSceneCreating();
                else if(v == mBtnDelete)
                    mStateMachine.onSceneDelete(mStateMachine.getSelectedScene());
			}
		};
		
		mBtnCreate = new Button(this);
		mBtnCreate.setText("New");
		mBtnCreate.setOnClickListener(buttonsListener);
		ltButtons.addView(mBtnCreate);
		
		mBtnPlay = new Button(this);
		mBtnPlay.setText("Play");
		mBtnPlay.setEnabled(false);
        mBtnPlay.setOnClickListener(buttonsListener);
		ltButtons.addView(mBtnPlay);
		
		mBtnEdit = new Button(this);
		mBtnEdit.setText("Edit");
		mBtnEdit.setEnabled(false);
		mBtnEdit.setOnClickListener(buttonsListener);
		ltButtons.addView(mBtnEdit);
		
		mBtnDelete = new Button(this);
		mBtnDelete.setText("Delete");
		mBtnDelete.setEnabled(false);
        mBtnDelete.setOnClickListener(buttonsListener);
		ltButtons.addView(mBtnDelete);
		
		ltMain.addView(ltButtons);
		
		mScenesList = createScenesListView();
		mScenesList.setAdapter(mScenesAdapter);
		ltMain.addView(mScenesList);
		
		setContentView(ltMain);
		
		mPreviousSelectedView = null;
	}
}
