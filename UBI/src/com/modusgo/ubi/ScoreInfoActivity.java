package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.modusgo.ubi.customviews.ExpandableHeightGridView;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.ubi.db.ScorePercentageContract.ScorePercentageEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;

public class ScoreInfoActivity extends MainActivity{
	
	long vehicleId = 0;
	
	private static final String ATTRIBUTE_NAME_VALUE = "value";
	private static final String ATTRIBUTE_NAME_TITLE = "title";
	
	public static final String SAVED_ADDITIONAL_DATA = "additionalData";
	public static final String SAVED_PERCENTAGE_DATA = "percentageData"; 
	
	LinearLayout llAdditionalData;
	SimpleAdapter percentInfoAdapter;

	ArrayList<Map<String, Object>> percentInfoData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_score_info);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Score Stats");
		
		if(savedInstanceState!=null){
			vehicleId = savedInstanceState.getLong(VehicleEntry._ID);
		}
		else if(getIntent()!=null){
			vehicleId = getIntent().getLongExtra(VehicleEntry._ID, 0);
		}

		vehicle = getVehicleFromDb(vehicleId);
		
		ExpandableHeightGridView gvPercentData = (ExpandableHeightGridView) findViewById(R.id.gvPercentData);
		//gvPercentData.setColumnWidth(100);
		gvPercentData.setNumColumns(3);
		
		percentInfoData = new ArrayList<Map<String, Object>>();
		
		DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		boolean updateSuccessful = updatePercentInfoAdapter(db);
		db.close();
		dbHelper.close();
		if(!updateSuccessful){
			finish();
		}
		
		gvPercentData.setAdapter(percentInfoAdapter);
		gvPercentData.setAdditionalTextExpand(1, 12.5f);
		gvPercentData.setExpanded(true);
		gvPercentData.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int infoStringResource = -1;
				switch (position) {
				case 0:
					infoStringResource = R.string.use_of_speed;
					break;
				case 1:
					infoStringResource = R.string.anticipation;
					break;
				case 2:
					infoStringResource = R.string.aggression;
					break;
				case 3:
					infoStringResource = R.string.smoothness;
					break;
				case 4:
					infoStringResource = R.string.completeness;
					break;
				case 5:
					infoStringResource = R.string.consistency;
					break;
				}
				if(infoStringResource != -1){
					Intent i = new Intent(ScoreInfoActivity.this, EducationActivity.class);
					i.putExtra(EducationActivity.SAVED_STRING_RESOURCE, infoStringResource);
					startActivity(i);
				}
			}
		});
		
		llAdditionalData = (LinearLayout) findViewById(R.id.llValue);
		fillAdditionalInfo();
        
	}
	
	private Vehicle getVehicleFromDb(long id){
		DbHelper dHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dHelper.getReadableDatabase();
		Cursor c = db.query(VehicleEntry.TABLE_NAME, 
				new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO,
				VehicleEntry.COLUMN_NAME_CAR_VIN,
				VehicleEntry.COLUMN_NAME_LAST_TRIP_DATE}, 
				VehicleEntry._ID+" = ?", new String[]{Long.toString(id)}, null, null, null);
		
		Vehicle v = new Vehicle();
		
		if(c.moveToFirst()){
			v.id = c.getLong(0);
			v.name = c.getString(1);
			v.photo = c.getString(2);
			v.carVIN = c.getString(3);
			v.lastTripDate = c.getString(4);
		}
		c.close();
		db.close();
		dHelper.close();
		return v;
	}
	
	private boolean updatePercentInfoAdapter(SQLiteDatabase db){		
		Cursor c = db.query(ScorePercentageEntry.TABLE_NAME, 
				new String[]{
				ScorePercentageEntry._ID,
				ScorePercentageEntry.COLUMN_NAME_STAT_NAME,
				ScorePercentageEntry.COLUMN_NAME_STAT_VALUE}, 
				ScorePercentageEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, ScoreGraphEntry._ID+" ASC");
		
		percentInfoData.clear();
		Map<String, Object> m;
		
		int infoCount = c.getCount();
		
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				m = new HashMap<String, Object>();
				m.put(ATTRIBUTE_NAME_TITLE, c.getString(1));
				m.put(ATTRIBUTE_NAME_VALUE, c.getInt(2));
				percentInfoData.add(m);
				c.moveToNext();
			}
		}
		c.close();
		
		if(infoCount==0)
			return false;
		
		String[] from = new String[]{ATTRIBUTE_NAME_VALUE, ATTRIBUTE_NAME_TITLE};
		int[] to = new int[]{R.id.tvPercentValue, R.id.tvTitle};
		
		if(percentInfoAdapter==null)
			percentInfoAdapter = new SimpleAdapter(this, percentInfoData, R.layout.score_percents_item, from, to);
		else
			percentInfoAdapter.notifyDataSetChanged();
		
		return true;
	}
	
	private void fillAdditionalInfo(){
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
		DecimalFormat df = new DecimalFormat("0.000");
		
		LinkedHashMap<String, String> infoFields = new LinkedHashMap<String, String>();
		infoFields.put("Last trip", Utils.convertTime(vehicle.lastTripDate, sdf));
		infoFields.put("VIN", vehicle.carVIN);
		infoFields.put("Profile date", "N/A");
		infoFields.put("Start date", "N/A");
		if(prefs.getString(Constants.PREF_UNITS_OF_MEASURE, "mile").equals("mile")){
			infoFields.put("Profile driving miles", df.format(vehicle.totalDistance)+" Miles");
		}
		else{
			infoFields.put("Profile driving kilometers", df.format(vehicle.totalDistance)+" KM");
		}
		infoFields.put("Estimated annual driving", "N/A");
		
		LayoutInflater inflater = getLayoutInflater();
		
		for (LinkedHashMap.Entry<String, String> entry : infoFields.entrySet()) {
	        String title = entry.getKey();
	        String value = entry.getValue();
	        
	        LinearLayout item = (LinearLayout)inflater.inflate(R.layout.score_additional_info_item, llAdditionalData, false);
			TextView tvTitle = (TextView)item.findViewById(R.id.tvTitle);
			tvTitle.setAllCaps(true);
			tvTitle.setText(title);
			((TextView)item.findViewById(R.id.tvValue)).setText(value);
			llAdditionalData.addView(item);
	    }
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

}
