package com.modusgo.ubi;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SignInActivity extends ActionBarActivity {
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
	    getSupportActionBar().hide();
	    
	    Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
	    
	    EditText email = (EditText)findViewById(R.id.email);
	    EditText password = (EditText)findViewById(R.id.password);

	    Button btnSignIn = (Button)findViewById(R.id.btnSignIn);
	    Button btnForgotPassword = (Button)findViewById(R.id.btnForgotPassword);
	    
	    email.setTypeface(robotoRegular);
	    password.setTypeface(robotoRegular);
	    btnSignIn.setTypeface(robotoRegular);
	    btnForgotPassword.setTypeface(robotoRegular);
	    
	    btnSignIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SignInActivity.this, MainActivity.class));
				finish();
			}
		});
	}
}
