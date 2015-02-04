package com.modusgo.twg;

import java.io.Serializable;
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

import com.modusgo.twg.R;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.ScoreCirclesContract.ScoreCirclesEntry;
import com.modusgo.twg.db.ScorePieChartContract.ScorePieChartEntry;
import com.modusgo.twg.db.VehicleContract.VehicleEntry;
import com.modusgo.twg.utils.Utils;

public class ScoreCirclesActivity extends MainActivity{

	public static final String SAVED_CIRCLES_BUNDLE = "circlesBundle";
	
	long driverId = 0;

	Bundle circlesData;
	RadioGroup rgCircles;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_score_circles);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Behaviors");
		
		if(savedInstanceState!=null){
			driverId = savedInstanceState.getLong(VehicleEntry._ID);
			circlesData = savedInstanceState.getBundle(SAVED_CIRCLES_BUNDLE);
		}
		else if(getIntent()!=null){
			driverId = getIntent().getLongExtra(VehicleEntry._ID,0);
			circlesData = getIntent().getBundleExtra(SAVED_CIRCLES_BUNDLE);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		vehicle = dHelper.getVehicleShort(driverId);
		dHelper.close();
		
		rgCircles = (RadioGroup) findViewById(R.id.radioGroupCircles);
        
        DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		boolean updateSuccessful = updateCircles(db);
		db.close();
		dbHelper.close();
		if(!updateSuccessful){
			finish();
		}
	}
	
	private boolean updateCircles(SQLiteDatabase db) {
        ArrayList<Fragment> circleFragments = new ArrayList<>();
        
        LayoutInflater inflater = getLayoutInflater();
        
		
		Cursor c = db.query(ScoreCirclesEntry.TABLE_NAME, 
				new String[]{
				ScoreCirclesEntry.COLUMN_NAME_TAB}, 
				ScoreCirclesEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, ScoreCirclesEntry.COLUMN_NAME_TAB, null, ScoreCirclesEntry._ID+" ASC");
		
		String circlesTabs[] = new String[c.getCount()];
		
		if(c.moveToFirst()){
			int i = 0;
			while(!c.isAfterLast()){
				circlesTabs[i] = c.getString(0);
				i++;
				c.moveToNext();
			}
		}
		c.close();
		
		if(circlesTabs.length==0)
			return false;
        
        for (int i = 0; i < circlesTabs.length; i++) {
        	
        	ArrayList<CirclesSection> sections = new ArrayList<CirclesSection>();
        	
        	c = db.query(ScoreCirclesEntry.TABLE_NAME, 
    				new String[]{
    				ScoreCirclesEntry.COLUMN_NAME_SECTION}, 
    				ScoreCirclesEntry.COLUMN_NAME_VEHICLE_ID + " = ? AND " + ScoreCirclesEntry.COLUMN_NAME_TAB + " = ?",
    				new String[]{Long.toString(vehicle.id), circlesTabs[i]},
    				ScoreCirclesEntry.COLUMN_NAME_SECTION, null, ScoreCirclesEntry._ID+" ASC");
    		
    		String sectionTitles[] = new String[c.getCount()];
    		
    		if(c.moveToFirst()){
    			int j = 0;
    			while(!c.isAfterLast()){
    				sectionTitles[j] = c.getString(0);
    				j++;
    				c.moveToNext();
    			}
    		}
    		c.close();
    		
    		for (int j = 0; j < sectionTitles.length; j++) {
    			c = db.query(ScoreCirclesEntry.TABLE_NAME, 
        				new String[]{
            			ScoreCirclesEntry._ID,
            			ScoreCirclesEntry.COLUMN_NAME_MARK,
            			ScoreCirclesEntry.COLUMN_NAME_DISTANCE},
            			ScoreCirclesEntry.COLUMN_NAME_VEHICLE_ID + " = ? AND " + ScoreCirclesEntry.COLUMN_NAME_TAB + " = ? AND " + ScoreCirclesEntry.COLUMN_NAME_SECTION + " = ?",
            			new String[]{Long.toString(vehicle.id), circlesTabs[i], sectionTitles[j]},
            			null, null, ScorePieChartEntry._ID+" ASC");
        		
                int piecesCount = c.getCount();
                int[] marks = new int[piecesCount];
                double[] distances = new double[piecesCount];
                
        		if(c.moveToFirst()){
        			int k = 0;
        			while(!c.isAfterLast()){
        				marks[k] = c.getInt(1);
        				distances[k] = c.getDouble(2);
        				k++;
        				c.moveToNext();
        			}
        		}
        		c.close();
        		sections.add(new CirclesSection(sectionTitles[j], marks, distances));
			}        	
        	
        	RadioButton rb = (RadioButton)inflater.inflate(R.layout.radio_tab, rgCircles, false);
        	rb.setText(circlesTabs[i]);
            rb.setBackgroundResource(R.drawable.radio_tab_bg_selector);
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
            rb.setTypeface(tf);
            
            final Fragment fragment = new CirclesFragment();
            Bundle b = new Bundle();
            b.putSerializable(CirclesFragment.SAVED_SECTIONS, sections);
            b.putString(TitledFragment.SAVED_TITLE, circlesTabs[i]);
            fragment.setArguments(b);
            circleFragments.add(fragment);

        	final String tabName = circlesTabs[i];
        	
            rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				        .replace(R.id.circlesContainer, fragment)
				        .commit();
						
        		        Utils.gaTrackScreen(ScoreCirclesActivity.this, "Behaviors Screen - "+tabName);
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
        
        return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(VehicleEntry._ID, driverId);
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
	
	public static class CirclesSection implements Serializable{
		
		private static final long serialVersionUID = 4900183600203934640L;
		public String sectionName;
		public int[] marks;
		public double[] distances;
		
		public CirclesSection(String sectionName, int[] marks, double[] distances) {
			super();
			this.sectionName = sectionName;
			this.marks = marks;
			this.distances = distances;
		}
	}

}
