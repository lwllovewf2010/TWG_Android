package com.modusgo.ubi.requesttasks;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class LogoutTask extends BasePostRequestAsyncTask{

	public LogoutTask(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	@Override
	protected JSONObject doInBackground(String... params) {
		return super.doInBackground(params);
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		super.onPostExecute(result);
	}
	
	@Override
	protected void onSuccess(JSONObject responseJSON) throws JSONException {
		System.out.println(responseJSON);
	}
	
	@Override
	protected void onError(String message) {
		System.out.println("Error logout");
	}
	
	@Override
	protected void onError401() {
		//Do nothing
	}

}
