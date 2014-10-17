package com.modusgo.ubi;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.modusgo.demo.R;

public class EducationActivity extends MainActivity {
	
	public static final String SAVED_STRING_RESOURCE = "stringResource";
	
	TextView tvContent;
	int infoStringResource;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_education);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Driver Education");
		getActionBar().getCustomView().setBackgroundColor(Color.parseColor("#00aeef"));
		
		if(savedInstanceState!=null){
			infoStringResource = savedInstanceState.getInt(SAVED_STRING_RESOURCE);
		}
		else if(getIntent()!=null){
			infoStringResource = getIntent().getIntExtra(SAVED_STRING_RESOURCE, 0);
		}

		tvContent = (TextView) findViewById(R.id.tvContent);
		tvContent.setText(Html.fromHtml(getResources().getString(infoStringResource)));
        
        
	}
	
	@Override
	public void up() {
		finish();
	}
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(SAVED_STRING_RESOURCE, infoStringResource);
		super.onSaveInstanceState(outState);
	}

}