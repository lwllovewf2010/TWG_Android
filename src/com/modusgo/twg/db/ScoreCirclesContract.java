package com.modusgo.twg.db;

import android.provider.BaseColumns;

public class ScoreCirclesContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public ScoreCirclesContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class ScoreCirclesEntry implements BaseColumns {
		public static final String TABLE_NAME = "score_circles";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_TAB = "tab";
		public static final String COLUMN_NAME_SECTION = "section";
		public static final String COLUMN_NAME_MARK = "value";
		public static final String COLUMN_NAME_DISTANCE = "distance";
	}
}