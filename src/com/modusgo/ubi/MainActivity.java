package com.modusgo.ubi;

import java.util.ArrayList;

import com.modusgo.modusadmin.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	Fragment frag1;
	ChartFragment frag2;
	FragmentTransaction fTrans;
	CheckBox chbStack;
	
	ChartsPagerAdapter mChartsPagerAdapter;
    ViewPager mViewPager;	
	
	  
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);

	    
	    ArrayList<Chart> charts = new ArrayList<Chart>();
	    charts.add(new Chart("Yippie", new float[]{0.2f,0.2f,0.8f,0}));
	    charts.add(new Chart("Ki", new float[]{0.5f,0.9f,0.6f,0}));
	    charts.add(new Chart("Yay", new float[]{0.3f,0.6f,0.8f,0}));
	    charts.add(new Chart("Mr. Willis", new float[]{0.9f,0.2f,0.6f,0}));
	    
	    /*fTrans = getSupportFragmentManager().beginTransaction();
	    fTrans.replace(R.id.charts, charts.get(0).fragment);
		fTrans.commit();*/
		
		
		mChartsPagerAdapter = new ChartsPagerAdapter(getSupportFragmentManager(),charts);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mChartsPagerAdapter);

		((Button)findViewById(R.id.button1)).setOnClickListener(this);
		((Button)findViewById(R.id.button2)).setOnClickListener(this);
		((Button)findViewById(R.id.button3)).setOnClickListener(this);
	    
	    /*final float scale = getResources().getDisplayMetrics().density;
	    
	    for (final Chart chart : charts) {
	    	final Button chartBtn = new Button(getApplicationContext());
	    	chartBtn.setText(chart.name);
	    	chartBtn.setWidth((int) (160 * scale + 0.5f));
	    	
	    	chartBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int[] loc = new int[2];
					chartBtn.getLocationOnScreen(loc);
					
					fTrans = getSupportFragmentManager().beginTransaction();
					
					HorizontalScrollView scrollView = (HorizontalScrollView)findViewById(R.id.horizontalScrollView1);
					int scrollToPosX = (int)(chartBtn.getLeft()-chartBtn.getWidth()/2f);
					if(scrollToPosX<scrollView.getScrollX())
						fTrans.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
					else
						fTrans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
						
					scrollView.smoothScrollTo(scrollToPosX,0);
					
					fTrans.replace(R.id.charts, chart.fragment);
					if (chbStack.isChecked()) fTrans.addToBackStack(null);
					fTrans.commit();
				}
			});
	    	
			((LinearLayout)findViewById(R.id.chartButtons)).addView(chartBtn);
		}
	    
	    chbStack = (CheckBox)findViewById(R.id.chbStack);*/
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public void onClick(View v) {
	    //fTrans = getSupportFragmentManager().beginTransaction();
	    //fTrans.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	    switch (v.getId()) {
	    	case R.id.button1:
	    	case R.id.button2:
	    	case R.id.button3:
	    		startActivity(new Intent(getApplicationContext(), DriverActivity.class));
	    	    break;
		    default:
		    	break;
	    }
	    //if (chbStack.isChecked()) fTrans.addToBackStack(null);
	    	//fTrans.commit();
	}

}
