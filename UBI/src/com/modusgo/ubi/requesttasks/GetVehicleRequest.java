package com.modusgo.ubi.requesttasks;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.modusgo.ubi.Vehicle;
import com.modusgo.ubi.db.DbHelper;

public class GetVehicleRequest extends BaseRequestAsyncTask{

	public GetVehicleRequest(Context context) {
		super(context);
	}
	
	@Override
	protected void onError(String message) {
		//Do nothing
	}

	@Override
	protected JSONObject doInBackground(String... params) {			
		return super.doInBackground(params);
	}
	
	@Override
	protected void onSuccess(JSONObject responseJSON) throws JSONException {
		System.out.println(responseJSON);
		
		JSONObject vehicleJSON = responseJSON.getJSONObject("vehicle");
		
		Vehicle vehicle = Vehicle.fromJSON(context, vehicleJSON);
		DbHelper dbHelper = DbHelper.getInstance(context);
		dbHelper.saveVehicle(vehicle);
		dbHelper.close();
		
		super.onSuccess(responseJSON);
	}
}
