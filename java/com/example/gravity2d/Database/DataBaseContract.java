package com.example.gravity2d.Database;

import android.provider.BaseColumns;

/**
 * Класс, описывающий внешнюю БД
 * @author ZiminAS
 * @version 1.0
 */
public class DataBaseContract {
	public static int VERSION = 1;
	public static String DATABASE_NAME = "Gravity2D.db";
	public static String TYPE_TEXT = " TEXT";
	public static String TYPE_REAL = " REAL";
	public static String TYPE_INT = " INTEGER";
	
	DataBaseContract() {}
	
	// Сцена имеет название. Кроме того, так как точка запуска объекта
	// одна на всю сцену, было решено, что не целесообразно создавать для
	// точек запуска отдельную таблицу, поэтому они хранятся тут же
	public static class ScenesTable implements BaseColumns {
		public static String TABLE_NAME = "scenes";
		public static String COL_NAME = "name";
		public static String COL_LAUNCH_X = "launch_x";
		public static String COL_LAUNCH_Y = "launch_y";
		
		public static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
				"(" + _ID + TYPE_INT + " PRIMARY KEY, " +
				COL_NAME + TYPE_TEXT + ", " +
				COL_LAUNCH_X + TYPE_REAL + ", " + 
				COL_LAUNCH_Y + TYPE_REAL + ");";
		
		public static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
	public static class PlanetsTable implements BaseColumns {
		public static String TABLE_NAME = "planets";
		// Идентификатор сцены, которой принадлежит планета
		public static String COL_SCENE_ID = "scene_id";
		public static String COL_X = "x";
		public static String COL_Y = "y";
		public static String COL_RADIUS = "radius";
		public static String COL_WEIGHT = "weight";
		
		public static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
				"(" + _ID + TYPE_INT + " PRIMARY KEY, " +
				COL_SCENE_ID + TYPE_INT + ", " +
				COL_X + TYPE_REAL + ", " +
				COL_Y + TYPE_REAL + ", " +
				COL_RADIUS + TYPE_REAL + ", " +
				COL_WEIGHT + TYPE_REAL + ");";
		
		public static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
	public static class TargetsTable implements BaseColumns {
		public static String TABLE_NAME = "targets";
		// Идентификатор сцены, которой принадлежит мишень
		public static String COL_SCENE_ID = "scene_id";
		public static String COL_FIRST_X = "first_x";
		public static String COL_FIRST_Y = "first_y";
		public static String COL_SECOND_X = "second_x";
		public static String COL_SECOND_Y = "second_y";
		
		public static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
				"(" + _ID + TYPE_INT + " PRIMARY KEY, " +
				COL_SCENE_ID + TYPE_INT + ", " +
				COL_FIRST_X + TYPE_REAL + ", " +
				COL_FIRST_Y + TYPE_REAL + ", " +
				COL_SECOND_X + TYPE_REAL + ", " +
				COL_SECOND_Y + TYPE_REAL + ");";
		
		public static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
}
