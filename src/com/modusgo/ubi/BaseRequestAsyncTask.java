package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
	protected List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
	
	public BaseRequestAsyncTask(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

        requestParams.add(new BasicNameValuePair("auth_key", prefs.getString(Constants.PREF_AUTH_KEY, "")));
	}
	
	@Override
	protected JSONObject doInBackground(String... params) {
		HttpResponse result = new RequestGet(Constants.API_BASE_URL+params[0], requestParams).execute();
		status = result.getStatusLine().getStatusCode();
		message = "Error "+result.getStatusLine().getStatusCode()+": "+result.getStatusLine().getReasonPhrase();
		
		return Utils.getJSONObjectFromHttpResponse(result);
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		if(status>=200 && status<300){
			onSuccess(result);
		}
		else if(status==401){
			prefs.edit().putString(Constants.PREF_AUTH_KEY, "").commit();
			Intent intent = new Intent(context, SignInActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			Toast.makeText(context, "Your session has expired.", Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}

		super.onPostExecute(result);
	}
	
	protected void onSuccess(JSONObject responseJSON){
	}

}