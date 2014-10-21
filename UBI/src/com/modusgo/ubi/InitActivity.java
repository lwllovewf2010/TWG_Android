package com.modusgo.ubi;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
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
import android.widget.TextView;

import com.modusgo.demo.R;
import com.modusgo.ubi.utils.RequestGet;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class InitActivity extends FragmentActivity {
    
	View layoutProgress;
	View layoutFields;
	EditText editClientId;
	TextView tvError;
	TextView tvProgress;
	
	Animation fadeInProgress;
	Animation fadeOutProgress;
	Animation fadeInFields;
	Animation fadeOutFields;
	
	SharedPreferences prefs;
	
	String clientId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init);
	    getActionBar().hide();
	    
	    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
        .memoryCacheSize(2 * 1024 * 1024)
        .diskCacheSize(50 * 1024 * 1024)
        .diskCacheFileCount(100)
        .build();
		ImageLoader.getInstance().init(config);
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(InitActivity.this);
	    clientId = prefs.getString(Constants.PREF_CLIENT_ID, "");
	    
	    layoutFields = findViewById(R.id.llFields);
	    layoutProgress = findViewById(R.id.rlProgress);
	    tvError = (TextView) findViewById(R.id.tvError);
	    tvProgress = (TextView) findViewById(R.id.tvProgress);
	    editClientId = (EditText)findViewById(R.id.editClientId);
	    
	    fadeInProgress = getFadeInAnmation(layoutProgress);
		fadeOutProgress = getFadeOutAnmation(layoutProgress);
		fadeInFields = getFadeInAnmation(layoutFields);
		fadeOutFields = getFadeOutAnmation(layoutFields);
	    
	    editClientId.setText(clientId);
	    
	    if(!clientId.equals("")){
		    layoutFields.setVisibility(View.GONE);
	    	new ServerCheckTask().execute();
	    }
	    
	    Button btnSubmit = (Button)findViewById(R.id.btnSubmit);
	    
	    ProgressBar pb = (ProgressBar)findViewById(R.id.progressLogging);
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    a.setDuration(1000);
	    pb.startAnimation(a);
	    
	    btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new ServerCheckTask().execute();
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
	
	class ServerCheckTask extends AsyncTask<Void, Void, Boolean>{

		JSONArray welcomeScreens = null;
		int status;
		String message = "Client ID not found or server is unavailable";
		
		@Override
		protected void onPreExecute() {
			if(layoutProgress.getVisibility()==View.GONE){
				layoutProgress.setVisibility(View.VISIBLE);
				layoutProgress.startAnimation(fadeInProgress);
				layoutFields.startAnimation(fadeOutFields);
			}
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			String clientId = editClientId.getText().toString();
			
			HttpResponse result = new RequestGet(Constants.API_BASE_URL_PREFIX+clientId+Constants.API_BASE_URL_POSTFIX+"info").execute();
			if(result==null){
				status = 0;
				//message = "Connection error";
				
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
	        	status = result.getStatusLine().getStatusCode();
	        	
	        	if(status>=200 && status<300){
					JSONObject responseJSON = Utils.getJSONObjectFromHttpResponse(result);
					if(responseJSON.getString("status").equals("success")){
						prefs.edit().putString(Constants.PREF_CLIENT_ID, clientId).commit();
						JSONObject infoJSON = responseJSON.getJSONObject("info");
						
						if(!infoJSON.isNull("welcome"))
							welcomeScreens = infoJSON.getJSONArray("welcome");
//						else{
//							welcomeScreens = new JSONArray("[{page_id: \"unique_identifator_of_page\",content_type:\"image\",title: \"Some title of the page\"," +
//						"image: \"http://fbrest.edgecaching.net/tpr-theme-img/concept_girl.png\"},{page_id: \"v2\", content_type: \"text\", confirm: \"popup\", " +
//						"confirm_text: \"I agree terms of services\", title: \"Whats new ?\", body: \"Improved search :)\"}]");
//						System.out.println(welcomeScreens);
//						}
						return true;
					}
	        	}
	        	else{
	        		//message = "Error "+result.getStatusLine().getStatusCode()+": "+result.getStatusLine().getReasonPhrase();
			        return false;
	        	}
			} catch (Exception e) {
				status = 0;
				//message = "Wrong Client ID";
				e.printStackTrace();
			}
	        
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				if(welcomeScreens!=null && welcomeScreens.length()>0){
					Intent i = new Intent(InitActivity.this, WelcomeActivity.class);
					i.putExtra(WelcomeActivity.SAVED_SCREENS, welcomeScreens.toString());
					startActivity(i);
				}
				else
					startActivity(new Intent(InitActivity.this, SignInActivity.class));
				overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
				finish();
			}
			else{
				if(!clientId.equals("")){
					tvProgress.setText("Check your internet connection...");
					new ServerCheckTask().execute();
				}
				else{
					layoutFields.setVisibility(View.VISIBLE);
					layoutProgress.startAnimation(fadeOutProgress);
					layoutFields.startAnimation(fadeInFields);
					tvError.setText(message);
				}
			}
			
			super.onPostExecute(result);
		}
	}
}
