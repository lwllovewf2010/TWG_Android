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

import com.modusgo.ubi.utils.Utils;

public class BaseRequestAsyncTask extends AsyncTask<Void, Void, HttpResponse>{

	Context context;
	SharedPreferences prefs;
	protected List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
	
	public BaseRequestAsyncTask(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

        requestParams.add(new BasicNameValuePair("auth_key", prefs.getString(Constants.PREF_AUTH_KEY, "")));
	}
	
	@Override
	protected HttpResponse doInBackground(Void... params) {
		return null;
	}
	
	@Override
	protected void onPostExecute(HttpResponse result) {
		int status = result.getStatusLine().getStatusCode();
		if(status>=200 && status<300){
			onSuccess(Utils.getJSONObjectFromHttpResponse(result));
		}
		else if(status==401){
			prefs.edit().putString(Constants.PREF_AUTH_KEY, "").commit();
			Intent intent = new Intent(context, SignInActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			Toast.makeText(context, "Your session has expired.", Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(context, "Error "+result.getStatusLine().getStatusCode()+": "+result.getStatusLine().getReasonPhrase(), Toast.LENGTH_SHORT).show();
		}

		super.onPostExecute(result);
	}
	
	protected void onSuccess(JSONObject responseJSON){
	}

}
