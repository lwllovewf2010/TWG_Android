package com.modusgo.ubi.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.modusgo.ubi.Driver;
import com.modusgo.ubi.Trip;
import com.modusgo.ubi.db.TripContract.TripEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;

public class DbHelper extends SQLiteOpenHelper {
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INTEGER";
	private static final String FLOAT_TYPE = " REAL";
	
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
	    "CREATE TABLE " + VehicleEntry.TABLE_NAME + " (" +
	    VehicleEntry._ID + " INTEGER PRIMARY KEY," +
	    VehicleEntry.COLUMN_NAME_DRIVER_NAME + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_DRIVER_MARKER_ICON + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_DRIVER_PHOTO + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_VIN + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_MAKE + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_MODEL + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_YEAR + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_FUEL + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_CHECKUP + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LONGITUDE + FLOAT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LAST_TRIP_ID + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_SCORE + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_GRADE + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_TOTAL_TRIPS_COUNT + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_TOTAL_DRIVING_TIME + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_TOTAL_DISTANCE + FLOAT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_TOTAL_BREAKING + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_TOTAL_ACCELERATION + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING_DISTANCE + FLOAT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_ALERTS + INT_TYPE + " ); ";
	
	private static final String SQL_CREATE_ENTRIES_2 =
		    "CREATE TABLE " + TripEntry.TABLE_NAME + " (" +
		    TripEntry._ID + " INTEGER PRIMARY KEY," +
		    TripEntry.COLUMN_NAME_EVENTS_COUNT + INT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_START_TIME + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_END_TIME + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_DISTANCE + FLOAT_TYPE + " ); ";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES_2 = "DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME;
	
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 2;
	public static final String DATABASE_NAME = "ubi.db";
	
	private static DbHelper sInstance;
	
	public static DbHelper getInstance(Context context) {
		// Use the application context, which will ensure that you 
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new DbHelper(context.getApplicationContext());
		}
		return sInstance;
	}
	
	public DbHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	public void onCreate(SQLiteDatabase db) {
		//One sql create request for each table
	    db.execSQL(SQL_CREATE_ENTRIES);
	    db.execSQL(SQL_CREATE_ENTRIES_2);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    // This database is only a cache for online data, so its upgrade policy is
	    // to simply to discard the data and start over
	    db.execSQL(SQL_DELETE_ENTRIES);
	    db.execSQL(SQL_CREATE_ENTRIES);
	    db.execSQL(SQL_DELETE_ENTRIES_2);
	    db.execSQL(SQL_CREATE_ENTRIES_2);
	}
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    onUpgrade(db, oldVersion, newVersion);
	}
	
	public Driver getDriverShort(long id){
		SQLiteDatabase db = sInstance.getReadableDatabase();
		Cursor c = db.query(VehicleEntry.TABLE_NAME, 
				new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO}, 
				VehicleEntry._ID+" = ?", new String[]{Long.toString(id)}, null, null, null);
		
		Driver d = new Driver();
		
		if(c.moveToFirst()){
			d.id = c.getLong(0);
			d.name = c.getString(1);
			d.photo = c.getString(2);
		}
		c.close();
		db.close();
		return d;
	}
	
	public void saveDrivers(ArrayList<Driver> drivers){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && drivers!=null){
			String sql = "INSERT OR REPLACE INTO "+ VehicleEntry.TABLE_NAME +" ("
					+ VehicleEntry._ID +","
					+ VehicleEntry.COLUMN_NAME_DRIVER_NAME +","
					+ VehicleEntry.COLUMN_NAME_DRIVER_MARKER_ICON +","
					+ VehicleEntry.COLUMN_NAME_DRIVER_PHOTO +","
					+ VehicleEntry.COLUMN_NAME_CAR_VIN +","
					+ VehicleEntry.COLUMN_NAME_CAR_MAKE +","
					+ VehicleEntry.COLUMN_NAME_CAR_MODEL +","
					+ VehicleEntry.COLUMN_NAME_CAR_YEAR +","
					+ VehicleEntry.COLUMN_NAME_CAR_FUEL +","
					+ VehicleEntry.COLUMN_NAME_CAR_CHECKUP +","
					+ VehicleEntry.COLUMN_NAME_LATITUDE +","
					+ VehicleEntry.COLUMN_NAME_LONGITUDE +","
					+ VehicleEntry.COLUMN_NAME_ADDRESS +","
					+ VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE +","
					+ VehicleEntry.COLUMN_NAME_LAST_TRIP_ID +","
					+ VehicleEntry.COLUMN_NAME_SCORE +","
					+ VehicleEntry.COLUMN_NAME_GRADE +","
					+ VehicleEntry.COLUMN_NAME_TOTAL_TRIPS_COUNT +","
					+ VehicleEntry.COLUMN_NAME_TOTAL_DRIVING_TIME +","
					+ VehicleEntry.COLUMN_NAME_TOTAL_DISTANCE +","
					+ VehicleEntry.COLUMN_NAME_TOTAL_BREAKING +","
					+ VehicleEntry.COLUMN_NAME_TOTAL_ACCELERATION +","
					+ VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING +","
					+ VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING_DISTANCE +","
					+ VehicleEntry.COLUMN_NAME_ALERTS +""
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    String ids = "";
		    for (int i = 0; i < drivers.size(); i++) {
		    	Driver driver = drivers.get(i);
		    	
		    	ids+= i!=drivers.size()-1 ? driver.id+"," : driver.id;
		    	
		    	statement.clearBindings();
		    	statement.bindLong(1, driver.id);
		    	statement.bindString(2, driver.name);
		    	statement.bindString(3, driver.markerIcon);
		    	statement.bindString(4, driver.photo);
		    	statement.bindString(5, driver.carVIN);
		    	statement.bindString(6, driver.carMake);
		    	statement.bindString(7, driver.carModel);
		    	statement.bindString(8, driver.carYear);
		    	statement.bindLong(9, driver.carFuelLevel);
		    	statement.bindLong(10, driver.carCheckup ? 1 : 0);
		    	statement.bindDouble(11, driver.latitude);
		    	statement.bindDouble(12, driver.longitude);
		    	statement.bindString(13, driver.address);
		    	statement.bindString(14, driver.lastTripDate);
		    	statement.bindLong(15, driver.lastTripId);
		    	statement.bindLong(16, driver.score);
		    	statement.bindString(17, driver.grade);
		    	statement.bindLong(18, driver.totalTripsCount);
		    	statement.bindLong(19, driver.totalDrivingTime);
		    	statement.bindDouble(20, driver.totalDistance);
		    	statement.bindLong(21, driver.totalBraking);
		    	statement.bindLong(22, driver.totalAcceleration);
		    	statement.bindLong(23, driver.totalSpeeding);
		    	statement.bindDouble(24, driver.totalSpeedingDistance);
		    	statement.bindLong(25, driver.alerts);
		    	statement.execute();
		    	
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();

		    SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+VehicleEntry.TABLE_NAME+" WHERE "+VehicleEntry._ID+" NOT IN (" + ids + ")");
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
		}
		
		database.close();
		
	}
	
	public void saveTrips(ArrayList<Trip> trips){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && trips!=null){
			String sql = "INSERT OR REPLACE INTO "+ TripEntry.TABLE_NAME +" ("
					+ TripEntry._ID +","
					+ TripEntry.COLUMN_NAME_EVENTS_COUNT +","
					+ TripEntry.COLUMN_NAME_START_TIME +","
					+ TripEntry.COLUMN_NAME_END_TIME +","
					+ TripEntry.COLUMN_NAME_DISTANCE +""
					+ ") VALUES (?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
//		    String ids = "";
		    for (int i = 0; i < trips.size(); i++) {
		    	Trip trip = trips.get(i);
//		    	ids+= i!=trips.size()-1 ? trip.id+"," : trip.id;
		    	statement.clearBindings();
		    	statement.bindLong(1, trip.id);
		    	statement.bindLong(2, trip.eventsCount);
		    	statement.bindString(3, trip.startDate);
		    	statement.bindString(4, trip.endDate);
		    	statement.bindDouble(5, trip.distance);
		    	statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();

//		    SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+TripEntry.TABLE_NAME+" WHERE "+TripEntry._ID+" NOT IN (" + ids + ")");
//		    database.beginTransaction();
//		    removeStatement.clearBindings();
//	        removeStatement.execute();
//	        database.setTransactionSuccessful();	
//		    database.endTransaction();
//		    removeStatement.close();
		}
		
		database.close();
		
	}
	
}
