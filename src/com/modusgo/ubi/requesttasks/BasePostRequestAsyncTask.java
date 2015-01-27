package com.modusgo.ubi.requesttasks;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.utils.RequestPost;
import com.modusgo.ubi.utils.Utils;

public class BasePostRequestAsyncTask extends BaseRequestAsyncTask{
	
	private final static String TAG = "BasePostRequestAsyncTask";
	
	public BasePostRequestAsyncTask(Context context) {
		super(context);
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		Log.d(TAG, "URL = '"+Constants.API_BASE_URL_PREFIX+/*prefs.getString(Constants.PREF_CLIENT_ID, "")+*/Constants.API_BASE_URL_POSTFIX+params[0]+"'");
		HttpResponse result = new RequestPost(Constants.API_BASE_URL_PREFIX+/*prefs.getString(Constants.PREF_CLIENT_ID, "")+*/Constants.API_BASE_URL_POSTFIX+params[0], requestParams).execute();
		try{
			status = result.getStatusLine().getStatusCode();
			message = "Error "+result.getStatusLine().getStatusCode()+": "+result.getStatusLine().getReasonPhrase();
		}
		catch(NullPointerException e){
			e.printStackTrace();
			status = 0;
			message = "Check your internet connection";
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
		return Utils.getJSONObjectFromHttpResponse(result);
	}

}
