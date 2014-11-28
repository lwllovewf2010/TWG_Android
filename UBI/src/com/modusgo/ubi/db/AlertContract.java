package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class AlertContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public AlertContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class AlertEntry implements BaseColumns {
		public static final String TABLE_NAME = "alerts";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_TRIP_ID = "trip_id";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_DESCRIPTION = "description";
		public static final String COLUMN_NAME_LATITUDE = "latitude";
		public static final String COLUMN_NAME_LONGITUDE = "longitude";
		public static final String COLUMN_NAME_SEEN_AT = "seen_at";
		public static final String COLUMN_NAME_ADDRESS = "address";
	}
}