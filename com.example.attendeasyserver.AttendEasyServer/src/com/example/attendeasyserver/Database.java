package com.example.attendeasyserver;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorJoiner.Result;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;

public class Database extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "attendeasy";
    private static final String STUDENTS_TABLE_NAME = "students";
    private static final String ATTENDANCE_TABLE_NAME = "attendance";
    
    // TODO
    private String imeiColumn = "imei";
    private String gtidColumn = "gtid";
    private String timeColumn = "time";
	private String createStudentTableCommand = "CREATE TABLE " + STUDENTS_TABLE_NAME + 
			"(" + imeiColumn + " varchar(50), "
			+ gtidColumn + " varchar(50))";
	
	private String createAttendanceTableCommand = "CREATE TABLE " + ATTENDANCE_TABLE_NAME + 
			"(" + imeiColumn + " varchar(50), "
			+ timeColumn + " date)"; 
    
	public Database(Context context) {		
		super(context, DATABASE_NAME, null, DATABASE_VERSION);		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createStudentTableCommand);
		db.execSQL(createAttendanceTableCommand);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub		
	}
	
	public boolean exists(String imei) {
		
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = new String[1];
		columns[0] = imeiColumn;
		Cursor cursor = db.query(STUDENTS_TABLE_NAME, columns, imeiColumn + " like '" + imei  + "'", null, null, null, null);
		
		if (cursor.getCount() > 0) {
			return true;
		}
		
		return false;
	}
	
	public void insertNew(String imei, String gtid) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(imeiColumn, imei);
		values.put(gtidColumn, gtid);
		
		
		long value = db.insert(STUDENTS_TABLE_NAME, null, values);
		System.out.print(value);
				
		ContentValues values2 = new ContentValues();
		Time now = new Time();
		now.setToNow();
		values2.put(imeiColumn, imei);
		values2.put(timeColumn, now.toMillis(false));
		
		value = db.insert(ATTENDANCE_TABLE_NAME, null, values2);
		System.out.print(value);
	}
	
	public void recordAttendance(String imei) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values2 = new ContentValues();
		Time now = new Time();
		now.setToNow();
		values2.put(imeiColumn, imei);
		values2.put(timeColumn, now.toMillis(false));
		db.insert(ATTENDANCE_TABLE_NAME, null, values2);
	}

}
