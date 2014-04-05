package com.example.gravity2d.Activities.SceneListClasses;

import java.util.Vector;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.gravity2d.Database.*;
import com.example.gravity2d.ModelObjects.SceneModel;

/**
 * Адаптер для списка сцен
 * @author ZiminAS
 * @version 1.0
 */
public class ScenesListAdapter extends BaseAdapter {

	private DataBaseHelper mData;
	private Vector<SceneModel> mScenes;
	private Context mContext;
	
	/**
	 * Производит обновление из базы даных
	 */
	public void updateData() {
		mScenes = mData.getScenesList();
		notifyDataSetChanged();
	}
	
	public ScenesListAdapter(Context context, DataBaseHelper dbHelper) {
		super();
		mData = dbHelper;
		mContext = context;
		updateData();
	}
	
	@Override
	public int getCount() {
		return mScenes.size();
	}

	@Override
	public Object getItem(int position) {
		return mScenes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mScenes.get(position).getSceneId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView != null) {
			TextView viewer = (TextView)convertView;
			viewer.setText(mScenes.get(position).getName());
			return viewer;
		}
		
		TextView viewer = new TextView(mContext);
		viewer.setBackgroundColor(Color.WHITE);
		viewer.setMinimumHeight(50);
		viewer.setText(mScenes.get(position).getName());
		return viewer;
	}

}
