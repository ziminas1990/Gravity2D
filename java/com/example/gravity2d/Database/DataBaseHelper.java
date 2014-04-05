package com.example.gravity2d.Database;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.example.gravity2d.ModelObjects.SceneModel;
import com.example.gravity2d.ModelObjects.ModelPlanet;
import com.example.gravity2d.ModelObjects.ModelTarget;
import com.example.gravity2d.PhxEngine.Coordinate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View.OnCreateContextMenuListener;

public class DataBaseHelper extends SQLiteOpenHelper {
	public DataBaseHelper(Context context) {
		super(context, DataBaseContract.DATABASE_NAME,
		      null, DataBaseContract.VERSION);
	}
	
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DataBaseContract.ScenesTable.CREATE_TABLE);
		db.execSQL(DataBaseContract.PlanetsTable.CREATE_TABLE);
		db.execSQL(DataBaseContract.TargetsTable.CREATE_TABLE);
	}
	
	public void dropDatabase(SQLiteDatabase db) {
		db.execSQL(DataBaseContract.ScenesTable.DROP_TABLE);
		db.execSQL(DataBaseContract.PlanetsTable.DROP_TABLE);
		db.execSQL(DataBaseContract.TargetsTable.DROP_TABLE);
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Пока была только одна версия БД, так что функция очень проста:
		dropDatabase(db);
		onCreate(db);
	}
	
	/**
	 * Функция для получения из БД списка сцен. В дальнейшем сцены могут быть
	 * загружены через функцию loadScene()
	 * @return Возвращает список сцен.
	 */
	public Vector<SceneModel> getScenesList() {
		Vector<SceneModel> scenes = new Vector<SceneModel>();
		SQLiteDatabase db = getWritableDatabase();
		
		String columns[] = new String[2];
		columns[0] = DataBaseContract.ScenesTable._ID;
		columns[1] = DataBaseContract.ScenesTable.COL_NAME;
		
		Cursor result = db.query(DataBaseContract.ScenesTable.TABLE_NAME,
				                 columns, null, null, null, null, null);
		while(result.moveToNext())
			scenes.add(new SceneModel(result.getLong(0), result.getString(1)));
		
		return scenes;
	}
	
	/**
	 * Функция для загрузки сцены. Добавляет к сцене планеты, цели и точку
	 * запуска
	 * @param scene Загружаемая сцена. Объект содержит идентификатор сцены и её
	 * имя. Объект можно получить из функции getScenesList
	 * @return Возвращает true, если сцена была загружена успешно, и false
	 * в противном случае (некорректный идентификатор)
	 */
	public boolean loadScene(SceneModel scene) {
		if(scene.getSceneId() == -1)
			return false;
		
		Vector<ModelPlanet> planets = getScenePlanets(scene.getSceneId());
		Iterator<ModelPlanet> itPlanet = planets.iterator();
		while(itPlanet.hasNext())
			scene.addPlanet(itPlanet.next());
		
		Vector<ModelTarget> targets = getSceneTargets(scene.getSceneId());
		Iterator<ModelTarget> itTarget = targets.iterator();
		while(itTarget.hasNext())
			scene.addTarget(itTarget.next());
		
		scene.setLaunchPoint(getSceneLauncherPoint(scene.getSceneId()));
		return true;
	}
	
	/**
	 * Функция обеспечивает создание новой сцены. Это единственный корректный
	 * метод создания новых сцен
	 * @param sceneName Имя новой сцены
	 * @return Возвращает объект новой сцены
	 */
	public SceneModel createNewScene(String sceneName) {
		SQLiteDatabase db = getWritableDatabase();
		// TODO: Здесь нужно ловить исключение SQLiteException
		
		ContentValues values = new ContentValues();
		values.put(DataBaseContract.ScenesTable.COL_NAME, sceneName);
		values.put(DataBaseContract.ScenesTable.COL_LAUNCH_X, 0);
		values.put(DataBaseContract.ScenesTable.COL_LAUNCH_Y, 0);
		
		long sceneId = db.insert(DataBaseContract.ScenesTable.TABLE_NAME,
				                 null, values);

		return new SceneModel(sceneId, sceneName);
	}
	
	/**
	 * Функция для удаления сцены из базы данных
	 * @param scene Удаляемая сцена
	 * @return Возвращает true, если удаление сцены прошло успешно, либо false
	 * в противном случае
	 */
	public boolean deleteScene(SceneModel scene) {
		if(scene.getSceneId() == -1)
			return false;
		
		SQLiteDatabase db = getWritableDatabase();
		String[] clause_values =
				new String[] { String.valueOf(scene.getSceneId()) };
		db.delete(DataBaseContract.PlanetsTable.TABLE_NAME,
				  DataBaseContract.PlanetsTable.COL_SCENE_ID + "=?",
				  clause_values);
		db.delete(DataBaseContract.TargetsTable.TABLE_NAME,
				  DataBaseContract.TargetsTable.COL_SCENE_ID + "=?",
				  clause_values);
		db.delete(DataBaseContract.ScenesTable.TABLE_NAME,
				  DataBaseContract.ScenesTable._ID + "=?",
				  clause_values);
		return true;
	}
	
	/**
	 * Функция для сохранения сцены в БД
	 * @param scene Обновляемая сцена
	 * @return Возвращает true, если обновление сцены прошло успешно, и
	 * false в противном случае
	 */
	public boolean storeScene(SceneModel scene) {
		if(scene.getSceneId() == -1)
			return false;
		
		// TODO: возможно, стоит попытаться оптимизировать процесс
		if(!deleteScene(scene))
			return false;
		
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		
		ContentValues values = new ContentValues();
		values.put(DataBaseContract.ScenesTable._ID, scene.getSceneId());
		values.put(DataBaseContract.ScenesTable.COL_NAME, scene.getName());
		values.put(DataBaseContract.ScenesTable.COL_LAUNCH_X,
				   scene.getLaunchPoint().x());
		values.put(DataBaseContract.ScenesTable.COL_LAUNCH_Y,
				   scene.getLaunchPoint().y());
		long result = db.insert(DataBaseContract.ScenesTable.TABLE_NAME, null,
				                values);
		if(result == -1) {
			db.endTransaction();
			return false;
		}
		
		Set<ModelPlanet> planets = scene.getAllPlanets();
		Iterator<ModelPlanet> itPlanet = planets.iterator();
		while(itPlanet.hasNext()) {
			if(insertPlanet(itPlanet.next(), scene.getSceneId()) == false) {
				db.endTransaction();
				return false;
			}
		}
		
		Set<ModelTarget> targets = scene.getAllTargets();
		Iterator<ModelTarget> itTarget = targets.iterator();
		while(itTarget.hasNext()) {
			if(insertTarget(itTarget.next(), scene.getSceneId()) == false) {
				db.endTransaction();
				return false;
			}
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
		return true;
	}
	
	/**
	 * Загружает точку запуска для сцены
	 * @param sceneId Идентификатор сцены
	 * @return Координаты точки запуска
	 */
	private Coordinate getSceneLauncherPoint(long sceneId) {
		SQLiteDatabase db = getWritableDatabase();
		
		String columns[] = new String[2];
		columns[0] = DataBaseContract.ScenesTable.COL_LAUNCH_X;
		columns[1] = DataBaseContract.ScenesTable.COL_LAUNCH_Y;
		
		Cursor result =
				db.query(DataBaseContract.ScenesTable.TABLE_NAME, columns,
						 DataBaseContract.ScenesTable._ID + "="
				         + String.valueOf(sceneId),
				         null, null, null, null);

		if(!result.moveToPosition(0))
			return new Coordinate(0, 0);
		return new Coordinate(result.getDouble(0), result.getDouble(1));
	}

	/**
	 * Функция добавляет к сцене планету
	 * @param planet Описание планеты
	 * @param sceneId Идентификатор сцены, которой принадлежит планета
	 * @return Возвращает true, если планета добавлена, и false в противном
	 * случае
	 */
	private boolean insertPlanet(ModelPlanet planet, long sceneId) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(DataBaseContract.PlanetsTable.COL_SCENE_ID, sceneId);
		values.put(DataBaseContract.PlanetsTable.COL_X, planet.Position().x());
		values.put(DataBaseContract.PlanetsTable.COL_Y, planet.Position().y());
		values.put(DataBaseContract.PlanetsTable.COL_RADIUS, planet.Radius());
		values.put(DataBaseContract.PlanetsTable.COL_WEIGHT, planet.Weight());
		
		long planetId = db.insert(DataBaseContract.PlanetsTable.TABLE_NAME,
				                  null, values);
		
		return planetId != -1;
	}
	
	/**
	 * Функция для получения списка планет, входящих в состав сцены
	 * @param sceneId Идентификатор сцены
	 * @return Возвращает список планет сцены
	 */
	private Vector<ModelPlanet> getScenePlanets(long sceneId) {
		Vector<ModelPlanet> allPlanets = new Vector<ModelPlanet>();
		
		SQLiteDatabase db = getReadableDatabase();
		
		String columns[] = new String[4];
		columns[0] = DataBaseContract.PlanetsTable.COL_X;
		columns[1] = DataBaseContract.PlanetsTable.COL_Y;
		columns[2] = DataBaseContract.PlanetsTable.COL_WEIGHT;
		columns[3] = DataBaseContract.PlanetsTable.COL_RADIUS;
		
		String selection = DataBaseContract.PlanetsTable.COL_SCENE_ID + "=" +
		                   String.valueOf(sceneId);
		
		Cursor result = db.query(DataBaseContract.PlanetsTable.TABLE_NAME,
				                 columns, selection, null, null, null, null);

        while(result.moveToNext()) {
			Coordinate position = new Coordinate(result.getDouble(0), result.getDouble(1));
			allPlanets.add(new ModelPlanet(result.getDouble(2), result.getDouble(3), position));
		}
		
		return allPlanets;
	}
	
	/**
	 * Функция для получения списка целей, входящих в состав сцены
	 * @param sceneId Идентификатор сцены
	 * @return Возвращает список целей сцены
	 */
	private Vector<ModelTarget> getSceneTargets(long sceneId) {
		Vector<ModelTarget> allTargets = new Vector<ModelTarget>();
		
		SQLiteDatabase db = getWritableDatabase();
		
		String columns[] = new String[4];
		columns[0] = DataBaseContract.TargetsTable.COL_FIRST_X;
		columns[1] = DataBaseContract.TargetsTable.COL_FIRST_Y;
		columns[2] = DataBaseContract.TargetsTable.COL_SECOND_X;
		columns[3] = DataBaseContract.TargetsTable.COL_SECOND_Y;
		
		String selection = DataBaseContract.TargetsTable.COL_SCENE_ID + "=" +
		                   String.valueOf(sceneId);
		
		Cursor result = db.query(DataBaseContract.TargetsTable.TABLE_NAME,
				                 columns, selection, null, null, null, null);
		
		while(result.moveToNext()) {
			Coordinate first = new Coordinate(result.getDouble(0),
					                          result.getDouble(1));
			Coordinate second = new Coordinate(result.getDouble(2),
                                               result.getDouble(3));
			allTargets.add(new ModelTarget(first, second));
		}
		
		return allTargets;
	}
	
	/**
	 * Функция добавляет к сцене цель
	 * @param target Добавляемая цель
	 * @param sceneId Идентификатор сцены, к которой добавляется цель
	 * @return Возвращает true, если цель была добавлена, и false в противном
	 * случае
	 */
	private boolean insertTarget(ModelTarget target, long sceneId) {
        SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(DataBaseContract.TargetsTable.COL_SCENE_ID, sceneId);
		values.put(DataBaseContract.TargetsTable.COL_FIRST_X,
				   target.FirstPoint().x());
		values.put(DataBaseContract.TargetsTable.COL_FIRST_Y,
				   target.FirstPoint().y());
		values.put(DataBaseContract.TargetsTable.COL_SECOND_X,
				   target.SecondPoint().x());
		values.put(DataBaseContract.TargetsTable.COL_SECOND_Y,
				   target.SecondPoint().y());
		
		long targetId = db.insert(DataBaseContract.TargetsTable.TABLE_NAME,
				                  null, values);
		
		return targetId != -1;
	}
}
