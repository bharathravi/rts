package com.example.attendeasyserver;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;

public class Database extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "students";
    
    // TODO
	private String createTableCommand = "CREATE TABLE " + TABLE_NAME + " ";
    
	public Database(Context context) {
		super(context, TABLE_NAME, null, DATABASE_VERSION);		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createTableCommand);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub		
	}
	
	public boolean exists(String imei) {
		
		SQLiteDatabase db = getReadableDatabase();
		// TODO Query to check if this imei exists
		
		return false;
	}
	
	public void insertNew(String imei, String gtID) {
		SQLiteDatabase db = getWritableDatabase();
		// TODO Query to insert a new IMEI
	}
	
	public void recordAttendance(String imei) {
		SQLiteDatabase db = getWritableDatabase();
		// TODO Query to insert a new timestamp
	}

}
