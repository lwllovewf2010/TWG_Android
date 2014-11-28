package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class SpeedingRouteContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public SpeedingRouteContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class SpeedingRouteEntry implements BaseColumns {
		public static final String TABLE_NAME = "speeding_routes";
		public static final String COLUMN_NAME_TRIP_ID = "trip_id";
		public static final String COLUMN_NAME_LATITUDE = "latitude";
		public static final String COLUMN_NAME_LONGITUDE = "longitude";
	}
}