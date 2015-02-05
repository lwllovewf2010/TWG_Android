package com.modusgo.twg.requesttasks;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.modusgo.templates.UpdateCallback;
import com.modusgo.twg.DiagnosticsTroubleCode;
import com.modusgo.twg.DriverActivity;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.utils.Maintenance;
import com.modusgo.twg.utils.Recall;
import com.modusgo.twg.utils.Utils;
import com.modusgo.twg.utils.Vehicle;

public class GetDiagnosticsTask extends BaseRequestAsyncTask
{
	private UpdateCallback callback = null;
	private Context context = null;
	SharedPreferences prefs = null;
	Vehicle vehicle = null;
	
	public GetDiagnosticsTask(Context context, UpdateCallback callback, Vehicle vehicle)
	{
		super(context);
		this.context = context;
		this.callback = callback;
		this.vehicle = vehicle;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

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
		requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
		requestParams.add(new BasicNameValuePair("mileage", ""+vehicle.odometer));
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
		DbHelper dbHelper = DbHelper.getInstance(context);
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

			callback.callback();
		}
		dbHelper.close();

		super.onSuccess(responseJSON);
	}
}
