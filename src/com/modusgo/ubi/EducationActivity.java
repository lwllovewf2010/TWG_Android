package com.modusgo.ubi;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class EducationActivity extends MainActivity {
	
	public static final String SAVED_STRING_RESOURCE = "stringResource";
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	
	TextView tvContent;
	int infoStringResource;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_education);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Driver Education");
		getActionBar().getCustomView().setBackgroundColor(Color.parseColor("#00aeef"));
		
		setButtonUpVisibility(false);
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
			infoStringResource = savedInstanceState.getInt(SAVED_STRING_RESOURCE);
		}
		else if(getIntent()!=null){
			driverIndex = getIntent().getIntExtra("id", 0);
			infoStringResource = getIntent().getIntExtra(SAVED_STRING_RESOURCE, 0);
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		
		tvContent = (TextView) findViewById(R.id.tvContent);
		tvContent.setText(Html.fromHtml(getResources().getString(infoStringResource)));
        
        
	}
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		outState.putInt(SAVED_STRING_RESOURCE, infoStringResource);
		super.onSaveInstanceState(outState);
	}

}