package com.modusgo.ubi;

import com.modusgo.ubi.utils.Utils;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class EducationActivity extends MainActivity {
	
	public static final String SAVED_STRING_RESOURCE = "stringResource";
	
	TextView tvContent;
	int infoStringResource;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_education);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Driver Education");
		
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
	protected void onResume() {
        Utils.gaTrackScreen(this, "Education Screen");
		super.onResume();
	}
	
	@Override
	public void up() {
		finish();
	}
	
	@Override
	protected void setActionBarAppearance() {
		actionBar.getCustomView().setBackgroundColor(Color.parseColor("#00aeef"));
		tvActionBarTitle.setTextColor(Color.parseColor("#FFFFFF"));
		Mode mMode = Mode.SRC_ATOP;
	    getResources().getDrawable(R.drawable.ic_arrow_left).setColorFilter(Color.parseColor("#FFFFFF"),mMode);
	    getResources().getDrawable(R.drawable.ic_menu).setColorFilter(Color.parseColor("#FFFFFF"),mMode);
	    getResources().getDrawable(R.drawable.ic_menu_close).setColorFilter(Color.parseColor("#FFFFFF"),mMode);
	    getResources().getDrawable(R.drawable.ic_map).setColorFilter(Color.parseColor("#FFFFFF"),mMode);
	    
	}
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(SAVED_STRING_RESOURCE, infoStringResource);
		super.onSaveInstanceState(outState);
	}

}