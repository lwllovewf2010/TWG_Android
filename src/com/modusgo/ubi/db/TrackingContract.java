package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class TrackingContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public TrackingContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class TrackingEntry implements BaseColumns {
		public static final String TABLE_NAME = "tracking";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
		public static final String COLUMN_NAME_LATITUDE = "latitude";
		public static final String COLUMN_NAME_LONGITUDE = "longitude";
		public static final String COLUMN_NAME_ALTITUDE = "altitude";
		public static final String COLUMN_NAME_HEADING = "heading";
		public static final String COLUMN_NAME_HORIZONTAL_ACCURACY = "horizontal_accuracy";
		public static final String COLUMN_NAME_VERTICAL_ACCURACY = "vertical_accuracy";
		public static final String COLUMN_NAME_SATELITES = "satelites";
		public static final String COLUMN_NAME_FIX_STATUS = "fix_status";
		public static final String COLUMN_NAME_SPEED = "speed";
		public static final String COLUMN_NAME_EVENT = "event";
		public static final String COLUMN_NAME_RAW_DATA = "raw_data";
		public static final String COLUMN_NAME_BLOCKED = "blocked";
	}
}