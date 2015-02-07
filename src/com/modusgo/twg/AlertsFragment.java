/**
 * 
 */
package com.modusgo.twg;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.modusgo.adapters.TWGInfoArrayAdapter;
import com.modusgo.templates.UpdateCallback;
import com.modusgo.twg.R;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.DTCContract.DTCEntry;
import com.modusgo.twg.db.MaintenanceContract.MaintenanceEntry;
import com.modusgo.twg.db.RecallContract.RecallEntry;
import com.modusgo.twg.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.twg.requesttasks.BaseRequestAsyncTask;
import com.modusgo.twg.requesttasks.GetDiagnosticsTask;
import com.modusgo.twg.utils.Maintenance;
import com.modusgo.twg.utils.Recall;
import com.modusgo.twg.utils.TWGListItem;
import com.modusgo.twg.utils.Utils;
import com.modusgo.twg.utils.Vehicle;
import com.modusgo.twg.utils.TWGListItem.twg_list_item_type;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author yaturner
 *
 */
public class AlertsFragment extends Fragment implements UpdateCallback
{

	private Vehicle vehicle;
	private SharedPreferences prefs;
	private SwipeRefreshLayout lRefresh;
	private DbHelper dbHelper = null;
	private SQLiteDatabase db = null;
	private Cursor c = null;
	private MainActivity main = null;
	View rootView = null;
	ListView infoList = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		main = (MainActivity) getActivity();
		rootView = inflater.inflate(R.layout.alerts_fragment, container, false);
		main.setActionBarTitle("Alerts");
		prefs = PreferenceManager.getDefaultSharedPreferences(main);
		vehicle = ((DriverActivity) getActivity()).vehicle;

		infoList = (ListView) rootView.findViewById(R.id.alerts_info_list);

		dbHelper = DbHelper.getInstance(getActivity());
		db = dbHelper.openDatabase();
		c = null;

		main.showBusyDialog(R.string.GatheringDiagnosticInformation);

		new GetDiagnosticsTask(getActivity(), this, vehicle).execute("vehicles/" + vehicle.id + "/diagnostics.json");

		return rootView;

	}

	protected void updateInfo()
	{
		main.hideBusyDialog();

		// ------------------------------------------DTC---------------------------------
		Cursor c = db.query(DTCEntry.TABLE_NAME, new String[]
		{ DTCEntry.COLUMN_NAME_CODE, DTCEntry.COLUMN_NAME_CONDITIONS, DTCEntry.COLUMN_NAME_CREATED_AT,
				DTCEntry.COLUMN_NAME_DESCRIPTION, DTCEntry.COLUMN_NAME_DETAILS, DTCEntry.COLUMN_NAME_FULL_DESCRIPTION,
				DTCEntry.COLUMN_NAME_IMPORTANCE, DTCEntry.COLUMN_NAME_LABOR_COST, DTCEntry.COLUMN_NAME_LABOR_HOURS,
				DTCEntry.COLUMN_NAME_PARTS, DTCEntry.COLUMN_NAME_PARTS_COST, DTCEntry.COLUMN_NAME_TOTAL_COST },
				ScoreGraphEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, null);

		final ArrayList<TWGListItem> info_list = new ArrayList<TWGListItem>();

		if(c.moveToFirst())
		{
			info_list.add(new TWGListItem(twg_list_item_type.li_alert_hdr, null));

			while(!c.isAfterLast())
			{
				final DiagnosticsTroubleCode dtc = new DiagnosticsTroubleCode(c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_CODE)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_CONDITIONS)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_CREATED_AT)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_DESCRIPTION)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_DETAILS)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_FULL_DESCRIPTION)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_IMPORTANCE)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_LABOR_COST)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_LABOR_HOURS)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_PARTS)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_PARTS_COST)), c.getString(c
						.getColumnIndex(DTCEntry.COLUMN_NAME_TOTAL_COST)));

				info_list.add(new TWGListItem(twg_list_item_type.li_alert_info, dtc));
				c.moveToNext();
			}
		}
		c.close();
		// ------------------------------------------ Recall Entry--------------------------------
		c = db.query(RecallEntry.TABLE_NAME, new String[]
		{ RecallEntry._ID, RecallEntry.COLUMN_NAME_CONSEQUENCE, RecallEntry.COLUMN_NAME_CORRECTIVE_ACTION,
				RecallEntry.COLUMN_NAME_CREATED_AT, RecallEntry.COLUMN_NAME_DEFECT_DESCRIPTION,
				RecallEntry.COLUMN_NAME_DESCRIPTION, RecallEntry.COLUMN_NAME_RECALL_ID },
				RecallEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, null);

		if(c.moveToFirst())
		{
			TWGListItem item = new TWGListItem(twg_list_item_type.li_recall_hdr, null);
			info_list.add(item);
			while(!c.isAfterLast())
			{
				final Recall recall = new Recall(c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
						c.getString(4), c.getString(5), c.getString(6));
				info_list.add(new TWGListItem(twg_list_item_type.li_recall_info, recall));
				c.moveToNext();
			}
		}
		c.close();

		final TWGInfoArrayAdapter info_adapter = new TWGInfoArrayAdapter(getActivity(), R.layout.twg_info_list_item,
				info_list);
		infoList.setAdapter(info_adapter);

		infoList.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				TWGListItem item = (TWGListItem) view.getTag();
				switch (item.type)
				{
				case li_recall_info:
					Recall recall = (Recall) item.value;
					Intent i = new Intent(getActivity(), RecallActivity.class);
					i.putExtra(RecallActivity.EXTRA_RECALL, recall);
					startActivity(i);
					break;
				case li_alert_info:
					// showAlertDetailView(item);
					break;
				}
			}
		});
	

	//-----------------Maintenances Due-----------------------
	///---------less than 100 miles left
	// --------------------------------Maintenances----------------------------
	c = db.query(MaintenanceEntry.TABLE_NAME, new String[]
	{ 
			MaintenanceEntry._ID, 
			MaintenanceEntry.COLUMN_NAME_CREATED_AT, 
			MaintenanceEntry.COLUMN_NAME_DESCRIPTION,
			MaintenanceEntry.COLUMN_NAME_IMPORTANCE, 
			MaintenanceEntry.COLUMN_NAME_MILEAGE,
			MaintenanceEntry.COLUMN_NAME_PRICE,
			MaintenanceEntry.COLUMN_NAME_COUNTDOWN }, 
			null, null, null, null, MaintenanceEntry.COLUMN_NAME_MILEAGE + " DESC");

//	info_list = new ArrayList<TWGListItem>();

	int demoCount = 0;
	if(c.moveToFirst())
	{
		while(!c.isAfterLast())
		{
			final Maintenance maintenance = new Maintenance(
					c.getLong(c.getColumnIndex(MaintenanceEntry._ID)),
					c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_CREATED_AT)), 
					c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_DESCRIPTION)), 
					c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_IMPORTANCE)), 
					c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_MILEAGE)), 
					c.getFloat(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_PRICE)), 
					c.getInt(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_COUNTDOWN)));
			/************************** DEBUGGING ONLY ***************************/
			if(demoCount < 2)
			{
				demoCount++;
				info_list.add(new TWGListItem(twg_list_item_type.li_service_due_item, maintenance));
			}
			/************************** DEBUGGING ONLY ***************************/
/////Release			info_list.add(new TWGListItem(twg_list_item_type.li_service_item, maintenance));
			c.moveToNext();
		}
//		info_adapter = new TWGInfoArrayAdapter(getActivity(), R.layout.twg_info_list_item, info_list);
		infoList.setAdapter(info_adapter);
	}
	c.close();
	}
	@Override
	public void callback()
	{
		updateInfo();
	}

}