package com.modusgo.ubi;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.ScorePieChartContract.ScorePieChartEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;

public class ScorePieChartActivity extends MainActivity{

	public static final String SAVED_PIE_CHART_ROAD_SETTINGS = "pieChartRoadSettings";
	public static final String SAVED_PIE_CHART_ROAD_TYPE = "pieChartRoadType";
	public static final String SAVED_PIE_CHART_TIME_OF_DAY = "pieChartTimeOfDay";
	
	long vehicleId = 0;
	RadioGroup rgPieCharts;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_score_pie_chart);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Time/Road Charts");
		
		if(savedInstanceState!=null){
			vehicleId = savedInstanceState.getLong(VehicleEntry._ID);
		}
		else if(getIntent()!=null){
			vehicleId = getIntent().getLongExtra(VehicleEntry._ID,0);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		vehicle = dHelper.getVehicleShort(vehicleId);
		dHelper.close();
		
		rgPieCharts = (RadioGroup) findViewById(R.id.radioGroupPieCharts);
		DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		boolean updateSuccessful = updatePieCharts(db);
		db.close();
		dbHelper.close();
		if(!updateSuccessful){
			finish();
		}
	}
	
	private boolean updatePieCharts(SQLiteDatabase db){		
		Cursor c = db.query(ScorePieChartEntry.TABLE_NAME, 
				new String[]{
				ScorePieChartEntry.COLUMN_NAME_TAB}, 
				ScorePieChartEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, ScorePieChartEntry.COLUMN_NAME_TAB, null, ScorePieChartEntry._ID+" ASC");
		
		String pieChartTabs[] = new String[c.getCount()];
		
		if(pieChartTabs.length==0)
			return false;
		
		if(c.moveToFirst()){
			int i = 0;
			while(!c.isAfterLast()){
				pieChartTabs[i] = c.getString(0);
				i++;
				c.moveToNext();
			}
		}
		c.close();
	   
        ArrayList<Fragment> pieChartFragments = new ArrayList<>();
        
        LayoutInflater inflater = getLayoutInflater();
        
        for (int i = 0; i < pieChartTabs.length; i++) {
            
            Bundle bundle = new Bundle();
            
            c = db.query(ScorePieChartEntry.TABLE_NAME, 
    				new String[]{
    				ScorePieChartEntry._ID,
    				ScorePieChartEntry.COLUMN_NAME_VALUE,
    				ScorePieChartEntry.COLUMN_NAME_TITLE,
    				ScorePieChartEntry.COLUMN_NAME_SUBTITLE},
    				ScorePieChartEntry.COLUMN_NAME_VEHICLE_ID + " = ? AND "+ScorePieChartEntry.COLUMN_NAME_TAB + " = ?", new String[]{Long.toString(vehicle.id), pieChartTabs[i]}, null, null, ScorePieChartEntry._ID+" ASC");
    		
            int piecesCount = c.getCount();
            float[] values = new float[piecesCount];
            String[] titles = new String[piecesCount];
            String[] subtitles = new String[piecesCount];
            
    		if(c.moveToFirst()){
    			int j = 0;
    			while(!c.isAfterLast()){
    				values[j] = c.getFloat(1);
    				titles[j] = c.getString(2);
    				subtitles[j] = c.getString(3);
    				j++;
    				c.moveToNext();
    			}
    		}
    		c.close();
    		
    		bundle.putFloatArray(PieChartFragment.SAVED_VALUES, values);
    	    bundle.putStringArray(PieChartFragment.SAVED_TITLES, titles);
    	    bundle.putStringArray(PieChartFragment.SAVED_SUBTITLES, subtitles);
    		
        	RadioButton rb = (RadioButton)inflater.inflate(R.layout.radio_tab, rgPieCharts, false);
        	rb.setText(pieChartTabs[i]);
        	rb.setBackgroundResource(R.drawable.radio_tab_bg_selector);
        	Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
        	rb.setTypeface(tf);
        	
        	final Fragment fragment = new PieChartFragment();
        	fragment.setArguments(bundle);
        	pieChartFragments.add(fragment);
        	
        	final String tabName = pieChartTabs[i];
        	
        	rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        		@Override
        		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        			if(isChecked){
        				getSupportFragmentManager().beginTransaction()
        				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        				.replace(R.id.pieChartsContainer, fragment)
        				.commit();
        				
        		        Utils.gaTrackScreen(ScorePieChartActivity.this, "Time/Road Charts Screen - "+tabName);
        			}
        		}
        	});
        	
        	rgPieCharts.addView(rb);
        	if(i==0){
        		rb.setId(R.id.radioButtonSelected);
        		rgPieCharts.check(rb.getId());
        		
        		getSupportFragmentManager().beginTransaction()
        		.replace(R.id.pieChartsContainer, fragment)
        		.commitAllowingStateLoss();
        	}
        }
		
		return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(VehicleEntry._ID, vehicleId);
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
	
	public static class PieChartTab{
		
		public String tabName;
		public float[] values;
		public String[] titles;
		public String[] subtitles;
		
		public PieChartTab(String tabName, float[] values, String[] titles,
				String[] subtitles) {
			super();
			this.tabName = tabName;
			this.values = values;
			this.titles = titles;
			this.subtitles = subtitles;
		}
	}
}
