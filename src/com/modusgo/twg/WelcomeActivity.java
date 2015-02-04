package com.modusgo.twg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.modusgo.twg.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class WelcomeActivity extends FragmentActivity {
    
	public static final String SAVED_SCREENS = "screensJSON";
	
	View layoutProgress;
	View layoutFields;
	TextView tvTitle;
	WebView webView;
	ImageView imageView;
	Button btnNext;
	
	SharedPreferences prefs;
	
	JSONArray screensJSON;
	private int currentScreen = -1;
	private int screensCount = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
	    getActionBar().hide();
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
	    
	    ImageView imageBg = (ImageView) findViewById(R.id.imageBg);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();
    	
    	ImageLoader.getInstance().displayImage(prefs.getString(Constants.PREF_BR_LOGIN_SCREEN_BG_IMAGE, ""), imageBg, options);
	    
	    
//	    layoutFields = findViewById(R.id.llFields);
	    layoutProgress = findViewById(R.id.rlProgress);
	    tvTitle = (TextView) findViewById(R.id.tvTitle);
	    webView = (WebView) findViewById(R.id.webView);
	    imageView = (ImageView) findViewById(R.id.image);
	    btnNext = (Button)findViewById(R.id.btnNext);
	    
	    try {
			screensJSON = new JSONArray(getIntent().getStringExtra(SAVED_SCREENS));
			
			if(screensJSON!=null){
		    	screensCount = screensJSON.length();
		    	if(screensCount>0)
		    		showNextScreen();
		    }
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    
	    ProgressBar pb = (ProgressBar)findViewById(R.id.progressLogging);
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    a.setDuration(1000);
	    pb.startAnimation(a);
	}
	
	private void showNextScreen() throws JSONException{
		if(currentScreen<screensCount-1){
			currentScreen++;
			final JSONObject screenJSON = screensJSON.getJSONObject(currentScreen);
			
			final String welcomePagesIds = prefs.getString(Constants.PREF_WELCOME_PAGES, "");
			final String pageId = screenJSON.optString("page_id");
			
			if(!pageId.equals("") && !welcomePagesIds.contains(pageId)){
			
				webView.setVisibility(View.GONE);
				imageView.setVisibility(View.GONE);
				
				tvTitle.setText(screenJSON.optString("title"));
				
				switch (screenJSON.optString("content_type","text")) {
				case "text":
					webView.setVisibility(View.VISIBLE);
					webView.loadData(screenJSON.optString("body"), "text/html", null);
					webView.setBackgroundColor(0x00000000);
					webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
					break;
				case "image":
					imageView.setVisibility(View.VISIBLE);
					
					DisplayImageOptions options = new DisplayImageOptions.Builder()
			        .showImageOnLoading(R.drawable.login_progress)
			        .showImageForEmptyUri(R.drawable.ic_launcher)
			        .showImageOnFail(R.drawable.ic_launcher)
			        .cacheInMemory(true)
			        .cacheOnDisk(true)
			        .build();
			    	
			    	ImageLoader.getInstance().displayImage(screenJSON.optString("image"), imageView, options);
					
					break;
				}
				
				if(screenJSON.optString("confirm").equals("popup")){
					btnNext.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
					        builder.setMessage(screenJSON.optString("confirm_text"))
					               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					                   public void onClick(DialogInterface dialog, int id) {
					                	   try {
					                		   prefs.edit().putString(Constants.PREF_WELCOME_PAGES, welcomePagesIds+" "+pageId).commit();
					                		   showNextScreen();
					                	   } catch (JSONException e) {
					                		   e.printStackTrace();
					                	   }
					                   }
					               })
					               .setNegativeButton("No", new DialogInterface.OnClickListener() {
					                   public void onClick(DialogInterface dialog, int id) {
					                       dialog.dismiss();
					                   }
					               });
					        builder.create().show();
						}
					});
				}
				else{
					btnNext.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								prefs.edit().putString(Constants.PREF_WELCOME_PAGES, welcomePagesIds+" "+pageId).commit();
								showNextScreen();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
			else{
				showNextScreen();
			}
		}
		else{
			startActivity(new Intent(this, SignInActivity.class));
			overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
			finish();
		}
	}
}
