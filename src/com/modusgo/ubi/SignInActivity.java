package com.modusgo.ubi;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SignInActivity extends Activity {
    
	View llProgress;
	View llFields;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
	    getActionBar().hide();
	    
	    Typeface encodeSansNormal = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-500-Medium.ttf");
	    Typeface encodeSansNormalLight = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");
	    
	    llFields = findViewById(R.id.loginFields);
	    llProgress = findViewById(R.id.loginProgress);
	    
	    TextView tvLoading = (TextView)findViewById(R.id.tvLoading);
	    
	    EditText username = (EditText)findViewById(R.id.username);
	    EditText password = (EditText)findViewById(R.id.password);

	    Button btnSignIn = (Button)findViewById(R.id.btnSignIn);
	    
	    tvLoading.setTypeface(encodeSansNormalLight);
	    username.setTypeface(encodeSansNormalLight);
	    password.setTypeface(encodeSansNormalLight);
	    btnSignIn.setTypeface(encodeSansNormal);
	    
	    
	    ProgressBar pb = (ProgressBar)findViewById(R.id.progressLogging);
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    a.setDuration(1000);
	    pb.startAnimation(a);
	    
	    btnSignIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new LoginTask().execute();
			}
		});
	}
	
	class LoginTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() {
			llFields.setVisibility(View.GONE);
			llProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				startActivity(new Intent(SignInActivity.this, MainActivity.class));
				finish();
			}
			else{
				llFields.setVisibility(View.VISIBLE);
				llProgress.setVisibility(View.GONE);				
			}
			super.onPostExecute(result);
		}
	}
}
