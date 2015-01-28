/**
 * 
 */
package com.modusgo.ubi;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.modusgo.adapters.TWGInfoArrayAdapter;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.DTCContract.DTCEntry;
import com.modusgo.ubi.db.MaintenanceContract.MaintenanceEntry;
import com.modusgo.ubi.db.RecallContract.RecallEntry;
import com.modusgo.ubi.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.Maintenance;
import com.modusgo.ubi.utils.TWGListItem;
import com.modusgo.ubi.utils.Utils;
import com.modusgo.ubi.utils.TWGListItem.twg_list_item_type;

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
public class AlertsFragment extends Fragment
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

		new GetDiagnosticsTask(getActivity()).execute("vehicles/" + vehicle.id + "/diagnostics.json");

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
	}

	/**
	 * GetDiagnosticsTask
	 * 
	 * @author yaturner
	 *
	 */
	class GetDiagnosticsTask extends BaseRequestAsyncTask
	{

		public GetDiagnosticsTask(Context context)
		{
			super(context);
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			// lRefresh.setRefreshing(true);
		}

		@Override
		protected void onPostExecute(JSONObject result)
		{
			super.onPostExecute(result);
			// lRefresh.setRefreshing(false);
		}

		@Override
		protected JSONObject doInBackground(String... params)
		{
			requestParams.add(new BasicNameValuePair("vehicle_id", "" + vehicle.id));
			requestParams.add(new BasicNameValuePair("mileage", "" + vehicle.odometer));
			return super.doInBackground(params);
		}

		@Override
		protected void onError(String message)
		{
			// super.onError(message);
		}

		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException
		{
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			System.out.println(responseJSON);

			if(responseJSON.has("diagnostics"))
			{
				JSONObject diagnosticsJSON = responseJSON.getJSONObject("diagnostics");

				// Editor e = prefs.edit();
				// e.putString(Constants.PREF_DIAGNOSTICS_CHECKUP_DATE+vehicle.id,
				// Utils.fixTimezoneZ(diagnosticsJSON.optString("last_checkup")));
				// System.out.println(Constants.PREF_DIAGNOSTICS_CHECKUP_DATE+vehicle.id+" = "+Utils.fixTimezoneZ(diagnosticsJSON.optString("last_checkup")));
				// e.putString(Constants.PREF_DIAGNOSTICS_STATUS+vehicle.id,
				// diagnosticsJSON.optString("checkup_status",ERROR_STATUS_MESSAGE));
				// e.commit();

				if(diagnosticsJSON.has("diagnostics_trouble_codes"))
				{
					Object dtcsObject = diagnosticsJSON.get("diagnostics_trouble_codes");
					if(dtcsObject instanceof JSONArray)
					{
						JSONArray dtcsJSON = (JSONArray) dtcsObject;
						ArrayList<DiagnosticsTroubleCode> dtcs = new ArrayList<DiagnosticsTroubleCode>();
						for(int i = 0; i < dtcsJSON.length(); i++)
						{

							JSONObject dtc = dtcsJSON.getJSONObject(i);

							dtcs.add(new DiagnosticsTroubleCode(dtc.optString("code"), dtc.optString("conditions"),
									Utils.fixTimezoneZ(dtc.optString("created_at")), dtc.optString("description"), dtc
											.optString("details"), dtc.optString("full_description"), dtc
											.optString("importance_text"), dtc.optString("labor_cost"), dtc
											.optString("labor_hours"), dtc.optString("parts"), dtc
											.optString("parts_cost"), dtc.optString("total_cost")));
						}
						vehicle.carDTCCount = dtcs.size();

						dbHelper.saveDTCs(vehicle.id, dtcs);
					}
				}

				if(diagnosticsJSON.has("recall_updates"))
				{
					Object recallsObject = diagnosticsJSON.get("recall_updates");
					if(recallsObject instanceof JSONArray)
					{
						JSONArray recallsJSON = (JSONArray) recallsObject;
						ArrayList<Recall> recalls = new ArrayList<Recall>();
						for(int i = 0; i < recallsJSON.length(); i++)
						{

							JSONObject recall = recallsJSON.getJSONObject(i);

							recalls.add(new Recall(recall.optLong("id"), recall.optString("consequence"), recall
									.optString("corrective_action"),
									Utils.fixTimezoneZ(recall.optString("recall_date")), recall
											.optString("defect_description"), recall.optString("description"), recall
											.optString("recall_id")));
						}
						dbHelper.saveRecalls(vehicle.id, recalls);
					}
				}

				if(diagnosticsJSON.has("vehicle_maintenances"))
				{
					Object maintenancesObject = diagnosticsJSON.get("vehicle_maintenances");
					if(maintenancesObject instanceof JSONArray)
					{
						JSONArray maintenancesJSON = (JSONArray) maintenancesObject;
						ArrayList<Maintenance> maintenances = new ArrayList<Maintenance>();
						for(int i = 0; i < maintenancesJSON.length(); i++)
						{

							JSONObject maintenance = maintenancesJSON.getJSONObject(i);

							maintenances.add(new Maintenance(maintenance.optLong("id"), Utils.fixTimezoneZ(maintenance
									.optString("created_at")), maintenance.optString("description"), maintenance
									.optString("importance"), Integer.toString(maintenance.optInt("mileage")),
									(float) maintenance.optDouble("price")));
						}
						dbHelper.saveMaintenances(vehicle.id, maintenances);
					}
				}

				// if(diagnosticsJSON.has("diagnostics_warranty_informations")){
				// Object warrantyInformationsObject =
				// diagnosticsJSON.get("diagnostics_warranty_informations");
				// if(warrantyInformationsObject instanceof JSONArray){
				// JSONArray warrantyInformationsJSON = (JSONArray)
				// warrantyInformationsObject;
				// ArrayList<WarrantyInformation> warrantyInformation = new
				// ArrayList<WarrantyInformation>();
				// for (int i = 0; i < warrantyInformationsJSON.length(); i++) {
				//
				// JSONObject wInfoJSON =
				// warrantyInformationsJSON.getJSONObject(i);
				//
				// warrantyInformation.add(new WarrantyInformation(
				// wInfoJSON.optString("created_at"),
				// wInfoJSON.optString("description"),
				// wInfoJSON.optString("mileage")));
				// }
				// dbHelper.saveWarrantyInformation(vehicle.id,
				// warrantyInformation);
				// }
				// }

				vehicle.carCheckupStatus = diagnosticsJSON.optString("checkup_status");
				vehicle.carLastCheckup = diagnosticsJSON.optString("last_checkup");
				dbHelper.saveVehicle(vehicle);

				updateInfo();
			}
			dbHelper.close();

			super.onSuccess(responseJSON);
		}
	}
}
