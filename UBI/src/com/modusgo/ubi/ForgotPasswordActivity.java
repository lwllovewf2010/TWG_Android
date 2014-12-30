package com.modusgo.ubi;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.farmers.ubi.R;
import com.modusgo.ubi.requesttasks.BasePostRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ForgotPasswordActivity extends FragmentActivity {
	
	View layoutProgress;
	View layoutFields;
	EditText editEmail;
	TextView tvMessage;
    
	SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);
	    getActionBar().hide();
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(ForgotPasswordActivity.this);
	    
	    ImageView imageBg = (ImageView) findViewById(R.id.imageBg);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();
    	ImageLoader.getInstance().displayImage(prefs.getString(Constants.PREF_BR_LOGIN_SCREEN_BG_IMAGE, ""), imageBg, options);
    	
//    	ImageView imageLogo = (ImageView) findViewById(R.id.imageLogo);
//    	DisplayImageOptions optionsLogo = new DisplayImageOptions.Builder()
//        .cacheInMemory(true)
//        .cacheOnDisk(true)
//        .build();
//    	ImageLoader.getInstance().displayImage(prefs.getString(Constants.PREF_BR_LOGIN_SCREEN_LOGO, ""), imageLogo, optionsLogo);
	    
	    layoutFields = findViewById(R.id.loginFields);
	    layoutProgress = findViewById(R.id.loginProgress);
	    
	    editEmail = (EditText) findViewById(R.id.email);
	    tvMessage = (TextView) findViewById(R.id.tvMessage);

	    Button btnSubmit = (Button)findViewById(R.id.btnSubmit);
	    btnSubmit.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, Constants.BUTTON_BG_COLOR)));
	    try{
	    	btnSubmit.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, Constants.BUTTON_TEXT_COLOR)));
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	    
	    Button btnBackToLogin = (Button) findViewById(R.id.btnBackToLogin);
	    btnBackToLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
			}
		});
		
	    ProgressBar pb = (ProgressBar)findViewById(R.id.progressLogging);
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    a.setDuration(1000);
	    pb.startAnimation(a);
	    
	    btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new RestorePasswordTask(ForgotPasswordActivity.this).execute("forgot.json");
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
	}
	
	@Override
	protected void onResume() {
		Utils.gaTrackScreen(this, "Frogot your password Screen");
		super.onResume();
	}
	
	class RestorePasswordTask extends BasePostRequestAsyncTask{
		
		Animation fadeInProgress;
		Animation fadeOutProgress;
		Animation fadeInFields;
		Animation fadeOutFields;
		int status;
		String message = "";
		
		
		public RestorePasswordTask(Context context) {
			super(context);
			fadeInProgress = com.modusgo.ubi.utils.AnimationUtils.getFadeInAnmation(ForgotPasswordActivity.this, layoutProgress);
			fadeOutProgress = com.modusgo.ubi.utils.AnimationUtils.getFadeOutAnmation(ForgotPasswordActivity.this, layoutProgress);
			fadeInFields = com.modusgo.ubi.utils.AnimationUtils.getFadeInAnmation(ForgotPasswordActivity.this, layoutFields);
			fadeOutFields = com.modusgo.ubi.utils.AnimationUtils.getFadeOutAnmation(ForgotPasswordActivity.this, layoutFields);
		}
		
		@Override
		protected void onPreExecute() {
			layoutProgress.setVisibility(View.VISIBLE);
			layoutProgress.startAnimation(fadeInProgress);
			layoutFields.startAnimation(fadeOutFields);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			layoutProgress.startAnimation(fadeOutProgress);
			layoutFields.startAnimation(fadeInFields);
			tvMessage.setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("email", editEmail.getText().toString()));
	        System.out.println(requestParams);
			
	        return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			String message = "";
			if(responseJSON.has("success_message")){
				message = responseJSON.optString("success_message");
				tvMessage.setBackgroundResource(R.drawable.rectanle_rounded_green);
			}
			else if(responseJSON.has("failure_message")){
				message = responseJSON.optString("failure_message");
				tvMessage.setBackgroundResource(R.drawable.rectanle_rounded_red);
			}
			
			tvMessage.setText(message);
			
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			tvMessage.setBackgroundResource(R.drawable.rectanle_rounded_gray);
			tvMessage.setText(message);
			//super.onError(message);
		}
	}
}
