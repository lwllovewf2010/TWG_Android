package com.modusgo.ubi;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.modusgo.demo.R;
import com.modusgo.ubi.utils.RequestGet;
import com.modusgo.ubi.utils.Utils;

public class InitActivity extends FragmentActivity {
    
	View layoutProgress;
	View layoutFields;
	EditText editClientId;
	
	SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init);
	    getActionBar().hide();
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(InitActivity.this);
	    
	    if(!prefs.getString(Constants.PREF_CLIENT_ID, "").equals("")){
	    	startActivity(new Intent(InitActivity.this, SignInActivity.class));
			finish();
	    }
	    
	    layoutFields = findViewById(R.id.llFields);
	    layoutProgress = findViewById(R.id.rlProgress);
	    
	    editClientId = (EditText)findViewById(R.id.editClientId);
	    
	    Button btnSubmit = (Button)findViewById(R.id.btnSubmit);
	    
	    ProgressBar pb = (ProgressBar)findViewById(R.id.progressLogging);
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    a.setDuration(1000);
	    pb.startAnimation(a);
	    
	    btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new LoginTask().execute();
			}
		});
	}
	
	public class MyAnimationListener implements AnimationListener {
	    
		View view;
	    boolean hideView;
	    
	    public MyAnimationListener(View v, boolean hideView) {
			view = v;
			this.hideView = hideView;
		}
	    
	    public void onAnimationEnd(Animation animation) {
	    	if(hideView)
	    		view.setVisibility(View.GONE);
	    	else
	    		view.setVisibility(View.VISIBLE);
	    }
	    
	    public void onAnimationRepeat(Animation animation) {
	    }
	    public void onAnimationStart(Animation animation) {
	    }
	}
	
	private Animation getFadeInAnmation(View v){
		Animation fadeIn = AnimationUtils.loadAnimation(InitActivity.this,android.R.anim.fade_in);
		fadeIn.setAnimationListener(new MyAnimationListener(v, false));
		return fadeIn;
	}
	
	private Animation getFadeOutAnmation(View v){
		Animation fadeOut = AnimationUtils.loadAnimation(InitActivity.this,android.R.anim.fade_out);
		fadeOut.setAnimationListener(new MyAnimationListener(v, true));
		return fadeOut;
	}
	
	class LoginTask extends AsyncTask<Void, Void, Boolean>{

		Animation fadeInProgress;
		Animation fadeOutProgress;
		Animation fadeInFields;
		Animation fadeOutFields;
		int status;
		String message = "";
		
		public LoginTask() {
			fadeInProgress = getFadeInAnmation(layoutProgress);
			fadeOutProgress = getFadeOutAnmation(layoutProgress);
			fadeInFields = getFadeInAnmation(layoutFields);
			fadeOutFields = getFadeOutAnmation(layoutFields);
		}
		
		@Override
		protected void onPreExecute() {
			layoutProgress.setVisibility(View.VISIBLE);
			layoutProgress.startAnimation(fadeInProgress);
			layoutFields.startAnimation(fadeOutFields);
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			String clientId = editClientId.getText().toString();
			
			HttpResponse result = new RequestGet(Constants.API_BASE_URL_PREFIX+clientId+Constants.API_BASE_URL_POSTFIX+"info").execute();
			if(result==null){
				status = 0;
				message = "Connection error";
				
				try {
					synchronized (this) {
						wait(1000);	
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				return false;
			}
			
	        try {
	        	if(status>=200 && status<300){
					JSONObject responseJSON = Utils.getJSONObjectFromHttpResponse(result);
					if(responseJSON.getString("status").equals("success")){
						prefs.edit().putString(Constants.PREF_CLIENT_ID, clientId).commit();
						return true;
					}
	        	}
	        	else{
	        		status = result.getStatusLine().getStatusCode();
			        message = "Error "+result.getStatusLine().getStatusCode()+": "+result.getStatusLine().getReasonPhrase();
			        return false;
	        	}
			} catch (Exception e) {
				status = 0;
				message = "Wrong Client ID";
				e.printStackTrace();
			}
	        
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				startActivity(new Intent(InitActivity.this, SignInActivity.class));
				finish();
			}
			else{
				layoutProgress.startAnimation(fadeOutProgress);
				layoutFields.startAnimation(fadeInFields);
				Toast.makeText(InitActivity.this, message, Toast.LENGTH_SHORT).show();
			}
			
			super.onPostExecute(result);
		}
	}
}
