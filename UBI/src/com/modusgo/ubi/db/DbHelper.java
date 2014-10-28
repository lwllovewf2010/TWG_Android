package com.modusgo.ubi.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.google.android.gms.maps.model.LatLng;
import com.modusgo.ubi.Driver;
import com.modusgo.ubi.ScoreCirclesActivity.CirclesSection;
import com.modusgo.ubi.ScoreFragment.MonthStats;
import com.modusgo.ubi.ScorePieChartActivity.PieChartTab;
import com.modusgo.ubi.Trip;
import com.modusgo.ubi.Trip.Event;
import com.modusgo.ubi.Trip.Point;
import com.modusgo.ubi.db.EventContract.EventEntry;
import com.modusgo.ubi.db.PointContract.PointEntry;
import com.modusgo.ubi.db.RouteContract.RouteEntry;
import com.modusgo.ubi.db.ScoreCirclesContract.ScoreCirclesEntry;
import com.modusgo.ubi.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.ubi.db.ScorePercentageContract.ScorePercentageEntry;
import com.modusgo.ubi.db.ScorePieChartContract.ScorePieChartEntry;
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
	    VehicleEntry.COLUMN_NAME_ALERTS + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_ODOMETER + INT_TYPE + " ); ";
	
	private static final String SQL_CREATE_ENTRIES_2 =
		    "CREATE TABLE " + TripEntry.TABLE_NAME + " (" +
		    TripEntry._ID + " INTEGER PRIMARY KEY," +
		    TripEntry.COLUMN_NAME_EVENTS_COUNT + INT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_START_TIME + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_END_TIME + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_DISTANCE + FLOAT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_AVG_SPEED + FLOAT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_MAX_SPEED + FLOAT_TYPE + " ); ";
	
	private static final String SQL_CREATE_ENTRIES_3 =
		    "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +
		    RouteEntry._ID + " INTEGER PRIMARY KEY," +
		    RouteEntry.COLUMN_NAME_TRIP_ID + INT_TYPE + COMMA_SEP +
		    RouteEntry.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
		    RouteEntry.COLUMN_NAME_LONGITUDE + FLOAT_TYPE +  " ); ";
	
	private static final String SQL_CREATE_ENTRIES_4 =
		    "CREATE TABLE " + PointEntry.TABLE_NAME + " (" +
		    PointEntry._ID + " INTEGER PRIMARY KEY," +
		    PointEntry.COLUMN_NAME_TRIP_ID + INT_TYPE + COMMA_SEP +
		    PointEntry.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
		    PointEntry.COLUMN_NAME_LONGITUDE + FLOAT_TYPE + COMMA_SEP +
		    PointEntry.COLUMN_NAME_EVENTS + TEXT_TYPE +  " ); ";
	
	private static final String SQL_CREATE_ENTRIES_5 =
		    "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +
		    EventEntry._ID + " INTEGER PRIMARY KEY," +
		    EventEntry.COLUMN_NAME_TRIP_ID + INT_TYPE + COMMA_SEP +
		    EventEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
		    EventEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
		    EventEntry.COLUMN_NAME_ADDRESS + TEXT_TYPE +  " ); ";
	
	private static final String SQL_CREATE_ENTRIES_6 =
		    "CREATE TABLE " + ScoreGraphEntry.TABLE_NAME + " (" +
		    ScoreGraphEntry._ID + " INTEGER PRIMARY KEY," +
		    ScoreGraphEntry.COLUMN_NAME_DRIVER_ID + INT_TYPE + COMMA_SEP +
		    ScoreGraphEntry.COLUMN_NAME_MONTH + INT_TYPE + COMMA_SEP +
		    ScoreGraphEntry.COLUMN_NAME_SCORE + INT_TYPE + COMMA_SEP +
		    ScoreGraphEntry.COLUMN_NAME_GRADE + TEXT_TYPE +  " ); ";
	
	private static final String SQL_CREATE_ENTRIES_7 =
		    "CREATE TABLE " + ScorePercentageEntry.TABLE_NAME + " (" +
		    ScorePercentageEntry._ID + " INTEGER PRIMARY KEY," +
		    ScorePercentageEntry.COLUMN_NAME_DRIVER_ID + INT_TYPE + COMMA_SEP +
		    ScorePercentageEntry.COLUMN_NAME_STAT_NAME + TEXT_TYPE + COMMA_SEP +
		    ScorePercentageEntry.COLUMN_NAME_STAT_VALUE + INT_TYPE +  " ); ";
	
	private static final String SQL_CREATE_ENTRIES_8 =
		    "CREATE TABLE " + ScorePieChartEntry.TABLE_NAME + " (" +
		    ScorePieChartEntry._ID + " INTEGER PRIMARY KEY," +
		    ScorePieChartEntry.COLUMN_NAME_DRIVER_ID + INT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_TAB + TEXT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_VALUE + FLOAT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_SUBTITLE + TEXT_TYPE + " ); ";
	
	private static final String SQL_CREATE_ENTRIES_9 =
		    "CREATE TABLE " + ScoreCirclesEntry.TABLE_NAME + " (" +
		    ScoreCirclesEntry._ID + " INTEGER PRIMARY KEY," +
		    ScoreCirclesEntry.COLUMN_NAME_DRIVER_ID + INT_TYPE + COMMA_SEP +
		    ScoreCirclesEntry.COLUMN_NAME_TAB + TEXT_TYPE + COMMA_SEP +
		    ScoreCirclesEntry.COLUMN_NAME_SECTION + TEXT_TYPE + COMMA_SEP +
		    ScoreCirclesEntry.COLUMN_NAME_MARK + INT_TYPE + COMMA_SEP +
		    ScoreCirclesEntry.COLUMN_NAME_DISTANCE + FLOAT_TYPE + " ); ";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES_2 = "DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES_3 = "DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES_4 = "DROP TABLE IF EXISTS " + PointEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES_5 = "DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES_6 = "DROP TABLE IF EXISTS " + ScoreGraphEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES_7 = "DROP TABLE IF EXISTS " + ScorePercentageEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES_8 = "DROP TABLE IF EXISTS " + ScorePieChartEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES_9 = "DROP TABLE IF EXISTS " + ScoreCirclesEntry.TABLE_NAME;
	
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 10;
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
	    db.execSQL(SQL_CREATE_ENTRIES_3);
	    db.execSQL(SQL_CREATE_ENTRIES_4);
	    db.execSQL(SQL_CREATE_ENTRIES_5);
	    db.execSQL(SQL_CREATE_ENTRIES_6);
	    db.execSQL(SQL_CREATE_ENTRIES_7);
	    db.execSQL(SQL_CREATE_ENTRIES_8);
	    db.execSQL(SQL_CREATE_ENTRIES_9);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    // This database is only a cache for online data, so its upgrade policy is
	    // to simply to discard the data and start over
	    db.execSQL(SQL_DELETE_ENTRIES);
	    db.execSQL(SQL_CREATE_ENTRIES);
	    db.execSQL(SQL_DELETE_ENTRIES_2);
	    db.execSQL(SQL_CREATE_ENTRIES_2);
	    db.execSQL(SQL_DELETE_ENTRIES_3);
	    db.execSQL(SQL_CREATE_ENTRIES_3);
	    db.execSQL(SQL_DELETE_ENTRIES_4);
	    db.execSQL(SQL_CREATE_ENTRIES_4);
	    db.execSQL(SQL_DELETE_ENTRIES_5);
	    db.execSQL(SQL_CREATE_ENTRIES_5);
	    db.execSQL(SQL_DELETE_ENTRIES_6);
	    db.execSQL(SQL_CREATE_ENTRIES_6);
	    db.execSQL(SQL_DELETE_ENTRIES_7);
	    db.execSQL(SQL_CREATE_ENTRIES_7);
	    db.execSQL(SQL_DELETE_ENTRIES_8);
	    db.execSQL(SQL_CREATE_ENTRIES_8);
	    db.execSQL(SQL_DELETE_ENTRIES_9);
	    db.execSQL(SQL_CREATE_ENTRIES_9);
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
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO,
				VehicleEntry.COLUMN_NAME_LATITUDE,
				VehicleEntry.COLUMN_NAME_LONGITUDE}, 
				VehicleEntry._ID+" = ?", new String[]{Long.toString(id)}, null, null, null);
		
		Driver d = new Driver();
		
		if(c.moveToFirst()){
			d.id = c.getLong(0);
			d.name = c.getString(1);
			d.photo = c.getString(2);
			d.latitude = c.getDouble(3);
			d.longitude = c.getDouble(4);
		}
		c.close();
		db.close();
		return d;
	}
	
	public Driver getDriver(long id){
		SQLiteDatabase db = sInstance.getReadableDatabase();
		Cursor c = db.query(VehicleEntry.TABLE_NAME, 
				new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_DRIVER_MARKER_ICON,
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO,
				VehicleEntry.COLUMN_NAME_CAR_MAKE,
				VehicleEntry.COLUMN_NAME_CAR_MODEL,
				VehicleEntry.COLUMN_NAME_CAR_YEAR,
				VehicleEntry.COLUMN_NAME_CAR_VIN,
				VehicleEntry.COLUMN_NAME_CAR_FUEL,
				VehicleEntry.COLUMN_NAME_CAR_CHECKUP,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_ID,
				VehicleEntry.COLUMN_NAME_ALERTS,
				VehicleEntry.COLUMN_NAME_LATITUDE,
				VehicleEntry.COLUMN_NAME_LONGITUDE,
				VehicleEntry.COLUMN_NAME_ADDRESS,
				VehicleEntry.COLUMN_NAME_GRADE,
				VehicleEntry.COLUMN_NAME_SCORE,
			    VehicleEntry.COLUMN_NAME_TOTAL_TRIPS_COUNT,
			    VehicleEntry.COLUMN_NAME_TOTAL_DRIVING_TIME,
			    VehicleEntry.COLUMN_NAME_TOTAL_DISTANCE,
			    VehicleEntry.COLUMN_NAME_TOTAL_BREAKING,
			    VehicleEntry.COLUMN_NAME_TOTAL_ACCELERATION,
			    VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING,
			    VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING_DISTANCE,
			    VehicleEntry.COLUMN_NAME_ODOMETER}, 
				VehicleEntry._ID+" = ?", new String[]{Long.toString(id)}, null, null, null);
		
		Driver d = new Driver();
		
		if(c.moveToFirst()){
			d.id = c.getLong(0);
			d.name = c.getString(1);
			d.markerIcon = c.getString(2);
			d.photo = c.getString(3);
			d.carMake = c.getString(4);
			d.carModel = c.getString(5);
			d.carYear = c.getString(6);
			d.carVIN = c.getString(7);
			d.carFuelLevel = c.getInt(8);
			d.carCheckup = c.getInt(9) == 1;
			d.lastTripDate = c.getString(10);
			d.lastTripId = c.getLong(11);
			d.alerts = c.getInt(12);
			d.latitude = c.getLong(13);
			d.longitude = c.getLong(14);
			d.address = c.getString(15);
			d.grade = c.getString(16);
			d.score = c.getInt(17);
			d.totalTripsCount = c.getInt(18);
			d.totalDrivingTime = c.getInt(19);
			d.totalDistance = c.getDouble(20);
			d.totalBraking = c.getInt(21);
			d.totalAcceleration = c.getInt(22);
			d.totalSpeeding = c.getInt(23);
			d.totalSpeedingDistance = c.getDouble(24);
			d.odometer = c.getInt(25);
				
		}
		c.close();
		db.close();
		return d;
	}
	
	public void saveDriver(Driver d){
		ArrayList<Driver> drivers = new ArrayList<Driver>();
		drivers.add(d);
		saveDrivers(drivers, false);
	}
	
	public void saveDrivers(ArrayList<Driver> drivers, boolean removeDrivers){
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
					+ VehicleEntry.COLUMN_NAME_ALERTS +","
					+ VehicleEntry.COLUMN_NAME_ODOMETER +""
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
			
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
		    	statement.bindLong(26, driver.odometer);
		    	statement.execute();
		    	
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();

		    if(removeDrivers){
			    SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+VehicleEntry.TABLE_NAME+" WHERE "+VehicleEntry._ID+" NOT IN (" + ids + ")");
			    database.beginTransaction();
			    removeStatement.clearBindings();
		        removeStatement.execute();
		        database.setTransactionSuccessful();	
			    database.endTransaction();
			    removeStatement.close();
		    }
		}
		
		database.close();
	}
	
	public void saveDrivers(ArrayList<Driver> drivers){
		saveDrivers(drivers, true);		
	}
	
	public void saveTrip(Trip trip){
		ArrayList<Trip> trips = new ArrayList<Trip>();
		trips.add(trip);
		saveTrips(trips);
	}
	
	public void saveTrips(ArrayList<Trip> trips){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && trips!=null){
			String sql = "INSERT OR REPLACE INTO "+ TripEntry.TABLE_NAME +" ("
					+ TripEntry._ID +","
					+ TripEntry.COLUMN_NAME_EVENTS_COUNT +","
					+ TripEntry.COLUMN_NAME_START_TIME +","
					+ TripEntry.COLUMN_NAME_END_TIME +","
					+ TripEntry.COLUMN_NAME_DISTANCE +","
					+ TripEntry.COLUMN_NAME_AVG_SPEED +","
					+ TripEntry.COLUMN_NAME_MAX_SPEED +""
					+ ") VALUES (?,?,?,?,?," +
					"(SELECT IFNULL(NULLIF((SELECT "+TripEntry.COLUMN_NAME_AVG_SPEED+" FROM trips WHERE "+TripEntry._ID+" IS ?),0),?))," +
					"(SELECT IFNULL(NULLIF((SELECT "+TripEntry.COLUMN_NAME_MAX_SPEED+" FROM trips WHERE "+TripEntry._ID+" IS ?),0),?)));";
			
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
		    	statement.bindDouble(6, trip.id);
		    	statement.bindDouble(7, trip.averageSpeed);
		    	statement.bindDouble(8, trip.id);
		    	statement.bindDouble(9, trip.maxSpeed);
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
	
	public void saveRoute(long tripId, ArrayList<LatLng> route){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && route!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+RouteEntry.TABLE_NAME+" WHERE "+RouteEntry.COLUMN_NAME_TRIP_ID+" = "+tripId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ RouteEntry.TABLE_NAME +" ("
					+ RouteEntry.COLUMN_NAME_TRIP_ID +","
					+ RouteEntry.COLUMN_NAME_LATITUDE +","
					+ RouteEntry.COLUMN_NAME_LONGITUDE
					+ ") VALUES (?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    for (LatLng loc : route) {
		    	statement.clearBindings();
		    	statement.bindLong(1, tripId);
		    	statement.bindDouble(2, loc.latitude);
		    	statement.bindDouble(3, loc.longitude);
		    	statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		database.close();
	}
	
	public void savePoints(long tripId, ArrayList<Point> points){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && points!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+PointEntry.TABLE_NAME+" WHERE "+PointEntry.COLUMN_NAME_TRIP_ID+" = "+tripId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ PointEntry.TABLE_NAME +" ("
					+ PointEntry.COLUMN_NAME_TRIP_ID +","
					+ PointEntry.COLUMN_NAME_LATITUDE +","
					+ PointEntry.COLUMN_NAME_LONGITUDE +","
					+ PointEntry.COLUMN_NAME_EVENTS
					+ ") VALUES (?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    for (Point p : points) {
		    	statement.clearBindings();
		    	statement.bindLong(1, tripId);
		    	statement.bindDouble(2, p.getLatitude());
		    	statement.bindDouble(3, p.getLongitude());
		    	statement.bindString(4, p.getEventsString());
		    	statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		database.close();
	}
	
	public void saveEvents(long tripId, ArrayList<Event> events){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && events!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+EventEntry.TABLE_NAME+" WHERE "+EventEntry.COLUMN_NAME_TRIP_ID+" = "+tripId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ EventEntry.TABLE_NAME +" ("
					+ EventEntry.COLUMN_NAME_TRIP_ID +","
					+ EventEntry.COLUMN_NAME_TYPE +","
					+ EventEntry.COLUMN_NAME_TITLE +","
					+ EventEntry.COLUMN_NAME_ADDRESS
					+ ") VALUES (?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    for (Event e : events) {
		    	statement.clearBindings();
		    	statement.bindLong(1, tripId);
		    	statement.bindString(2, e.type.toString());
		    	statement.bindString(3, e.title);
		    	statement.bindString(4, e.address);
		    	statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		database.close();
	}
	
	public void saveScoreGraph(long driverId, MonthStats[] yearStats){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && yearStats!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+ScoreGraphEntry.TABLE_NAME+" WHERE "+ScoreGraphEntry.COLUMN_NAME_DRIVER_ID+" = "+driverId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ ScoreGraphEntry.TABLE_NAME +" ("
					+ ScoreGraphEntry.COLUMN_NAME_DRIVER_ID +","
					+ ScoreGraphEntry.COLUMN_NAME_MONTH +","
					+ ScoreGraphEntry.COLUMN_NAME_SCORE +","
					+ ScoreGraphEntry.COLUMN_NAME_GRADE
					+ ") VALUES (?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    for (MonthStats ms : yearStats) {
		    	statement.clearBindings();
		    	statement.bindLong(1, driverId);
		    	statement.bindLong(2, ms.month);
		    	statement.bindLong(3, ms.score);
		    	statement.bindString(4, ms.grade);
		    	statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		database.close();
	}
	
	public void saveScorePercentage(long driverId, LinkedHashMap<String, Integer> valuesMap){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && valuesMap!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+ScorePercentageEntry.TABLE_NAME+" WHERE "+ScorePercentageEntry.COLUMN_NAME_DRIVER_ID+" = "+driverId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ ScorePercentageEntry.TABLE_NAME +" ("
					+ ScorePercentageEntry.COLUMN_NAME_DRIVER_ID +","
					+ ScorePercentageEntry.COLUMN_NAME_STAT_NAME +","
					+ ScorePercentageEntry.COLUMN_NAME_STAT_VALUE
					+ ") VALUES (?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (LinkedHashMap.Entry<String, Integer> entry : valuesMap.entrySet()) {
		        String key = entry.getKey();
		        int value = entry.getValue();
		        
		        statement.clearBindings();
		    	statement.bindLong(1, driverId);
		    	statement.bindString(2, key);
		    	statement.bindLong(3, value);
		    	statement.execute();
		    }
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		database.close();
	}
	
	public void saveScorePieCharts(long driverId, ArrayList<PieChartTab> tabs){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && tabs!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+ScorePieChartEntry.TABLE_NAME+" WHERE "+ScorePieChartEntry.COLUMN_NAME_DRIVER_ID+" = "+driverId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ ScorePieChartEntry.TABLE_NAME +" ("
					+ ScorePieChartEntry.COLUMN_NAME_DRIVER_ID +","
					+ ScorePieChartEntry.COLUMN_NAME_TAB +","
					+ ScorePieChartEntry.COLUMN_NAME_VALUE +","
					+ ScorePieChartEntry.COLUMN_NAME_TITLE +","
					+ ScorePieChartEntry.COLUMN_NAME_SUBTITLE
					+ ") VALUES (?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (PieChartTab tab : tabs) {
		    	int piecesCount = tab.values.length;
		    	for (int i = 0; i < piecesCount; i++) {
		    		statement.clearBindings();
			    	statement.bindLong(1, driverId);
			    	statement.bindString(2, tab.tabName);
			    	statement.bindDouble(3, tab.values[i]);
			    	statement.bindString(4, tab.titles[i]);
			    	if(tab.subtitles!=null)
			    		statement.bindString(5, tab.subtitles[i]);
			    	else
			    		statement.bindString(5, "");
			    	statement.execute();
				}
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		database.close();
	}
	
	public void saveScoreCircles(long driverId, LinkedHashMap<String, ArrayList<CirclesSection>> tabs){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && tabs!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+ScoreCirclesEntry.TABLE_NAME+" WHERE "+ScoreCirclesEntry.COLUMN_NAME_DRIVER_ID+" = "+driverId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ ScoreCirclesEntry.TABLE_NAME +" ("
					+ ScoreCirclesEntry.COLUMN_NAME_DRIVER_ID +","
					+ ScoreCirclesEntry.COLUMN_NAME_TAB +","
					+ ScoreCirclesEntry.COLUMN_NAME_SECTION +","
					+ ScoreCirclesEntry.COLUMN_NAME_MARK +","
					+ ScoreCirclesEntry.COLUMN_NAME_DISTANCE
					+ ") VALUES (?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (LinkedHashMap.Entry<String, ArrayList<CirclesSection>> entry : tabs.entrySet()) {
		    	String tabName = entry.getKey();
		    	ArrayList<CirclesSection> sections = entry.getValue();
		    	
		    	for (CirclesSection section : sections) {
		    		for (int i = 0; i < section.marks.length; i++) {
			    		statement.clearBindings();
				    	statement.bindLong(1, driverId);
				    	statement.bindString(2, tabName);
				    	statement.bindString(3, section.sectionName);
				    	statement.bindLong(4, section.marks[i]);
				    	statement.bindDouble(5, section.distances[i]);
				    	statement.execute();
					}
				}
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		database.close();
	}
	
}
