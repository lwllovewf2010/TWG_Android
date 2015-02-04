package com.modusgo.twg.db;

import android.provider.BaseColumns;

public class RecallContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public RecallContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class RecallEntry implements BaseColumns {
		public static final String TABLE_NAME = "recalls";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_CONSEQUENCE = "consequence";
		public static final String COLUMN_NAME_CORRECTIVE_ACTION = "corrective_action";
		public static final String COLUMN_NAME_CREATED_AT = "created_at";
		public static final String COLUMN_NAME_DEFECT_DESCRIPTION = "defect_description";
		public static final String COLUMN_NAME_DESCRIPTION = "description";
		public static final String COLUMN_NAME_RECALL_ID = "recall_id";
	}
}