/**
 * 
 */
package com.modusgo.twg.requesttasks;

import com.modusgo.templates.UpdateCallback;
import com.modusgo.twg.Constants;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.MaintenanceContract.MaintenanceEntry;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

/**
 * @author yaturner
 *
 */
public class UpdateMaintenanceCountdownTask extends AsyncTask<Void, Void, Void>
{
	private final static String TAG = "UpdateMaintenanceCountdownTask";

	SharedPreferences prefs;
	Context context = null;
	UpdateCallback callback = null;

	public UpdateMaintenanceCountdownTask(Context context, UpdateCallback callback)
	{
		this.context = context;
		this.callback = callback;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
	}


	@Override
	protected Void doInBackground(Void... params)
	{
		Editor e = prefs.edit();
		DbHelper dbHelper = DbHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.openDatabase();
		Cursor c = null;
		int deltaDistance = prefs.getInt(Constants.PREF_DELTA_DISTANCE, 0);
		int initCountdowns = prefs.getInt(Constants.PREF_INIT_NAINTENANCE_COUNTDOWNS, 1);
		ContentValues cv = new ContentValues();

		c = db.query(MaintenanceEntry.TABLE_NAME, new String[]
		{ MaintenanceEntry._ID, MaintenanceEntry.COLUMN_NAME_CREATED_AT, MaintenanceEntry.COLUMN_NAME_DESCRIPTION,
				MaintenanceEntry.COLUMN_NAME_IMPORTANCE, MaintenanceEntry.COLUMN_NAME_MILEAGE,
				MaintenanceEntry.COLUMN_NAME_PRICE, MaintenanceEntry.COLUMN_NAME_COUNTDOWN },
				null, null, null, null,
				MaintenanceEntry.COLUMN_NAME_MILEAGE + " DESC");

		if(c.moveToFirst())
		{
			while(!c.isAfterLast())
			{
				int id = c.getInt(c.getColumnIndex(MaintenanceEntry._ID));
				if(initCountdowns == 1)
				{
					cv.put(MaintenanceEntry.COLUMN_NAME_COUNTDOWN,
							c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_MILEAGE)));
					db.update(MaintenanceEntry.TABLE_NAME, cv, "_id=" + id, null);
				} else if(deltaDistance > 0)
				{
					long countdown = c.getLong(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_COUNTDOWN));
					countdown = ((countdown - deltaDistance) < 0) ? 0 : (countdown - deltaDistance);
					cv.put(MaintenanceEntry.COLUMN_NAME_COUNTDOWN, countdown);
					db.update(MaintenanceEntry.TABLE_NAME, cv, "_id=" + id, null);

				}
				c.moveToNext();
			}
		}

		e.putLong(Constants.PREF_DELTA_DISTANCE, 0);
		e.putInt(Constants.PREF_INIT_NAINTENANCE_COUNTDOWNS, 0);
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		super.onPostExecute(null);
		callback.callback();
	}

}
