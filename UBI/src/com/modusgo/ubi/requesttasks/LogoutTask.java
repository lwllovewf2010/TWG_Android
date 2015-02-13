package com.modusgo.ubi.requesttasks;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.TripsFragment;

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

}
