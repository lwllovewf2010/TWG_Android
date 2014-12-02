package com.modusgo.ubi.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.google.android.gms.maps.model.LatLng;
import com.modusgo.ubi.Alert;
import com.modusgo.ubi.DiagnosticsFragment.Maintenance;
import com.modusgo.ubi.DiagnosticsFragment.WarrantyInformation;
import com.modusgo.ubi.DiagnosticsTroubleCode;
import com.modusgo.ubi.LimitsFragment.Limit;
import com.modusgo.ubi.Recall;
import com.modusgo.ubi.ScoreCirclesActivity.CirclesSection;
import com.modusgo.ubi.ScoreFragment.MonthStats;
import com.modusgo.ubi.ScorePieChartActivity.PieChartTab;
import com.modusgo.ubi.Trip;
import com.modusgo.ubi.Trip.Point;
import com.modusgo.ubi.Vehicle;
import com.modusgo.ubi.db.AlertContract.AlertEntry;
import com.modusgo.ubi.db.DDEventContract.DDEventEntry;
import com.modusgo.ubi.db.DTCContract.DTCEntry;
import com.modusgo.ubi.db.LimitsContract.LimitsEntry;
import com.modusgo.ubi.db.MaintenanceContract.MaintenanceEntry;
import com.modusgo.ubi.db.PointContract.PointEntry;
import com.modusgo.ubi.db.RecallContract.RecallEntry;
import com.modusgo.ubi.db.RouteContract.RouteEntry;
import com.modusgo.ubi.db.ScoreCirclesContract.ScoreCirclesEntry;
import com.modusgo.ubi.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.ubi.db.ScorePercentageContract.ScorePercentageEntry;
import com.modusgo.ubi.db.ScorePieChartContract.ScorePieChartEntry;
import com.modusgo.ubi.db.SpeedingRouteContract.SpeedingRouteEntry;
import com.modusgo.ubi.db.TripContract.TripEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.db.WarrantyInfoContract.WarrantyInfoEntry;

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
	    VehicleEntry.COLUMN_NAME_CAR_DTC_COUNT + INT_TYPE + COMMA_SEP +
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
	    VehicleEntry.COLUMN_NAME_ODOMETER + INT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_LAST_CHECKUP + TEXT_TYPE + COMMA_SEP +
	    VehicleEntry.COLUMN_NAME_CAR_CHECKUP_STATUS + TEXT_TYPE + " ); ",
	    
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
		    TripEntry.COLUMN_NAME_FUEL + INT_TYPE + COMMA_SEP +
		    TripEntry.COLUMN_NAME_FUEL_UNIT + TEXT_TYPE + COMMA_SEP +
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
	
		    "CREATE TABLE " + ScorePieChartEntry.TABLE_NAME + " (" +
		    ScorePieChartEntry._ID + " INTEGER PRIMARY KEY," +
		    ScorePieChartEntry.COLUMN_NAME_VEHICLE_ID + INT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_TAB + TEXT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_VALUE + FLOAT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
		    ScorePieChartEntry.COLUMN_NAME_SUBTITLE + TEXT_TYPE + " ); ",
	
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
			MaintenanceEntry.COLUMN_NAME_PRICE + TEXT_TYPE + " ); ",
		    
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
		    
		    "CREATE TABLE " + DDEventEntry.TABLE_NAME + " (" +
		    DDEventEntry._ID + " INTEGER PRIMARY KEY," +
		    DDEventEntry.COLUMN_NAME_EVENT + TEXT_TYPE + COMMA_SEP +
			DDEventEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE + " )"};

	private static final String[] SQL_DELETE_ENTRIES = new String[]{
	"DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + SpeedingRouteEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + PointEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + ScoreGraphEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + ScorePercentageEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + ScorePieChartEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + ScoreCirclesEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + DTCEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + RecallEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + MaintenanceEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + WarrantyInfoEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + LimitsEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + AlertEntry.TABLE_NAME,
	"DROP TABLE IF EXISTS " + DDEventEntry.TABLE_NAME};
	
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 37;
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
	
	public ArrayList<Vehicle> getVehiclesShort(){
		SQLiteDatabase db = sInstance.getReadableDatabase();
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
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO}, 
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
				drivers.add(d);
				
				c.moveToNext();
			}
		}
		c.close();
		return drivers;
	}
	
	public Vehicle getVehicleShort(long id){
		SQLiteDatabase db = sInstance.getReadableDatabase();
		Cursor c = db.query(VehicleEntry.TABLE_NAME, 
				new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO,
				VehicleEntry.COLUMN_NAME_LATITUDE,
				VehicleEntry.COLUMN_NAME_LONGITUDE}, 
				VehicleEntry._ID+" = ?", new String[]{Long.toString(id)}, null, null, null);
		
		Vehicle d = new Vehicle();
		
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
	
	public Vehicle getVehicle(long id){
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
				VehicleEntry.COLUMN_NAME_CAR_FUEL_UNIT,
				VehicleEntry.COLUMN_NAME_CAR_DTC_COUNT,
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
			    VehicleEntry.COLUMN_NAME_ODOMETER,
			    VehicleEntry.COLUMN_NAME_CAR_LAST_CHECKUP,
			    VehicleEntry.COLUMN_NAME_CAR_CHECKUP_STATUS}, 
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
			v.carDTCCount = c.getInt(10);
			v.lastTripDate = c.getString(11);
			v.lastTripId = c.getLong(12);
			v.alerts = c.getInt(13);
			v.latitude = c.getDouble(14);
			v.longitude = c.getDouble(15);
			v.address = c.getString(16);
			v.grade = c.getString(17);
			v.score = c.getInt(18);
			v.totalTripsCount = c.getInt(19);
			v.totalDrivingTime = c.getInt(20);
			v.totalDistance = c.getDouble(21);
			v.totalBraking = c.getInt(22);
			v.totalAcceleration = c.getInt(23);
			v.totalSpeeding = c.getInt(24);
			v.totalSpeedingDistance = c.getDouble(25);
			v.odometer = c.getInt(26);
			v.carLastCheckup = c.getString(27);
			v.carCheckupStatus = c.getString(28);
				
		}
		c.close();
		db.close();
		return v;
	}
	
	public void saveVehicle(Vehicle v){
		ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
		vehicles.add(v);
		saveVehicles(vehicles, false);
	}
	
	public void saveVehicles(ArrayList<Vehicle> drivers, boolean removeVehicles){
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
					+ VehicleEntry.COLUMN_NAME_CAR_FUEL_UNIT +","
					+ VehicleEntry.COLUMN_NAME_CAR_DTC_COUNT +","
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
					+ VehicleEntry.COLUMN_NAME_ODOMETER +","
					+ VehicleEntry.COLUMN_NAME_CAR_LAST_CHECKUP +","
					+ VehicleEntry.COLUMN_NAME_CAR_CHECKUP_STATUS +""
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
			
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
		    	statement.bindLong(11, vehicle.carDTCCount);
		    	statement.bindDouble(12, vehicle.latitude);
		    	statement.bindDouble(13, vehicle.longitude);
		    	statement.bindString(14, vehicle.address);
		    	statement.bindString(15, vehicle.lastTripDate);
		    	statement.bindLong(16, vehicle.lastTripId);
		    	statement.bindLong(17, vehicle.score);
		    	statement.bindString(18, vehicle.grade);
		    	statement.bindLong(19, vehicle.totalTripsCount);
		    	statement.bindLong(20, vehicle.totalDrivingTime);
		    	statement.bindDouble(21, vehicle.totalDistance);
		    	statement.bindLong(22, vehicle.totalBraking);
		    	statement.bindLong(23, vehicle.totalAcceleration);
		    	statement.bindLong(24, vehicle.totalSpeeding);
		    	statement.bindDouble(25, vehicle.totalSpeedingDistance);
		    	statement.bindLong(26, vehicle.alerts);
		    	statement.bindLong(27, vehicle.odometer);
		    	statement.bindString(28, vehicle.carLastCheckup);
		    	statement.bindString(29, vehicle.carCheckupStatus);
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
		
		database.close();
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
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
					+ TripEntry.COLUMN_NAME_VIEWED_AT +","
					+ TripEntry.COLUMN_NAME_UPDATED_AT +","
					+ TripEntry.COLUMN_NAME_HIDDEN +""
					+ ") VALUES (?,?,?,?,?,?," +
					"(SELECT IFNULL(NULLIF((SELECT "+TripEntry.COLUMN_NAME_AVG_SPEED+" FROM trips WHERE "+TripEntry._ID+" IS ?),0),?))," +
					"(SELECT IFNULL(NULLIF((SELECT "+TripEntry.COLUMN_NAME_MAX_SPEED+" FROM trips WHERE "+TripEntry._ID+" IS ?),0),?)),?,?,?," +
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
		    	statement.bindLong(12, trip.fuelLevel);
		    	statement.bindString(13, trip.fuelUnit);
		    	statement.bindDouble(14, trip.id);
		    	statement.bindString(15, trip.viewedAt);
		    	statement.bindString(16, trip.updatedAt);
		    	statement.bindDouble(17, trip.id);
		    	statement.bindLong(18, trip.hidden ? 1 : 0);
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
	
	public void saveSpeedingRoute(long tripId, ArrayList<LatLng> route){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && route!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+SpeedingRouteEntry.TABLE_NAME+" WHERE "+SpeedingRouteEntry.COLUMN_NAME_TRIP_ID+" = "+tripId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();	
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ SpeedingRouteEntry.TABLE_NAME +" ("
					+ SpeedingRouteEntry.COLUMN_NAME_TRIP_ID +","
					+ SpeedingRouteEntry.COLUMN_NAME_LATITUDE +","
					+ SpeedingRouteEntry.COLUMN_NAME_LONGITUDE
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
		
		database.close();
	}
	
	public void saveScoreGraph(long driverId, MonthStats[] yearStats){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void saveScorePercentage(long driverId, LinkedHashMap<String, Integer> valuesMap){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void saveScorePieCharts(long driverId, ArrayList<PieChartTab> tabs){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void saveDTCs(long vehicleId, ArrayList<DiagnosticsTroubleCode> dtcs){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void saveRecalls(long vehicleId, ArrayList<Recall> recalls){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && recalls!=null){
			SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+RecallEntry.TABLE_NAME+" WHERE "+RecallEntry.COLUMN_NAME_VEHICLE_ID+" = "+vehicleId);
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
			
			String sql = "INSERT INTO "+ RecallEntry.TABLE_NAME +" ("
					+ RecallEntry.COLUMN_NAME_VEHICLE_ID +","
					+ RecallEntry.COLUMN_NAME_CONSEQUENCE +","
					+ RecallEntry.COLUMN_NAME_CORRECTIVE_ACTION +","
					+ RecallEntry.COLUMN_NAME_CREATED_AT +","
					+ RecallEntry.COLUMN_NAME_DEFECT_DESCRIPTION +","
					+ RecallEntry.COLUMN_NAME_DESCRIPTION +","
					+ RecallEntry.COLUMN_NAME_RECALL_ID
					+ ") VALUES (?,?,?,?,?,?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    for (Recall recall : recalls) {
		    	statement.clearBindings();
			    statement.bindLong(1, vehicleId);
			    statement.bindString(2, recall.consequence);
			    statement.bindString(3, recall.corrective_action);
			    statement.bindString(4, recall.created_at);
			    statement.bindString(5, recall.defect_description);
			    statement.bindString(6, recall.description);
			    statement.bindString(7, recall.recall_id);
			    statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		database.close();
	}
	
	public void saveMaintenances(long vehicleId, ArrayList<Maintenance> maintenances){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
			    statement.bindString(7, m.price);
			    statement.execute();
			}
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		
		database.close();
	}
	
	public void deleteMaintenance(long vehicleId, long maintenanceId){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void saveWarrantyInformation(long vehicleId, ArrayList<WarrantyInformation> warrantyInfo){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void saveLimits(long driverId, ArrayList<Limit> limits){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void saveAlert(long vehicleId, Alert alert){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
			
			database.close();
		}
	}
	
	public void saveAlerts(long vehicleId, ArrayList<Alert> alerts){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void deleteAlert(long vehicleId, long alertId){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void deleteAllAlerts(long vehicleId){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
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
		
		database.close();
	}
	
	public void saveDDEvent(String event, String timestamp){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null){
			
			String sql = "INSERT INTO "+ DDEventEntry.TABLE_NAME +" ("
					+ DDEventEntry.COLUMN_NAME_EVENT +","
					+ DDEventEntry.COLUMN_NAME_TIMESTAMP
					+ ") VALUES (?,?);";
			
			SQLiteStatement statement = database.compileStatement(sql);
		    database.beginTransaction();
		    
		    statement.clearBindings();
		    statement.bindString(1, event);
		    statement.bindString(2, timestamp);
		    statement.execute();
		    
		    database.setTransactionSuccessful();	
		    database.endTransaction();
		    statement.close();
		}
		database.close();
	}
	
	public void deleteDDEvents(ArrayList<Long> ids){
		SQLiteDatabase database = sInstance.getWritableDatabase();
		
		if(database!=null && ids!=null && ids.size()>0){
			
			String sIds = "";
			int idsSize = ids.size();
		    for (int i = 0; i < idsSize; i++) {
		    	sIds+= i!=idsSize-1 ? ids.get(i)+"," : ids.get(i);
		    }
			
		    SQLiteStatement removeStatement = database.compileStatement("DELETE FROM "+DDEventEntry.TABLE_NAME+" WHERE "+DDEventEntry._ID+" IN (" + sIds + ")");
		    database.beginTransaction();
		    removeStatement.clearBindings();
	        removeStatement.execute();
	        database.setTransactionSuccessful();
		    database.endTransaction();
		    removeStatement.close();
		    
		}
		database.close();
	}
	
}
