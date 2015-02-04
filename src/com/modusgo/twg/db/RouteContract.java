package com.modusgo.twg.db;

import android.provider.BaseColumns;

public class RouteContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public RouteContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class RouteEntry implements BaseColumns {
		public static final String TABLE_NAME = "routes";
		public static final String COLUMN_NAME_TRIP_ID = "trip_id";
		public static final String COLUMN_NAME_LATITUDE = "latitude";
		public static final String COLUMN_NAME_LONGITUDE = "longitude";
	}
}