package com.modusgo.ubi.requesttasks;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import android.content.Context;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.utils.RequestPost;
import com.modusgo.ubi.utils.Utils;

public class BasePostRequestAsyncTask extends BaseRequestAsyncTask{
	
	public BasePostRequestAsyncTask(Context context) {
		super(context);
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		HttpResponse result = new RequestPost(Constants.API_BASE_URL_PREFIX+prefs.getString(Constants.PREF_CLIENT_ID, "")+Constants.API_BASE_URL_POSTFIX+params[0], requestParams).execute();
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
