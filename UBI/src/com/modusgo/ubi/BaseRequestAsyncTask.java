package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.modusgo.ubi.utils.RequestGet;
import com.modusgo.ubi.utils.Utils;

public class BaseRequestAsyncTask extends AsyncTask<String, Void, JSONObject>{

	Context context;
	SharedPreferences prefs;
	int status;
	String message;
	String baseUrl;
	
	protected List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
	
	public BaseRequestAsyncTask(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		baseUrl = Constants.API_BASE_URL_PREFIX+prefs.getString(Constants.PREF_CLIENT_ID, "")+Constants.API_BASE_URL_POSTFIX;

        requestParams.add(new BasicNameValuePair("auth_key", prefs.getString(Constants.PREF_AUTH_KEY, "")));
	}
	
	@Override
	protected JSONObject doInBackground(String... params) {
		HttpResponse result = new RequestGet(baseUrl+(params!=null && params.length>0 ? params[0] : ""), requestParams).execute();
		
		try{
			status = result.getStatusLine().getStatusCode();
			message = "Error "+result.getStatusLine().getStatusCode()+": "+result.getStatusLine().getReasonPhrase();
		}
		catch(NullPointerException e){
			e.printStackTrace();
			status = 0;
			message = "Connection error";
			return null;
		}
		return Utils.getJSONObjectFromHttpResponse(result);
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		if(status>=200 && status<300){
			try {
				onSuccess(result);
			} catch (JSONException e) {
				e.printStackTrace();
				onError("Error parsing data");
			}
		}
		else if(status==401){
			onError401();
		}
		else{
			onError(message);
		}

		super.onPostExecute(result);
	}
	
	protected void onError401(){
		prefs.edit().putString(Constants.PREF_AUTH_KEY, "").commit();
		Intent intent = new Intent(context, SignInActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		onError("Your session has expired.");
	}
	
	protected void onError(String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	protected void onSuccess(JSONObject responseJSON) throws JSONException {
	}

}
