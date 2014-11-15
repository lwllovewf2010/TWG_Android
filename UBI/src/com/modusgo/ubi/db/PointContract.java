package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class PointContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public PointContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class PointEntry implements BaseColumns {
		public static final String TABLE_NAME = "points";
		public static final String COLUMN_NAME_TRIP_ID = "trip_id";
		public static final String COLUMN_NAME_LATITUDE = "latitude";
		public static final String COLUMN_NAME_LONGITUDE = "longitude";
		public static final String COLUMN_NAME_EVENT = "event";
		public static final String COLUMN_NAME_TITLE = "title";
	}
}