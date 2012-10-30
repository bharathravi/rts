package com.example.attendeasyserver;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Pair;

public class Database extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "attendeasy";
    private static final String STUDENTS_TABLE_NAME = "students";
    private static final String ATTENDANCE_TABLE_NAME = "attendance";
    private static final String CLASSES_TABLE_NAME = "classes";
  
    
    // TODO
    private String classIdColumn = "class_id";
    private String classNameColumn = "className";
    
    private String imeiColumn = "imei";
    private String gtidColumn = "gtid";
    private String timeColumn = "time";
	
    private String createClassesTableCommand = "CREATE TABLE " + CLASSES_TABLE_NAME + 
			"(" + classIdColumn + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ classNameColumn + " varchar(100))";
	    
    private String createStudentTableCommand = "CREATE TABLE " + STUDENTS_TABLE_NAME + 
			"(" + classIdColumn + " INTEGER NOT NULL, " 
    		+ imeiColumn + " varchar(50) NOT NULL, "
			+ gtidColumn + " varchar(50) NOT NULL, "
			+ "PRIMARY KEY (" + classIdColumn + ", " + imeiColumn + "),"
			+ "FOREIGN KEY(" + classIdColumn+ ") REFERENCES " + CLASSES_TABLE_NAME + "(" +  classIdColumn + ") ON DELETE CASCADE ON UPDATE CASCADE)";
	
	private String createAttendanceTableCommand = "CREATE TABLE " + ATTENDANCE_TABLE_NAME + 
			"(" + classIdColumn + " INTEGER NOT NULL,"  
			+ imeiColumn + " varchar(50) NOT NULL, "
			+ timeColumn + " date NOT NULL, "
			+ "PRIMARY KEY (" +  classIdColumn + "," + imeiColumn + ", " + timeColumn +  "),"
			+ "FOREIGN KEY(" + classIdColumn + ") REFERENCES "  + CLASSES_TABLE_NAME + "(" +  classIdColumn + ") ON DELETE CASCADE ON UPDATE CASCADE)"; 
    
	public Database(Context context) {		
		super(context, DATABASE_NAME, null, DATABASE_VERSION);		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createClassesTableCommand);
		db.execSQL(createStudentTableCommand);
		db.execSQL(createAttendanceTableCommand);
				
		ContentValues values = new ContentValues();	
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
	    super.onOpen(db);
	    if (!db.isReadOnly()) {
	        // Enable foreign key constraints
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub		
	}
	
	public List<Pair<Integer, String>> getClassList() {
		List<Pair<Integer, String>> list = new ArrayList<Pair<Integer,String>>();
		SQLiteDatabase db = getReadableDatabase();	
		Cursor cursor = db.rawQuery("SELECT * FROM " + CLASSES_TABLE_NAME, null);
	
		cursor.moveToFirst();
		int classIdIndex = cursor.getColumnIndex(classIdColumn);
		int classNameIndex = cursor.getColumnIndex(classNameColumn);
		
		if (cursor.getCount() > 0) {				
		  do {
			  Integer classId = cursor.getInt(classIdIndex);
			  String className = cursor.getString(classNameIndex);
			
			  list.add(new Pair<Integer, String>(classId, className));
		  } while (cursor.moveToNext());
		}
		
		cursor.close();
		return list;
	}
	
	public boolean exists(String imei, int classId) {		
		SQLiteDatabase db = getReadableDatabase();		
	
		Cursor cursor = db.rawQuery("SELECT * FROM " + STUDENTS_TABLE_NAME 
				+ " WHERE " + imeiColumn + " LIKE '" + imei + "' " +
				" and " + classIdColumn + "=" + classId, null);

	    if (!cursor.moveToFirst()) {
	    	cursor.close();
	    	return false;
	    }
	    
	    int value = cursor.getCount();
	    cursor.close();
		if (value == 1) {						
			return true;			
		} else {
			return false;
		}
	}
	
	public long insertNewStudent(String imei, String gtid, int classId) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(imeiColumn, imei);
		values.put(gtidColumn, gtid);
		values.put(classIdColumn, classId);
		
		long value = db.insert(STUDENTS_TABLE_NAME, null, values);		
		
		if (value > -1) { 
				
		  ContentValues values2 = new ContentValues();
		  Time now = new Time();
		  now.setToNow();
		  values2.put(imeiColumn, imei);
		  values2.put(timeColumn, now.toMillis(false));
		  values2.put(classIdColumn, classId);
		
		  value = db.insert(ATTENDANCE_TABLE_NAME, null, values2);
		}
		
		return value;
	}
	
	public void recordAttendance(String imei, int classId) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values2 = new ContentValues();
		Time now = new Time();
		now.setToNow();
		values2.put(imeiColumn, imei);
		values2.put(timeColumn, now.toMillis(false));
		values2.put(classIdColumn, classId);
		
		db.insert(ATTENDANCE_TABLE_NAME, null, values2);
	}

	public void addNewClass(String classname) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(classNameColumn, classname);
		db.insert(CLASSES_TABLE_NAME, null, values);		
	}
	
	public void deleteClass(Integer classId) {
		SQLiteDatabase db = getWritableDatabase();		
		db.delete(CLASSES_TABLE_NAME, classIdColumn + "=?", new String[]{classId.toString()});
	}
	
	public String getClassName(Integer classId) {
		SQLiteDatabase db = getReadableDatabase();		
		Cursor cursor = db.rawQuery("SELECT * FROM " + CLASSES_TABLE_NAME 
				+ " WHERE " + classIdColumn + "=" + classId, null);
		
		int classNameColId = cursor.getColumnIndex(classNameColumn);
		
		cursor.moveToFirst();
		
		String returner = "";
		if (cursor.getCount() == 1) {
			 returner = cursor.getString(classNameColId);
		}
		
		cursor.close();
		return returner;
	}

	public Time getMostRecentAttendance(String imei, Integer classId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + ATTENDANCE_TABLE_NAME + " WHERE " + imeiColumn + " LIKE '" + imei + "' " +
				" and " + classIdColumn + "=" + classId +  " ORDER BY " + timeColumn + " DESC", null);
		
		Time time = null;
		if (cursor.moveToFirst()) {
			int timeColumnId = cursor.getColumnIndex(timeColumn);
			long timeValue =  cursor.getLong(timeColumnId);
			time = new Time();
			time.set(timeValue);
			
		}
		cursor.close();
		return time;
	}

	public int getAttendance(String imei, int classId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + ATTENDANCE_TABLE_NAME + " WHERE " + imeiColumn + " LIKE '" + imei + "' " +
				" and " + classIdColumn + "=" + classId, null);
		
		int count = 0;
		if (cursor.moveToFirst()) {
			count = cursor.getCount();
		}
		
		cursor.close();
		return count;
	}
}
