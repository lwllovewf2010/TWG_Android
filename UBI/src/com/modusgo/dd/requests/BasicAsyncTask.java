package com.modusgo.dd.requests;

import android.os.AsyncTask;

public abstract class BasicAsyncTask extends AsyncTask<Void, String, String[]>{
	
	public static final String ERROR = "error";
	
	@Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        if(!result[0].equals(ERROR)){
        	onPostExecuteSuccess(result[0],result[1],result[2]);
        }
        else{
			onPostExecuteError(result[1],result[2]);
        }
    }
    
    protected void onPostExecuteSuccess(String arg0, String arg1, String arg2) {
    }
    
    protected void onPostExecuteError(String errorCode, String errorMessage) {
    }
}
