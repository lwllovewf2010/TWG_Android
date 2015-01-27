package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class LimitsContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public LimitsContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class LimitsEntry implements BaseColumns {
		public static final String TABLE_NAME = "limits";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_KEY = "key";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_VALUE = "value";
		public static final String COLUMN_NAME_MIN_VALUE = "min_value";
		public static final String COLUMN_NAME_MAX_VALUE = "max_value";
		public static final String COLUMN_NAME_STEP = "step";
		public static final String COLUMN_NAME_ACTIVE = "active";
	}
}