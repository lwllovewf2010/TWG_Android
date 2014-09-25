package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Set;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ScoreCirclesActivity extends MainActivity{

	public static final String SAVED_CIRCLES_BUNDLE = "circlesBundle";
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;

	Bundle circlesData;
	RadioGroup rgCircles;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_score_circles);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Behaviors");
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
			circlesData = savedInstanceState.getBundle(SAVED_CIRCLES_BUNDLE);
		}
		else if(getIntent()!=null){
			driverIndex = getIntent().getIntExtra("id",0);
			circlesData = getIntent().getBundleExtra(SAVED_CIRCLES_BUNDLE);
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		
		rgCircles = (RadioGroup) findViewById(R.id.radioGroupCircles);
        
		Set<String> bundleKeys = circlesData.keySet();
		Bundle[] b = new Bundle[bundleKeys.size()];
		int i = 0;
		for (String key : bundleKeys) {
			b[i] = circlesData.getBundle(key);
			i++;
		}
		
		updateCircles(b);
	}
	
	private void updateCircles(Bundle bundles[]) {
        ArrayList<Fragment> circleFragments = new ArrayList<>();
        
        LayoutInflater inflater = getLayoutInflater();
        
        for (int i = 0; i < bundles.length; i++) {
        	RadioButton rb = (RadioButton)inflater.inflate(R.layout.radio_tab, rgCircles, false);
        	rb.setText(bundles[i].getString(CirclesFragment.SAVED_TITLE));
            rb.setBackgroundResource(R.drawable.radio_tab_bg_selector);
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
            rb.setTypeface(tf);
            
            final Fragment fragment = new CirclesFragment();
            fragment.setArguments(bundles[i]);
            circleFragments.add(fragment);
            
            rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				        .replace(R.id.circlesContainer, fragment)
				        .commit();
					}
				}
			});
            
            rgCircles.addView(rb);
            if(i==0){
                rb.setId(R.id.radioButtonSelected);
                rgCircles.check(rb.getId());
                
                getSupportFragmentManager().beginTransaction()
                .replace(R.id.circlesContainer, fragment)
                .commitAllowingStateLoss();
            }
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		outState.putBundle(SAVED_CIRCLES_BUNDLE, circlesData);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void up() {
		super.up();
		overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
	}

}
