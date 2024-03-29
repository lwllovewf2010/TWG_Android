package com.modusgo.ubi.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.modusgo.ubi.Alert;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.DiagnosticsFragment.Maintenance;
import com.modusgo.ubi.DiagnosticsFragment.WarrantyInformation;
import com.modusgo.ubi.DiagnosticsTroubleCode;
import com.modusgo.ubi.LimitsFragment.Limit;
import com.modusgo.ubi.Recall;
import com.modusgo.ubi.ScoreCirclesActivity.CirclesSection;
import com.modusgo.ubi.ScoreFragment.MonthStats;
import com.modusgo.ubi.ScorePieChartActivity.PieChartTab;
import com.modusgo.ubi.Tracking;
import com.modusgo.ubi.Trip;
import com.modusgo.ubi.Trip.Point;
import com.modusgo.ubi.Vehicle;
import com.modusgo.ubi.db.AlertContract.AlertEntry;
import com.modusgo.ubi.db.DTCContract.DTCEntry;
import com.modusgo.ubi.db.LimitsContract.LimitsEntry;
import com.modusgo.ubi.db.MaintenanceContract.MaintenanceEntry;
import com.modusgo.ubi.db.PointContract.PointEntry;
import com.modusgo.ubi.db.RecallContract.RecallEntry;
import com.modusgo.ubi.db.RouteContract.RouteEntry;
import com.modusgo.ubi.db.ScoreCirclesContract.ScoreCirclesEntry;
import com.modusgo.ubi.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.ubi.db.ScoreInfoContract.ScoreInfoEntry;
import com.modusgo.ubi.db.ScorePercentageContract.ScorePercentageEntry;
import com.modusgo.ubi.db.ScorePieChartContract.ScorePieChartEntry;
import com.modusgo.ubi.db.SpeedingRouteContract.SpeedingRouteEntry;
import com.modusgo.ubi.db.TrackingContract.TrackingEntry;
import com.modusgo.ubi.db.TripContract.TripEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.db.WarrantyInfoContract.WarrantyInfoEntry;
import com.modusgo.ubi.utils.Utils;

public class DbHelper extends SQLiteOpenHelper {
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INTEGER";
	private static final String FLOAT_TYPE = " REAL";
	
	private static final String COMMA_SEP = ",";
	
	private static final String[] SQL_CREATE_ENTRIES = new String[]{
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
	    VehicleEntry.COLUMN_NAME_CAR_FUEL_UNIT + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_FUEL_STATUS + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_DTC_COUNT + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LONGITUDE + FLOAT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LAST_TRIP_ID + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_IN_TRIP + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_HIDE_ENGINE + INT_TYPE + COMMA_SEP +
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
	    VehicleEntry.COLUMN_NAME_ODOMETER + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_LAST_CHECKUP + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_CHECKUP_STATUS + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LIMITS_BLOCKED + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_LIMITS_BLOCKED_BY + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_UPDATED_AT + TEXT_TYPE + " ); ",
	    
		    "CREATE TABLE " + TripEntry.TABLE_NAME + " (" +
		    TripEntry._ID + " INTEGER PRIMARY KEY," +
		    TripEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_EVENTS_COUNT + INT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_START_TIME + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_END_TIME + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_DISTANCE + FLOAT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_AVG_SPEED + FLOAT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_MAX_SPEED + FLOAT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_GRADE + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_FUEL + FLOAT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_FUEL_UNIT + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_FUEL_COST + FLOAT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_FUEL_STATUS + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_VIEWED_AT + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_UPDATED_AT + TEXT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_HIDDEN + INT_TYPE + " ); ",
	
		    "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +
		    RouteEntry._ID + " INTEGER PRIMARY KEY," +
		    RouteEntry.COLUMN_NAME_TRIP_ID + INT_TYPE + COMMA_SEP +
		    RouteEntry.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
		    RouteEntry.COLUMN_NAME_LONGITUDE + FLOAT_TYPE +  " ); ",
	
		    "CREATE TABLE " + SpeedingRouteEntry.TABLE_NAME + " (" +
		    SpeedingRouteEntry._ID + " INTEGER PRIMARY KEY," +
		    SpeedingRouteEntry.COLUMN_NAME_TRIP_ID + INT_TYPE + COMMA_SEP +
		    SpeedingRouteEntry.COLUMN_NAME_NUM + INT_TYPE + COMMA_SEP +
		    SpeedingRouteEntry.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
		    SpeedingRouteEntry.COLUMN_NAME_LONGITUDE + FLOAT_TYPE +  " ); ",
	
		    "CREATE TABLE " + PointEntry.TABLE_NAME + " (" +
		    PointEntry._ID + " INTEGER PRIMARY KEY," +
		    PointEntry.COLUMN_NAME_TRIP_ID + INT_TYPE + COMMA_SEP +
		    PointEntry.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
		    PointEntry.COLUMN_NAME_LONGITUDE + FLOAT_TYPE + COMMA_SEP +
		    PointEntry.COLUMN_NAME_EVENT + TEXT_TYPE + COMMA_SEP +
		    PointEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
		    PointEntry.COLUMN_NAME_ADDRESS + TEXT_TYPE +  " ); ",
	
		    "CREATE TABLE " + ScoreGraphEntry.TABLE_NAME + " (" +
		    ScoreGraphEntry._ID + " INTEGER PRIMARY KEY," +
		    ScoreGraphEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
		    ScoreGraphEntry.COLUMN_NAME_MONTH + INT_TYPE + COMMA_SEP +
		    ScoreGraphEntry.COLUMN_NAME_SCORE + INT_TYPE + COMMA_SEP +
		    ScoreGraphEntry.COLUMN_NAME_GRADE + TEXT_TYPE +  " ); ",
	
		    "CREATE TABLE " + ScorePercentageEntry.TABLE_NAME + " (" +
		    ScorePercentageEntry._ID + " INTEGER PRIMARY KEY," +
		    ScorePercentageEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
		    ScorePercentageEntry.COLUMN_NAME_STAT_NAME + TEXT_TYPE + COMMA_SEP +
		    ScorePercentageEntry.COLUMN_NAME_STAT_VALUE + INT_TYPE +  " ); ",
	
		    "CREATE TABLE " + ScoreInfoEntry.TABLE_NAME + " (" +
		    ScoreInfoEntry._ID + " INTEGER PRIMARY KEY," +
		    ScoreInfoEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
		    ScoreInfoEntry.COLUMN_NAME_PROFILE_DATE + TEXT_TYPE + COMMA_SEP +
		    ScoreInfoEntry.COLUMN_NAME_START_DATE + TEXT_TYPE + COMMA_SEP +
		    ScoreInfoEntry.COLUMN_NAME_PROFILE_DRIVING_MILES + FLOAT_TYPE + COMMA_SEP +
		    ScoreInfoEntry.COLUMN_NAME_ESTIMATED_ANNUAL_DRIVING + FLOAT_TYPE +  " ); ",
	
		    "CREATE TABLE " + ScorePieChartEntry.TABLE_NAME + " (" +
		    ScorePieChartEntry._ID + " INTEGER PRIMARY KEY," +
		    ScorePieChartEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_TAB + TEXT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_VALUE + FLOAT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_SUBTITLE + TEXT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_COLOR + INT_TYPE + " ); ",
	
		    "CREATE TABLE " + ScoreCirclesEntry.TABLE_NAME + " (" +
		    ScoreCirclesEntry._ID + " INTEGER PRIMARY KEY," +
		    ScoreCirclesEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
		    ScoreCirclesEntry.COLUMN_NAME_TAB + TEXT_TYPE + COMMA_SEP +
		    ScoreCirclesEntry.COLUMN_NAME_SECTION + TEXT_TYPE + COMMA_SEP +
		    ScoreCirclesEntry.COLUMN_NAME_MARK + INT_TYPE + COMMA_SEP +
		    ScoreCirclesEntry.COLUMN_NAME_DISTANCE + FLOAT_TYPE + " ); ",
	
		    "CREATE TABLE " + DTCEntry.TABLE_NAME + " (" +
		    DTCEntry._ID + " INTEGER PRIMARY KEY," +
		    DTCEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_CODE + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_CONDITIONS + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_CREATED_AT + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_DETAILS + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_FULL_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_IMPORTANCE + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_LABOR_COST + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_LABOR_HOURS + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_PARTS + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_PARTS_COST + TEXT_TYPE + COMMA_SEP +
		    DTCEntry.COLUMN_NAME_TOTAL_COST + TEXT_TYPE + " ); ",
	
		    "CREATE TABLE " + RecallEntry.TABLE_NAME + " (" +
		    RecallEntry._ID + " INTEGER PRIMARY KEY," +
		    RecallEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
		    RecallEntry.COLUMN_NAME_CONSEQUENCE + TEXT_TYPE + COMMA_SEP +
		    RecallEntry.COLUMN_NAME_CORRECTIVE_ACTION + TEXT_TYPE + COMMA_SEP +
		    RecallEntry.COLUMN_NAME_CREATED_AT + TEXT_TYPE + COMMA_SEP +
		    RecallEntry.COLUMN_NAME_DEFECT_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
		    RecallEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
		    RecallEntry.COLUMN_NAME_RECALL_ID + TEXT_TYPE + " ); ",
		    
		    "CREATE TABLE " + MaintenanceEntry.TABLE_NAME + " (" +
		    MaintenanceEntry._ID + " INTEGER PRIMARY KEY," +
		    MaintenanceEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
			MaintenanceEntry.COLUMN_NAME_CREATED_AT + TEXT_TYPE + COMMA_SEP +
			MaintenanceEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
			MaintenanceEntry.COLUMN_NAME_IMPORTANCE + TEXT_TYPE + COMMA_SEP +
			MaintenanceEntry.COLUMN_NAME_MILEAGE + TEXT_TYPE + COMMA_SEP +
			MaintenanceEntry.COLUMN_NAME_PRICE + FLOAT_TYPE + " ); ",
		    
		    "CREATE TABLE " + WarrantyInfoEntry.TABLE_NAME + " (" +
		    WarrantyInfoEntry._ID + " INTEGER PRIMARY KEY," +
		    WarrantyInfoEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
			WarrantyInfoEntry.COLUMN_NAME_CREATED_AT + TEXT_TYPE + COMMA_SEP +
			WarrantyInfoEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
			WarrantyInfoEntry.COLUMN_NAME_MILEAGE + TEXT_TYPE + " )",
		    
		    "CREATE TABLE " + LimitsEntry.TABLE_NAME + " (" +
		    LimitsEntry._ID + " INTEGER PRIMARY KEY," +
		    LimitsEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
			LimitsEntry.COLUMN_NAME_KEY + TEXT_TYPE + COMMA_SEP +
			LimitsEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
			LimitsEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
			LimitsEntry.COLUMN_NAME_VALUE + TEXT_TYPE + COMMA_SEP +
			LimitsEntry.COLUMN_NAME_MIN_VALUE + TEXT_TYPE + COMMA_SEP +
			LimitsEntry.COLUMN_NAME_MAX_VALUE + TEXT_TYPE + COMMA_SEP +
			LimitsEntry.COLUMN_NAME_STEP + TEXT_TYPE + COMMA_SEP +
			LimitsEntry.COLUMN_NAME_ACTIVE + INT_TYPE + " ); ",
		    
		    "CREATE TABLE " + AlertEntry.TABLE_NAME + " (" +
		    AlertEntry._ID + " INTEGER PRIMARY KEY," +
		    AlertEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_TRIP_ID + INT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_LONGITUDE + FLOAT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_SEEN_AT + TEXT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_GEOFENCE + TEXT_TYPE + COMMA_SEP +
			AlertEntry.COLUMN_NAME_ADDRESS + TEXT_TYPE + " ); ",
		    
		    "CREATE TABLE " + TrackingEntry.TABLE_NAME + " (" +
		    TrackingEntry._ID + " INTEGER PRIMARY KEY," +
		    TrackingEntry.COLUMN_NAME_DRIVER_ID + INT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_LONGITUDE + FLOAT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_ALTITUDE + FLOAT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_HEADING + FLOAT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_HORIZONTAL_ACCURACY + FLOAT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_VERTICAL_ACCURACY + FLOAT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_SATELITES + INT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_FIX_STATUS + INT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_SPEED + FLOAT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_EVENT + TEXT_TYPE + COMMA_SEP +
		    TrackingEntry.COLUMN_NAME_RAW_DATA + TEXT_TYPE + COMMA_SEP +
			TrackingEntry.COLUMN_NAME_BLOCKED + INT_TYPE + " )"};

	private static final String[] SQL_DELETE_ENTRIES = new String[]{
	"DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + SpeedingRouteEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + PointEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + ScoreGraphEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + ScorePercentageEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + ScoreInfoEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + ScorePieChartEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + ScoreCirclesEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + DTCEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + RecallEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + MaintenanceEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + WarrantyInfoEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + LimitsEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + AlertEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + TrackingEntry.TABLE_NAME};
	
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 54;
	public static final String DATABASE_NAME = "ubi.db";
	
	private static DbHelper sInstance;
    private SQLiteDatabase database;
    private int databaseOpenCounter = 0;
	
	public static synchronized DbHelper getInstance(Context context) {
		// Use the application context, which will ensure that you 
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new DbHelper(context.getApplicationContext());
		}
		return sInstance;
	}
	
	public synchronized SQLiteDatabase openDatabase() {
        databaseOpenCounter++;
        if(databaseOpenCounter == 1) {
            database = sInstance.getWritableDatabase();
        }
        return database;
    }
	
	public synchronized void closeDatabase() {
        databaseOpenCounter--;
        if(databaseOpenCounter == 0) {
            database.close();
        }
	}
	@Override
	public synchronized void close() {
		if(databaseOpenCounter == 0)
			super.close();
	}
	
	public DbHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	public void onCreate(SQLiteDatabase db) {
		//One sql create request for each table
		for (String sql_query : SQL_CREATE_ENTRIES) {
			db.execSQL(sql_query);
		}
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    // This database is only a cache for online data, so its upgrade policy is
	    // to simply to discard the data and start over
		for (int i = 0; i < SQL_CREATE_ENTRIES.length; i++) {
			db.execSQL(SQL_DELETE_ENTRIES[i]);
			db.execSQL(SQL_CREATE_ENTRIES[i]);
		}
	}
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    onUpgrade(db, oldVersion, newVersion);
	}
	
	public void resetDatabase(){
		SQLiteDatabase db = openDatabase();
		for (int i = 0; i < SQL_CREATE_ENTRIES.length; i++) {
			db.execSQL(SQL_DELETE_ENTRIES[i]);
			db.execSQL(SQL_CREATE_ENTRIES[i]);
		}
		closeDatabase();
	}
	
	public ArrayList<Vehicle> getVehiclesShort(){
		System.out.println("Get vehicles short");
		SQLiteDatabase db = openDatabase();
		Cursor c = db.query(VehicleEntry.TABLE_NAME, 
				new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_CAR_MAKE,
				VehicleEntry.COLUMN_NAME_CAR_MODEL,
				VehicleEntry.COLUMN_NAME_CAR_YEAR,
				VehicleEntry.COLUMN_NAME_CAR_DTC_COUNT,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE,
				VehicleEntry.COLUMN_NAME_ALERTS,
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO,
				VehicleEntry.COLUMN_NAME_IN_TRIP,
				VehicleEntry.COLUMN_NAME_HIDE_ENGINE,
				VehicleEntry.COLUMN_NAME_UPDATED_AT}, 
				null, null, null, null, null);
		
		ArrayList<Vehicle> drivers = new ArrayList<Vehicle>();
		
		if(c.moveToFirst()){
			while (!c.isAfterLast()) {
				Vehicle d = new Vehicle();
				d.id = c.getLong(0);
				d.name = c.getString(1);
				d.carMake = c.getString(2);
				d.carModel = c.getString(3);
				d.carYear = c.getString(4);
				d.carDTCCount = c.getInt(5);
				d.lastTripDate = c.getString(6);
				d.alerts = c.getInt(7);
				d.photo = c.getString(8);
				d.inTrip = c.getInt(9) == 1;
				d.hideEngineIcon = c.getInt(10) == 1;
				d.updatedAt = c.getString(11);
				drivers.add(d);
				
				c.moveToNext();
			}
		}
		c.close();
		closeDatabase();
		return drivers;
	}
	
	public Vehicle getVehicleShort(long id){
		System.out.println("Get vehicle short");
		SQLiteDatabase db = openDatabase();
		Cursor c = db.query(VehicleEntry.TABLE_NAME, 
				new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO,
				VehicleEntry.COLUMN_NAME_LATITUDE,
				VehicleEntry.COLUMN_NAME_LONGITUDE,
				VehicleEntry.COLUMN_NAME_IN_TRIP}, 
				VehicleEntry._ID+" = ?", new String[]{Long.toString(id)}, null, null, null);
		
		Vehicle d = new Vehicle();
		
		if(c.moveToFirst()){
			d.id = c.getLong(0);
			d.name = c.getString(1);
			d.photo = c.getString(2);
			d.latitude = c.getDouble(3);
			d.longitude = c.getDouble(4);
			d.inTrip = c.getInt(5) == 1;
		}
		c.close();
		closeDatabase();
		return d;
	}
	
	public Vehicle getVehicle(long id){
		System.out.println("Get vehicle");
		SQLiteDatabase db = openDatabase();
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
				VehicleEntry.COLUMN_NAME_CAR_FUEL_UNIT,
				VehicleEntry.COLUMN_NAME_CAR_FUEL_STATUS,
				VehicleEntry.COLUMN_NAME_CAR_DTC_COUNT,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_ID,
				VehicleEntry.COLUMN_NAME_IN_TRIP,
				VehicleEntry.COLUMN_NAME_HIDE_ENGINE,
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
			    VehicleEntry.COLUMN_NAME_ODOMETER,
			    VehicleEntry.COLUMN_NAME_CAR_LAST_CHECKUP,
			    VehicleEntry.COLUMN_NAME_CAR_CHECKUP_STATUS,
			    VehicleEntry.COLUMN_NAME_LIMITS_BLOCKED,
			    VehicleEntry.COLUMN_NAME_LIMITS_BLOCKED_BY,
			    VehicleEntry.COLUMN_NAME_UPDATED_AT}, 
				VehicleEntry._ID+" = ?", new String[]{Long.toString(id)}, null, null, null);
		
		Vehicle v = new Vehicle();
		
		if(c.moveToFirst()){
			v.id = c.getLong(0);
			v.name = c.getString(1);
			v.markerIcon = c.getString(2);
			v.photo = c.getString(3);
			v.carMake = c.getString(4);
			v.carModel = c.getString(5);
			v.carYear = c.getString(6);
			v.carVIN = c.getString(7);
			v.carFuelLevel = c.getInt(8);
			v.carFuelUnit = c.getString(9);
			v.carFuelStatus = c.getString(10);
			v.carDTCCount = c.getInt(11);
			v.lastTripDate = c.getString(12);
			v.lastTripId = c.getLong(13);
			v.inTrip = c.getInt(14) == 1;
			v.hideEngineIcon = c.getInt(15) == 1;
			v.alerts = c.getInt(16);
			v.latitude = c.getDouble(17);
			v.longitude = c.getDouble(18);
			v.address = c.getString(19);
			v.grade = c.getString(20);
			v.score = c.getInt(21);
			v.totalTripsCount = c.getInt(22);
			v.totalDrivingTime = c.getInt(23);
			v.totalDistance = c.getDouble(24);
			v.totalBraking = c.getInt(25);
			v.totalAcceleration = c.getInt(26);
			v.totalSpeeding = c.getInt(27);
			v.totalSpeedingDistance = c.getDouble(28);
			v.carMileage = c.getInt(29);
			v.carLastCheckup = c.getString(30);
			v.carCheckupStatus = c.getString(31);
			v.limitsBlocked = c.getInt(32) == 1;
			v.limitsBlockedBy = c.getString(33);
			v.updatedAt = c.getString(34);
				
		}
		c.close();
		closeDatabase();
		return v;
	}
	
	
	public ArrayList<Trip> getTripsFromDb(Date endDate, int count, long id, SharedPreferences prefs){
		SQLiteDatabase db = openDatabase();
		
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
		
		Cursor c = db.query(TripEntry.TABLE_NAME, 
				new String[]{
				TripEntry._ID,
				TripEntry.COLUMN_NAME_EVENTS_COUNT,
				TripEntry.COLUMN_NAME_START_TIME,
				TripEntry.COLUMN_NAME_END_TIME,
				TripEntry.COLUMN_NAME_DISTANCE,
				TripEntry.COLUMN_NAME_GRADE,
				TripEntry.COLUMN_NAME_FUEL,
				TripEntry.COLUMN_NAME_FUEL_UNIT,
				TripEntry.COLUMN_NAME_FUEL_STATUS,
				TripEntry.COLUMN_NAME_FUEL_COST,
				TripEntry.COLUMN_NAME_VIEWED_AT,
				TripEntry.COLUMN_NAME_UPDATED_AT},
				TripEntry.COLUMN_NAME_VEHICLE_ID + " = " + id + " AND " + TripEntry.COLUMN_NAME_HIDDEN + " = 0 AND " +
				"datetime(" + TripEntry.COLUMN_NAME_START_TIME + ")<datetime('"+ Utils.fixTimeZoneColon(sdf.format(endDate)) + "')", null, null, null, "datetime("+TripEntry.COLUMN_NAME_START_TIME+") DESC", ""+count);

		System.out.println("trips from db: "+c.getCount());
		
		ArrayList<Trip> trips = new ArrayList<Trip>();
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				Trip t = new Trip(
						prefs,
						c.getLong(0), 
						c.getInt(1), 
						c.getString(2), 
						c.getString(3), 
						c.getDouble(4),
						c.getString(5));
				t.fuel = c.getFloat(6);
				t.fuelUnit = c.getString(7);
				t.fuelStatus = c.getString(8);
				t.fuelCost = c.getFloat(9);
				t.viewedAt = c.getString(10);
				t.updatedAt = c.getString(11);
				
				try {
					if(!TextUtils.isEmpty(t.viewedAt) && !TextUtils.isEmpty(t.updatedAt)){
						Calendar cViewedAt = Calendar.getInstance();
						cViewedAt.setTime(sdf.parse(t.viewedAt));
						Calendar cUpdatedAt = Calendar.getInstance();
						cUpdatedAt.setTime(sdf.parse(t.updatedAt));
						
						if(cViewedAt.after(cUpdatedAt))
							t.viewed = true;
					}
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				trips.add(t);
				c.moveToNext();
			}
		}
		c.close();
		closeDatabase();
		
		System.out.println("trips array "+trips.size());
		
		return trips;
	}
	
	public void saveVehicle(Vehicle v){
		ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
		vehicles.add(v);
		saveVehicles(vehicles, false);
	}
	
	public void saveVehicles(ArrayList<Vehicle> drivers, boolean removeVehicles){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && drivers!=null){
			String[] fields = new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_DRIVER_MARKER_ICON,
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO,
				VehicleEntry.COLUMN_NAME_CAR_VIN,
				VehicleEntry.COLUMN_NAME_CAR_MAKE,
				VehicleEntry.COLUMN_NAME_CAR_MODEL,
				VehicleEntry.COLUMN_NAME_CAR_YEAR,
				VehicleEntry.COLUMN_NAME_CAR_FUEL,
				VehicleEntry.COLUMN_NAME_CAR_FUEL_UNIT,
				VehicleEntry.COLUMN_NAME_CAR_FUEL_STATUS,
				VehicleEntry.COLUMN_NAME_CAR_DTC_COUNT,
				VehicleEntry.COLUMN_NAME_LATITUDE,
				VehicleEntry.COLUMN_NAME_LONGITUDE,
				VehicleEntry.COLUMN_NAME_ADDRESS,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_ID,
				VehicleEntry.COLUMN_NAME_IN_TRIP,
				VehicleEntry.COLUMN_NAME_HIDE_ENGINE,
				VehicleEntry.COLUMN_NAME_SCORE,
				VehicleEntry.COLUMN_NAME_GRADE,
				VehicleEntry.COLUMN_NAME_TOTAL_TRIPS_COUNT,
				VehicleEntry.COLUMN_NAME_TOTAL_DRIVING_TIME,
				VehicleEntry.COLUMN_NAME_TOTAL_DISTANCE,
				VehicleEntry.COLUMN_NAME_TOTAL_BREAKING,
				VehicleEntry.COLUMN_NAME_TOTAL_ACCELERATION,
				VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING,
				VehicleEntry.COLUMN_NAME_TOTAL_SPEEDING_DISTANCE,
				VehicleEntry.COLUMN_NAME_ALERTS,
				VehicleEntry.COLUMN_NAME_ODOMETER,
				VehicleEntry.COLUMN_NAME_CAR_LAST_CHECKUP,
				VehicleEntry.COLUMN_NAME_CAR_CHECKUP_STATUS,
				VehicleEntry.COLUMN_NAME_LIMITS_BLOCKED,
				VehicleEntry.COLUMN_NAME_LIMITS_BLOCKED_BY,
				VehicleEntry.COLUMN_NAME_UPDATED_AT
			};
			
			String sql = buildSQLStatementString("INSERT OR REPLACE", VehicleEntry.TABLE_NAME, fields);
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    String ids = "";
		    for (int i = 0; i < drivers.size(); i++) {
		    	Vehicle vehicle = drivers.get(i);
		    	
		    	ids+= i!=drivers.size()-1 ? vehicle.id+"," : vehicle.id;
		    	
		    	statement.clearBindings();
		    	statement.bindLong(1, vehicle.id);
		    	statement.bindString(2, vehicle.name);
		    	statement.bindString(3, vehicle.markerIcon);
		    	statement.bindString(4, vehicle.photo);
		    	statement.bindString(5, vehicle.carVIN);
		    	statement.bindString(6, vehicle.carMake);
		    	statement.bindString(7, vehicle.carModel);
		    	statement.bindString(8, vehicle.carYear);
		    	statement.bindLong(9, vehicle.carFuelLevel);
		    	statement.bindString(10, vehicle.carFuelUnit);
		    	statement.bindString(11, vehicle.carFuelStatus);
		    	statement.bindLong(12, vehicle.carDTCCount);
		    	statement.bindDouble(13, vehicle.latitude);
		    	statement.bindDouble(14, vehicle.longitude);
		    	statement.bindString(15, vehicle.address);
		    	statement.bindString(16, vehicle.lastTripDate);
		    	statement.bindLong(17, vehicle.lastTripId);
		    	statement.bindLong(18, vehicle.inTrip ? 1 : 0);
		    	statement.bindLong(19, vehicle.hideEngineIcon ? 1 : 0);
		    	statement.bindLong(20, vehicle.score);
		    	statement.bindString(21, vehicle.grade);
		    	statement.bindLong(22, vehicle.totalTripsCount);
		    	statement.bindLong(23, vehicle.totalDrivingTime);
		    	statement.bindDouble(24, vehicle.totalDistance);
		    	statement.bindLong(25, vehicle.totalBraking);
		    	statement.bindLong(26, vehicle.totalAcceleration);
		    	statement.bindLong(27, vehicle.totalSpeeding);
		    	statement.bindDouble(28, vehicle.totalSpeedingDistance);
		    	statement.bindLong(29, vehicle.alerts);
		    	statement.bindLong(30, vehicle.carMileage);
		    	statement.bindString(31, vehicle.carLastCheckup);
		    	statement.bindString(32, vehicle.carCheckupStatus);
		    	statement.bindLong(33, vehicle.limitsBlocked ? 1 : 0);
		    	statement.bindString(34, vehicle.limitsBlockedBy);
		    	statement.bindString(35, vehicle.updatedAt);
		    	statement.execute();
		    	
			}
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();

		    if(removeVehicles){
			    SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+VehicleEntry.TABLE_NAME+" WHERE "+VehicleEntry._ID+" NOT IN (" + ids + ")");
			    database.beginTransaction();
			    removeStatement.clearBindings();
		        removeStatement.execute();
		        database.setTransactionSuccessful();	
			    database.endTransaction();
			    removeStatement.close();
		    }
		}
		
		closeDatabase();
	}
	
	public void saveVehicles(ArrayList<Vehicle> drivers){
		saveVehicles(drivers, true);		
	}
	
	public void saveTrip(long driverId, Trip trip){
		ArrayList<Trip> trips = new ArrayList<Trip>();
		trips.add(trip);
		saveTrips(driverId, trips);
	}
	
	public void saveTrips(long driverId, ArrayList<Trip> trips){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && database.isOpen() && trips!=null){
			String sql = "INSERT OR REPLACE INTO "+ TripEntry.TABLE_NAME +" ("
					+ TripEntry._ID +","
					+ TripEntry.COLUMN_NAME_VEHICLE_ID +","
					+ TripEntry.COLUMN_NAME_EVENTS_COUNT +","
					+ TripEntry.COLUMN_NAME_START_TIME +","
					+ TripEntry.COLUMN_NAME_END_TIME +","
					+ TripEntry.COLUMN_NAME_DISTANCE +","
					+ TripEntry.COLUMN_NAME_AVG_SPEED +","
					+ TripEntry.COLUMN_NAME_MAX_SPEED +","
					+ TripEntry.COLUMN_NAME_GRADE +","
					+ TripEntry.COLUMN_NAME_FUEL +","
					+ TripEntry.COLUMN_NAME_FUEL_UNIT +","
					+ TripEntry.COLUMN_NAME_FUEL_COST +","
					+ TripEntry.COLUMN_NAME_FUEL_STATUS +","
					+ TripEntry.COLUMN_NAME_VIEWED_AT +","
					+ TripEntry.COLUMN_NAME_UPDATED_AT +","
					+ TripEntry.COLUMN_NAME_HIDDEN +""
					+ ") VALUES (?,?,?,?,?,?," +
					"(SELECT IFNULL(NULLIF((SELECT "+TripEntry.COLUMN_NAME_AVG_SPEED+" FROM trips WHERE "+TripEntry._ID+" IS ?),0),?))," +
					"(SELECT IFNULL(NULLIF((SELECT "+TripEntry.COLUMN_NAME_MAX_SPEED+" FROM trips WHERE "+TripEntry._ID+" IS ?),0),?)),?,?,?,?,?," +
					"(SELECT IFNULL(NULLIF((SELECT "+TripEntry.COLUMN_NAME_VIEWED_AT+" FROM trips WHERE "+TripEntry._ID+" IS ?),''),?))," +
					"?," +
					"(SELECT IFNULL(NULLIF((SELECT "+TripEntry.COLUMN_NAME_HIDDEN+" FROM trips WHERE "+TripEntry._ID+" IS ?),0),?)));";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
//		    String ids = "";
		    for (int i = 0; i < trips.size(); i++) {
		    	Trip trip = trips.get(i);
//		    	ids+= i!=trips.size()-1 ? trip.id+"," : trip.id;
		    	statement.clearBindings();
		    	statement.bindLong(1, trip.id);
		    	statement.bindLong(2, driverId);
		    	statement.bindLong(3, trip.eventsCount);
		    	statement.bindString(4, trip.startDate);
		    	statement.bindString(5, trip.endDate);
		    	statement.bindDouble(6, trip.distance);
		    	statement.bindDouble(7, trip.id);
		    	statement.bindDouble(8, trip.averageSpeed);
		    	statement.bindDouble(9, trip.id);
		    	statement.bindDouble(10, trip.maxSpeed);
		    	statement.bindString(11, trip.grade);
		    	statement.bindDouble(12, trip.fuel);
		    	statement.bindString(13, trip.fuelUnit);
		    	statement.bindDouble(14, trip.fuelCost);
		    	statement.bindString(15, trip.fuelStatus);
		    	statement.bindDouble(16, trip.id);
		    	statement.bindString(17, trip.viewedAt);
		    	statement.bindString(18, trip.updatedAt);
		    	statement.bindDouble(19, trip.id);
		    	statement.bindLong(20, trip.hidden ? 1 : 0);
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
		
		closeDatabase();
		
	}
	
	public void saveRoute(long tripId, ArrayList<LatLng> route){
		SQLiteDatabase database = openDatabase();
		
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
		
		closeDatabase();
	}
	
	public void saveSpeedingRoute(long tripId, ArrayList<ArrayList<LatLng>> routes){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && routes!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+SpeedingRouteEntry.TABLE_NAME+" WHERE "+SpeedingRouteEntry.COLUMN_NAME_TRIP_ID+" = "+tripId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ SpeedingRouteEntry.TABLE_NAME +" ("
					+ SpeedingRouteEntry.COLUMN_NAME_TRIP_ID +","
					+ SpeedingRouteEntry.COLUMN_NAME_NUM +","
					+ SpeedingRouteEntry.COLUMN_NAME_LATITUDE +","
					+ SpeedingRouteEntry.COLUMN_NAME_LONGITUDE
					+ ") VALUES (?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    int routesCount = routes.size();
		    System.out.println("Saving speeding, "+routesCount);
		    for (int i = 0; i < routesCount; i++) {
		    	ArrayList<LatLng> route = routes.get(i);
		    	for (LatLng loc : route) {
			    	statement.clearBindings();
			    	statement.bindLong(1, tripId);
			    	statement.bindLong(2, i);
			    	statement.bindDouble(3, loc.latitude);
			    	statement.bindDouble(4, loc.longitude);
			    	statement.execute();
				}
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void savePoints(long tripId, ArrayList<Point> points){
		SQLiteDatabase database = openDatabase();
		
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
					+ PointEntry.COLUMN_NAME_EVENT +","
					+ PointEntry.COLUMN_NAME_TITLE +","
					+ PointEntry.COLUMN_NAME_ADDRESS
					+ ") VALUES (?,?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    for (Point p : points) {
		    	statement.clearBindings();
		    	statement.bindLong(1, tripId);
		    	statement.bindDouble(2, p.getLatitude());
		    	statement.bindDouble(3, p.getLongitude());
		    	statement.bindString(4, p.getEvent());
		    	statement.bindString(5, p.getTitle());
		    	statement.bindString(6, p.getAddress());
		    	statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void saveScoreGraph(long driverId, MonthStats[] yearStats){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && yearStats!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+ScoreGraphEntry.TABLE_NAME+" WHERE "+ScoreGraphEntry.COLUMN_NAME_VEHICLE_ID+" = "+driverId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ ScoreGraphEntry.TABLE_NAME +" ("
					+ ScoreGraphEntry.COLUMN_NAME_VEHICLE_ID +","
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
		
		closeDatabase();
	}
	
	public void saveScorePercentage(long driverId, LinkedHashMap<String, Integer> valuesMap){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && valuesMap!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+ScorePercentageEntry.TABLE_NAME+" WHERE "+ScorePercentageEntry.COLUMN_NAME_VEHICLE_ID+" = "+driverId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ ScorePercentageEntry.TABLE_NAME +" ("
					+ ScorePercentageEntry.COLUMN_NAME_VEHICLE_ID +","
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
		
		closeDatabase();
	}
	
	public void saveScoreInfo(long vehicleId, String profileDate, String startDate, float profileDrivingMiles, float estimatedAnnualDriving){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+ScoreInfoEntry.TABLE_NAME+" WHERE "+ScoreInfoEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ ScoreInfoEntry.TABLE_NAME +" ("
					+ ScoreInfoEntry.COLUMN_NAME_VEHICLE_ID +","
					+ ScoreInfoEntry.COLUMN_NAME_PROFILE_DATE +","
					+ ScoreInfoEntry.COLUMN_NAME_START_DATE +","
					+ ScoreInfoEntry.COLUMN_NAME_PROFILE_DRIVING_MILES +","
					+ ScoreInfoEntry.COLUMN_NAME_ESTIMATED_ANNUAL_DRIVING
					+ ") VALUES (?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    statement.clearBindings();
		    statement.bindLong(1, vehicleId);
		    statement.bindString(2, profileDate);
		    statement.bindString(3, startDate);
		    statement.bindDouble(4, profileDrivingMiles);
		    statement.bindDouble(5, estimatedAnnualDriving);
		    statement.execute();
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void saveScorePieCharts(long driverId, ArrayList<PieChartTab> tabs){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && tabs!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+ScorePieChartEntry.TABLE_NAME+" WHERE "+ScorePieChartEntry.COLUMN_NAME_VEHICLE_ID+" = "+driverId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ ScorePieChartEntry.TABLE_NAME +" ("
					+ ScorePieChartEntry.COLUMN_NAME_VEHICLE_ID +","
					+ ScorePieChartEntry.COLUMN_NAME_TAB +","
					+ ScorePieChartEntry.COLUMN_NAME_VALUE +","
					+ ScorePieChartEntry.COLUMN_NAME_TITLE +","
					+ ScorePieChartEntry.COLUMN_NAME_SUBTITLE +","
					+ ScorePieChartEntry.COLUMN_NAME_COLOR
					+ ") VALUES (?,?,?,?,?,?);";
			
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
		    		statement.bindLong(6, tab.colorIds[i]);
			    	statement.execute();
				}
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void saveScoreCircles(long driverId, LinkedHashMap<String, ArrayList<CirclesSection>> tabs){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && tabs!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+ScoreCirclesEntry.TABLE_NAME+" WHERE "+ScoreCirclesEntry.COLUMN_NAME_VEHICLE_ID+" = "+driverId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ ScoreCirclesEntry.TABLE_NAME +" ("
					+ ScoreCirclesEntry.COLUMN_NAME_VEHICLE_ID +","
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
		
		closeDatabase();
	}
	
	public void saveDTCs(long vehicleId, ArrayList<DiagnosticsTroubleCode> dtcs){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && dtcs!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+DTCEntry.TABLE_NAME+" WHERE "+DTCEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ DTCEntry.TABLE_NAME +" ("
					+ DTCEntry.COLUMN_NAME_VEHICLE_ID +","
					+ DTCEntry.COLUMN_NAME_CODE +","
					+ DTCEntry.COLUMN_NAME_CONDITIONS +","
					+ DTCEntry.COLUMN_NAME_CREATED_AT +","
					+ DTCEntry.COLUMN_NAME_DESCRIPTION +","
					+ DTCEntry.COLUMN_NAME_DETAILS +","
					+ DTCEntry.COLUMN_NAME_FULL_DESCRIPTION +","
					+ DTCEntry.COLUMN_NAME_IMPORTANCE +","
					+ DTCEntry.COLUMN_NAME_LABOR_COST +","
					+ DTCEntry.COLUMN_NAME_LABOR_HOURS +","
					+ DTCEntry.COLUMN_NAME_PARTS +","
					+ DTCEntry.COLUMN_NAME_PARTS_COST +","
					+ DTCEntry.COLUMN_NAME_TOTAL_COST
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (DiagnosticsTroubleCode dtc : dtcs) {
		    	statement.clearBindings();
			    statement.bindLong(1, vehicleId);
			    statement.bindString(2, dtc.code);
			    statement.bindString(3, dtc.conditions);
			    statement.bindString(4, dtc.created_at);
			    statement.bindString(5, dtc.description);
			    statement.bindString(6, dtc.details);
			    statement.bindString(7, dtc.full_description);
			    statement.bindString(8, dtc.importance);
			    statement.bindString(9, dtc.labor_cost);
			    statement.bindString(10, dtc.labor_hours);
			    statement.bindString(11, dtc.parts);
			    statement.bindString(12, dtc.parts_cost);
			    statement.bindString(13, dtc.total_cost);
			    statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void saveRecalls(long vehicleId, ArrayList<Recall> recalls){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && recalls!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+RecallEntry.TABLE_NAME+" WHERE "+RecallEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ RecallEntry.TABLE_NAME +" ("
					+ RecallEntry._ID +","
					+ RecallEntry.COLUMN_NAME_VEHICLE_ID +","
					+ RecallEntry.COLUMN_NAME_CONSEQUENCE +","
					+ RecallEntry.COLUMN_NAME_CORRECTIVE_ACTION +","
					+ RecallEntry.COLUMN_NAME_CREATED_AT +","
					+ RecallEntry.COLUMN_NAME_DEFECT_DESCRIPTION +","
					+ RecallEntry.COLUMN_NAME_DESCRIPTION +","
					+ RecallEntry.COLUMN_NAME_RECALL_ID
					+ ") VALUES (?,?,?,?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (Recall recall : recalls) {
		    	System.out.println("recall "+recall.id);
		    	statement.clearBindings();
			    statement.bindLong(1, recall.id);
			    statement.bindLong(2, vehicleId);
			    statement.bindString(3, recall.consequence);
			    statement.bindString(4, recall.corrective_action);
			    statement.bindString(5, recall.created_at);
			    statement.bindString(6, recall.defect_description);
			    statement.bindString(7, recall.description);
			    statement.bindString(8, recall.recall_id);
			    statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void deleteRecall(long vehicleId, long recallId){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+RecallEntry.TABLE_NAME+" WHERE "+RecallEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId +
					" AND " + RecallEntry._ID+" = " + recallId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
		}
		
		closeDatabase();
	}
	
	public void saveMaintenances(long vehicleId, ArrayList<Maintenance> maintenances){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && maintenances!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+MaintenanceEntry.TABLE_NAME+" WHERE "+MaintenanceEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ MaintenanceEntry.TABLE_NAME +" ("
					+ MaintenanceEntry._ID +","
					+ MaintenanceEntry.COLUMN_NAME_VEHICLE_ID +","
					+ MaintenanceEntry.COLUMN_NAME_CREATED_AT +","
					+ MaintenanceEntry.COLUMN_NAME_DESCRIPTION +","
					+ MaintenanceEntry.COLUMN_NAME_IMPORTANCE +","
					+ MaintenanceEntry.COLUMN_NAME_MILEAGE +","
					+ MaintenanceEntry.COLUMN_NAME_PRICE
					+ ") VALUES (?,?,?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (Maintenance m : maintenances) {
		    	statement.clearBindings();
			    statement.bindLong(1, m.id);
			    statement.bindLong(2, vehicleId);
			    statement.bindString(3, m.created_at);
			    statement.bindString(4, m.description);
			    statement.bindString(5, m.importance);
			    statement.bindString(6, m.mileage);
			    statement.bindDouble(7, m.price);
			    statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void deleteMaintenance(long vehicleId, long maintenanceId){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+MaintenanceEntry.TABLE_NAME+" WHERE "+MaintenanceEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId +
					" AND " + MaintenanceEntry._ID+" = " + maintenanceId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
		}
		
		closeDatabase();
	}
	
	public void saveWarrantyInformation(long vehicleId, ArrayList<WarrantyInformation> warrantyInfo){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && warrantyInfo!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+WarrantyInfoEntry.TABLE_NAME+" WHERE "+WarrantyInfoEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ WarrantyInfoEntry.TABLE_NAME +" ("
					+ WarrantyInfoEntry.COLUMN_NAME_VEHICLE_ID +","
					+ WarrantyInfoEntry.COLUMN_NAME_CREATED_AT +","
					+ WarrantyInfoEntry.COLUMN_NAME_DESCRIPTION +","
					+ WarrantyInfoEntry.COLUMN_NAME_MILEAGE
					+ ") VALUES (?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (WarrantyInformation wi : warrantyInfo) {
		    	statement.clearBindings();
			    statement.bindLong(1, vehicleId);
			    statement.bindString(2, wi.created_at);
			    statement.bindString(3, wi.description);
			    statement.bindString(4, wi.mileage);
			    statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void saveLimits(long driverId, ArrayList<Limit> limits){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && limits!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+LimitsEntry.TABLE_NAME+" WHERE "+LimitsEntry.COLUMN_NAME_VEHICLE_ID+" = "+driverId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ LimitsEntry.TABLE_NAME +" ("
					+ LimitsEntry.COLUMN_NAME_VEHICLE_ID +","
					+ LimitsEntry.COLUMN_NAME_KEY +","
					+ LimitsEntry.COLUMN_NAME_TITLE +","
					+ LimitsEntry.COLUMN_NAME_TYPE +","
					+ LimitsEntry.COLUMN_NAME_VALUE +","
					+ LimitsEntry.COLUMN_NAME_MIN_VALUE +","
					+ LimitsEntry.COLUMN_NAME_MAX_VALUE +","
					+ LimitsEntry.COLUMN_NAME_STEP +","
					+ LimitsEntry.COLUMN_NAME_ACTIVE
					+ ") VALUES (?,?,?,?,?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (Limit l : limits) {
		    	statement.clearBindings();
			    statement.bindLong(1, driverId);
			    statement.bindString(2, l.key);
			    statement.bindString(3, l.title);
			    statement.bindString(4, l.type);
			    statement.bindString(5, l.value);
			    statement.bindString(6, l.minValue);
			    statement.bindString(7, l.maxValue);
			    statement.bindString(8, l.step);
			    statement.bindLong(9, l.active ? 1 : 0);
			    statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void saveAlert(long vehicleId, Alert alert){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null){		    
		    String sql = "INSERT OR REPLACE INTO "+ AlertEntry.TABLE_NAME +" ("
					+ AlertEntry._ID +","
					+ AlertEntry.COLUMN_NAME_VEHICLE_ID +","
					+ AlertEntry.COLUMN_NAME_TRIP_ID +","
					+ AlertEntry.COLUMN_NAME_TYPE +","
					+ AlertEntry.COLUMN_NAME_TIMESTAMP +","
					+ AlertEntry.COLUMN_NAME_TITLE +","
					+ AlertEntry.COLUMN_NAME_DESCRIPTION +","
					+ AlertEntry.COLUMN_NAME_LATITUDE +","
					+ AlertEntry.COLUMN_NAME_LONGITUDE +","
					+ AlertEntry.COLUMN_NAME_SEEN_AT +","
					+ AlertEntry.COLUMN_NAME_GEOFENCE +","
					+ AlertEntry.COLUMN_NAME_ADDRESS
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
		    
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    statement.clearBindings();
		    statement.bindLong(1, alert.id);
		    statement.bindLong(2, vehicleId);
		    statement.bindLong(3, alert.tripId);
		    statement.bindString(4, alert.type);
		    statement.bindString(5, alert.timestamp);
		    statement.bindString(6, alert.title);
		    statement.bindString(7, alert.description);
		    statement.bindDouble(8, alert.location.latitude);
		    statement.bindDouble(9, alert.location.longitude);
		    statement.bindString(10, alert.seenAt);
		    statement.bindString(11, alert.geofenceString);
		    statement.bindString(12, alert.address);
		    statement.execute();
			
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
			
			closeDatabase();
		}
	}
	
	public void saveAlerts(long vehicleId, ArrayList<Alert> alerts){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && alerts!=null){
			
			String sql = "INSERT OR REPLACE INTO "+ AlertEntry.TABLE_NAME +" ("
					+ AlertEntry._ID +","
					+ AlertEntry.COLUMN_NAME_VEHICLE_ID +","
					+ AlertEntry.COLUMN_NAME_TRIP_ID +","
					+ AlertEntry.COLUMN_NAME_TYPE +","
					+ AlertEntry.COLUMN_NAME_TIMESTAMP +","
					+ AlertEntry.COLUMN_NAME_TITLE +","
					+ AlertEntry.COLUMN_NAME_DESCRIPTION +","
					+ AlertEntry.COLUMN_NAME_LATITUDE +","
					+ AlertEntry.COLUMN_NAME_LONGITUDE +","
					+ AlertEntry.COLUMN_NAME_SEEN_AT +","
					+ AlertEntry.COLUMN_NAME_GEOFENCE +","
					+ AlertEntry.COLUMN_NAME_ADDRESS
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?," +
					"(SELECT IFNULL(NULLIF((SELECT "+AlertEntry.COLUMN_NAME_ADDRESS+" FROM " + AlertEntry.TABLE_NAME + " WHERE "+AlertEntry._ID+" IS ?),''),?)));";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (Alert a : alerts) {
		    	statement.clearBindings();
			    statement.bindLong(1, a.id);
			    statement.bindLong(2, vehicleId);
			    statement.bindLong(3, a.tripId);
			    statement.bindString(4, a.type);
			    statement.bindString(5, a.timestamp);
			    statement.bindString(6, a.title);
			    statement.bindString(7, a.description);
			    statement.bindDouble(8, a.location.latitude);
			    statement.bindDouble(9, a.location.longitude);
			    statement.bindString(10, a.seenAt);
			    statement.bindString(11, a.geofenceString);
			    statement.bindLong(12, a.id);
			    statement.bindString(13, a.address);
			    statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		closeDatabase();
	}
	
	public void deleteAlert(long vehicleId, long alertId){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null){
			SQLiteStatement removeStatement = database.compileStatement(
					"DELETE FROM "+AlertEntry.TABLE_NAME+" WHERE "+AlertEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId + " AND " + AlertEntry._ID + " = " + alertId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
		}
		
		closeDatabase();
	}
	
	public void deleteAllAlerts(long vehicleId){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null){
			SQLiteStatement removeStatement = database.compileStatement(
					"DELETE FROM "+AlertEntry.TABLE_NAME+" WHERE "+AlertEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
		}
		
		closeDatabase();
	}
	
	public void saveTrackingEvent(Tracking timepoint, long driverId){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null){
			
			String sql = "INSERT INTO "+ TrackingEntry.TABLE_NAME +" ("
					+ TrackingEntry.COLUMN_NAME_DRIVER_ID + ","
					+ TrackingEntry.COLUMN_NAME_TIMESTAMP + ","
					+ TrackingEntry.COLUMN_NAME_EVENT + ","
					+ TrackingEntry.COLUMN_NAME_LATITUDE + ","
					+ TrackingEntry.COLUMN_NAME_LONGITUDE + ","
					+ TrackingEntry.COLUMN_NAME_ALTITUDE + ","
					+ TrackingEntry.COLUMN_NAME_SATELITES + ","
					+ TrackingEntry.COLUMN_NAME_HEADING + ","
					+ TrackingEntry.COLUMN_NAME_SPEED + ","
					+ TrackingEntry.COLUMN_NAME_FIX_STATUS + ","
					+ TrackingEntry.COLUMN_NAME_RAW_DATA + ","
					+ TrackingEntry.COLUMN_NAME_HORIZONTAL_ACCURACY + ","
					+ TrackingEntry.COLUMN_NAME_VERTICAL_ACCURACY + ","
					+ TrackingEntry.COLUMN_NAME_BLOCKED
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    statement.clearBindings();
		    statement.bindLong(1, driverId);
		    statement.bindString(2, timepoint.timestamp);
		    statement.bindString(3, timepoint.event);
		    statement.bindDouble(4, timepoint.latitude);
		    statement.bindDouble(5, timepoint.longitude);
		    statement.bindDouble(6, timepoint.altitude);
		    statement.bindLong(7, timepoint.satelites);
		    statement.bindDouble(8, timepoint.heading);
		    statement.bindDouble(9, timepoint.speed);
		    statement.bindLong(10, timepoint.fixStatus ? 1 : 0);
		    statement.bindString(11, timepoint.rawData);
		    statement.bindDouble(12, timepoint.horizontalAccuracy);
		    statement.bindDouble(13, timepoint.verticalAccuracy);
		    statement.bindLong(14, timepoint.blocked ? 1 : 0);
		    statement.execute();
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		closeDatabase();
	}
	
	public void setTrackingEventsBlock(ArrayList<Long> ids, boolean blocked){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && ids!=null && ids.size()>0){
			
			String sIds = "";
			int idsSize = ids.size();
		    for (int i = 0; i < idsSize; i++) {
		    	sIds+= i!=idsSize-1 ? ids.get(i)+"," : ids.get(i);
		    }
			
		    String blockStr = blocked ? "1" : "0";
		    
		    SQLiteStatement removeStatement = database.compileStatement("UPDATE "+TrackingEntry.TABLE_NAME+" SET " + TrackingEntry.COLUMN_NAME_BLOCKED + "= '"+blockStr+"' WHERE "+TrackingEntry._ID+" IN (" + sIds + ")");
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
		    
		}
		closeDatabase();
	}
	
	public void deleteTrackingEvents(ArrayList<Long> ids){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null && ids!=null && ids.size()>0){
			
			String sIds = "";
			int idsSize = ids.size();
		    for (int i = 0; i < idsSize; i++) {
		    	sIds+= i!=idsSize-1 ? ids.get(i)+"," : ids.get(i);
		    }
			
		    SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+TrackingEntry.TABLE_NAME+" WHERE "+TrackingEntry._ID+" IN (" + sIds + ")");
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
		    
		}
		closeDatabase();
	}
	
	public void deleteTrackingEvents(){
		SQLiteDatabase database = openDatabase();
		
		if(database!=null){
			
		    SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+TrackingEntry.TABLE_NAME);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
		    
		}
		closeDatabase();
	}
	
	private String buildSQLStatementString(String start, String tableName, String[] fields){
		StringBuilder sql = new StringBuilder(start + " INTO " + tableName +" (");
		
		int fieldsCount = fields.length;
		for (int i = 0; i < fieldsCount; i++) {
			sql.append(fields[i]);
			if(i<fieldsCount-1){
				sql.append(",");
			}
		}
		sql.append(") VALUES (");
		for (int i = 0; i < fieldsCount; i++) {
			sql.append("?");
			if(i<fieldsCount-1){
				sql.append(", ");
			}
		}
		sql.append(");");
		
		return sql.toString();
	}
	
}
