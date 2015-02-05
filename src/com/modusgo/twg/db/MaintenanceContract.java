package com.modusgo.twg.db;

import android.provider.BaseColumns;

public class MaintenanceContract
{
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public MaintenanceContract()
	{
	}

	/* Inner class that defines the table contents */
	public static abstract class MaintenanceEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "maintenances";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_CREATED_AT = "created_at";
		public static final String COLUMN_NAME_DESCRIPTION = "description";
		public static final String COLUMN_NAME_IMPORTANCE = "importance";
		public static final String COLUMN_NAME_MILEAGE = "mileage";
		public static final String COLUMN_NAME_PRICE = "price";
		public static final String COLUMN_NAME_COUNTDOWN = "countdown";
	}
}