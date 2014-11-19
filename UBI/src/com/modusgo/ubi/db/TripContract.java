package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class TripContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public TripContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class TripEntry implements BaseColumns {
		public static final String TABLE_NAME = "trips";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_EVENTS_COUNT = "events_count";
		public static final String COLUMN_NAME_START_TIME = "start_time";
		public static final String COLUMN_NAME_END_TIME = "end_time";
		public static final String COLUMN_NAME_DISTANCE = "distance";
		public static final String COLUMN_NAME_AVG_SPEED = "avg_speed";
		public static final String COLUMN_NAME_MAX_SPEED = "max_speed";
		public static final String COLUMN_NAME_GRADE = "grade";
        public static final String COLUMN_NAME_FUEL = "fuel_level";
        public static final String COLUMN_NAME_FUEL_UNIT = "fuel_unit";
        public static final String COLUMN_NAME_VIEWED_AT = "viewed_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
        public static final String COLUMN_NAME_HIDDEN = "hidden";
	}
}