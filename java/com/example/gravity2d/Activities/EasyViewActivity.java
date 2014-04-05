package com.example.gravity2d.Activities;

import com.example.gravity2d.R;
import com.example.gravity2d.Activities.Common.*;
import com.example.gravity2d.Database.DataBaseHelper;
import com.example.gravity2d.ModelObjects.SceneModel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class EasyViewActivity extends Activity {

	SceneView mViewer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("", "On EasyViewActivity creating");
		setContentView(R.layout.activity_easy_view);
		mViewer = (SceneView)findViewById(R.id.viewer);
		Intent intent = getIntent();
		long sceneId = intent.getLongExtra("SCENE_ID", -1);
		String sceneName = "";	// TODO: получать из intent'а
		if(sceneId != -1) {
			DataBaseHelper dbHelper = new DataBaseHelper(this);
			SceneModel model = new SceneModel(sceneId, sceneName);
			if(dbHelper.loadScene(model))
				mViewer.setScene(model);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.easy_view, menu);
		return true;
	}

}
