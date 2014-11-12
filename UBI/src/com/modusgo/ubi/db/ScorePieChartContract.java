package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class ScorePieChartContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public ScorePieChartContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class ScorePieChartEntry implements BaseColumns {
		public static final String TABLE_NAME = "score_piechart";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_TAB = "tab";
		public static final String COLUMN_NAME_VALUE = "value";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_SUBTITLE = "subtitle";
	}
}