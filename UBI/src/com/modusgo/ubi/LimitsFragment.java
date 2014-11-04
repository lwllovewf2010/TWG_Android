package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.LimitsContract.LimitsEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class LimitsFragment extends Fragment {
	
	Driver driver;
	
	LinearLayout content;
	LinearLayout llProgress;
	LayoutInflater inflater;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout) inflater.inflate(
				R.layout.fragment_limits, container, false);

		this.inflater = inflater; 
		
		((MainActivity) getActivity()).setActionBarTitle("LIMITS");
		
		driver = ((DriverActivity)getActivity()).driver;
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    if(driver.photo == null || driver.photo.equals(""))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(driver.photo, imagePhoto, options);
	    }
		
		rootView.findViewById(R.id.btnSwitchDriverMenu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).menu.toggle();
			}
		});

		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);

		llProgress = (LinearLayout)rootView.findViewById(R.id.llProgress);
		content = (LinearLayout)rootView.findViewById(R.id.llContent);
		
		new GetLimitsTask(getActivity()).execute("vehicles/"+driver.id+"/limits.json");
		
		return rootView;
	}
	
	private void updateLimits(){
		
		ArrayList<Limit> limits = getLimitsFromDB();
		ArrayList<LimitsListGroup> groups = new ArrayList<LimitsListGroup>();
		
		for (Limit limit : limits) {
			switch (limit.type) {
			case "number_picker":
			case "number_input":
				ArrayList<LimitsListChild> maxSpeedLimitsChildren = new ArrayList<LimitsListChild>();
				int value = 0;
				try{
					value = Integer.parseInt(limit.value);
				}
				catch(NumberFormatException e){
					e.printStackTrace();
				}
				maxSpeedLimitsChildren.add(new LimitsSingleValueChild(value, "Set max to"));
				groups.add(new LimitsListGroup(limit.title, limit.active, maxSpeedLimitsChildren));
				
				break;
			case "flag":
				groups.add(new LimitsListGroup(limit.title, limit.active));
				break;
			case "time_between_picker":
				ArrayList<LimitsListChild> timeOfDayLimitsChildren = new ArrayList<LimitsListChild>();
				timeOfDayLimitsChildren.add(new LimitsTimePeriodChild(limit.minValue, limit.maxValue, "Between", "and"));
				groups.add(new LimitsListGroup(limit.title, limit.active, timeOfDayLimitsChildren));
				break;
			case "geofence":
				ArrayList<LimitsListChild> geofenceChildren = new ArrayList<LimitsListChild>();
				geofenceChildren.add(new LimitsLinkChild(GeofenceActivity.class, "Set geofence"));
				groups.add(new LimitsListGroup(limit.title, limit.active, geofenceChildren));
				
				break;
			default:
				break;
			}
		}
		
		for (final LimitsListGroup limitsListGroup : groups) {
			View groupView = inflater.inflate(R.layout.limits_toggle_button_item, content, false);
			final ToggleButton btnToggle = (ToggleButton)groupView.findViewById(R.id.btnToggle);
			
			groupView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					btnToggle.performClick();
				}
			});
			
			
			if(limitsListGroup.childs.size()>0){
				final LinearLayout groupChilds = (LinearLayout)groupView.findViewById(R.id.llChilds);
				for (LimitsListChild child : limitsListGroup.childs) {
					final View childView = inflater.inflate(child.layoutId, groupChilds, false);

					childView.setClickable(true);
					//Block parent click listener
					childView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
						}
					});
					
					TextView textChild = (TextView) childView.findViewById(R.id.tvTitle);
					textChild.setText(child.text[0]);
					
					if(child instanceof LimitsSingleValueChild){
						final LimitsSingleValueChild c = (LimitsSingleValueChild)child;
						
						final TextView tvSingleValue = (TextView)childView.findViewById(R.id.tvValue);
						tvSingleValue.setText(c.value+" ");
						tvSingleValue.setOnClickListener(new OnClickListener() {		
							@Override
							public void onClick(View v) {
								AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							    // Get the layout inflater
							    LayoutInflater inflater = getActivity().getLayoutInflater();

							    // Inflate and set the layout for the dialog
							    // Pass null as the parent view because its going in the dialog layout
							    final View dialogContentView = inflater.inflate(R.layout.dialog_numbers, null);
							    ((TextView)dialogContentView.findViewById(R.id.editValue)).setText(""+c.value);
							    builder.setView(dialogContentView)
							    // Add action buttons
							    .setTitle(((TextView)childView.findViewById(R.id.tvValue)).getText())
							    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
							    	@Override
							        public void onClick(DialogInterface dialog, int id) {
							    		int value = Integer.valueOf(((TextView)dialogContentView.findViewById(R.id.editValue)).getText().toString());
							    		tvSingleValue.setText(value+" ");
							    		c.value = value;
							        }
							    })
							    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							    	public void onClick(DialogInterface dialog, int id) {
							    		dialog.cancel();
							        }
							    });      
							    builder.create().show();
							}
						});
					}
					else if(child instanceof LimitsTimePeriodChild){
						final LimitsTimePeriodChild c = (LimitsTimePeriodChild)child;
						
						SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa", Locale.US);
						final Calendar startCalendar = Calendar.getInstance();
						final Calendar endCalendar = Calendar.getInstance();
						try {
							startCalendar.setTime(sdf.parse(c.startTime));
							endCalendar.setTime(sdf.parse(c.endTime));
						} catch (ParseException e) {
							e.printStackTrace();
							startCalendar.set(2014, 0, 1, 10, 0);
							endCalendar.set(2014, 0, 1, 20, 0);
						}
						
						if(child.text[1]!=null){
							((TextView) childView.findViewById(R.id.tvTitle2)).setText(child.text[1]);
						}
						
						final TextView tvValue = (TextView)childView.findViewById(R.id.tvValue);
						tvValue.setText(c.startTime);
						tvValue.setOnClickListener(new OnClickListener() {		
							@Override
							public void onClick(View v) {
								TimePickerDialog tpd = new TimePickerDialog(getActivity(), new OnTimeSetListener(){
									@Override
									public void onTimeSet(TimePicker arg0, int hourOfDay, int minutes) {
										startCalendar.set(2014, 0, 1, hourOfDay, minutes);
										tvValue.setText(getTimeString(hourOfDay, minutes));
										c.startTime = getTimeString(hourOfDay, minutes);
									}
								}, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), false);
								tpd.show();
							}
						});
						
						final TextView tvValue2 = (TextView)childView.findViewById(R.id.tvValue2);
						tvValue2.setText(c.endTime);
						tvValue2.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								TimePickerDialog tpd = new TimePickerDialog(getActivity(), new OnTimeSetListener(){
									@Override
									public void onTimeSet(TimePicker arg0, int hourOfDay, int minutes) {
										endCalendar.set(2014, 0, 1, hourOfDay, minutes);
										tvValue2.setText(getTimeString(hourOfDay, minutes));
										c.endTime = getTimeString(hourOfDay, minutes);
									}
								}, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false);
								tpd.show();
							}
						});
					}
					else if(child instanceof LimitsLinkChild){
						final LimitsLinkChild c = (LimitsLinkChild)child;
						
						LinearLayout llContent = (LinearLayout) childView.findViewById(R.id.llContent);
						
						llContent.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								showSavedToast = false;
								Intent i = new Intent(getActivity(), c.linkActivityClass);
								i.putExtra(VehicleEntry._ID, driver.id);
								startActivity(i);
							}
						});
					}
					
					groupChilds.addView(childView);
				}
				
				if(limitsListGroup.enabled){
					btnToggle.setChecked(true);
					groupChilds.setVisibility(View.VISIBLE);
				}
				
				btnToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isChecked)
							groupChilds.setVisibility(View.VISIBLE);
						else
							groupChilds.setVisibility(View.GONE);
						
						limitsListGroup.enabled = isChecked;
					}
				});
			}
			else{
				if(limitsListGroup.enabled){
					btnToggle.setChecked(true);
				}
				
				btnToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {						
						limitsListGroup.enabled = isChecked;
					}
				});
			}
			
			((TextView)groupView.findViewById(R.id.tvTitle)).setText(limitsListGroup.groupTitle);
			
			content.addView(groupView);
		}
	}
	
	private ArrayList<Limit> getLimitsFromDB(){
		DbHelper dbHelper = DbHelper.getInstance(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor c = db.query(LimitsEntry.TABLE_NAME, 
				new String[]{
				LimitsEntry.COLUMN_NAME_KEY,
				LimitsEntry.COLUMN_NAME_TITLE,
				LimitsEntry.COLUMN_NAME_TYPE,
				LimitsEntry.COLUMN_NAME_VALUE,
				LimitsEntry.COLUMN_NAME_MIN_VALUE,
				LimitsEntry.COLUMN_NAME_MAX_VALUE,
				LimitsEntry.COLUMN_NAME_STEP,
				LimitsEntry.COLUMN_NAME_ACTIVE,}, 
				LimitsEntry.COLUMN_NAME_DRIVER_ID + " = " + driver.id, null, null, null, null);
		
		ArrayList<Limit> limits = new ArrayList<Limit>();
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				Limit l = new Limit();
				l.key = c.getString(0);
				l.title = c.getString(1);
				l.type = c.getString(2);
				l.value = c.getString(3);
				l.minValue = c.getString(4);
				l.maxValue = c.getString(5);
				l.step = c.getString(6);
				l.active = c.getInt(7) == 1 ? true : false;
				limits.add(l);
				c.moveToNext();
			}
		}
		c.close();
		db.close();
		dbHelper.close();
		
		return limits;
	}
	
	private String getTimeString(int hourOfDay, int minutes){
		Calendar datetime = Calendar.getInstance();
	    datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
	    datetime.set(Calendar.MINUTE, minutes);

	    String am_pm ="";
	    if (datetime.get(Calendar.AM_PM) == Calendar.AM)
	        am_pm = "AM";
	    else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
	        am_pm = "PM";

	    String strHrsToShow = datetime.get(Calendar.HOUR)<10 ? "0"+datetime.get(Calendar.HOUR) : ""+datetime.get(Calendar.HOUR);
	    String strMntToShow = datetime.get(Calendar.MINUTE)<10 ? "0"+datetime.get(Calendar.MINUTE) : ""+datetime.get(Calendar.MINUTE); 

	    return strHrsToShow+":"+strMntToShow+" "+am_pm;
	}
	
	@Override
	public void onResume() {
		showSavedToast = true;
		super.onResume();
	}
	
	public class Limit{

		public String key;
		public String title;
		public String type;
		public String value;
		public String minValue;
		public String maxValue;
		public String step;
		public boolean active;
		
	}

	class LimitsListGroup{
		String groupTitle;
		boolean enabled = false;
		ArrayList<LimitsListChild> childs;
		
		public LimitsListGroup(String groupTitle, boolean enabled,
				ArrayList<LimitsListChild> childs) {
			super();
			this.groupTitle = groupTitle;
			this.enabled = enabled;
			this.childs = childs;
		}
		public LimitsListGroup(String groupTitle, boolean enabled) {
			super();
			this.groupTitle = groupTitle;
			this.enabled = enabled;
			this.childs = new ArrayList<LimitsListChild>();
		}
	}
	
	class LimitsListChild{
		int layoutId;
		String text[];
		
		public LimitsListChild(int layoutId, String... text) {
			super();
			this.layoutId = layoutId;
			this.text = text;
		}
	}
	
	class LimitsSingleValueChild extends LimitsListChild{
		
		int value;
		
		public LimitsSingleValueChild(int value, String text) {
			super(R.layout.limits_edittext_item, text);
			this.value = value;
		}
	}
	
	class LimitsTimePeriodChild extends LimitsListChild{
		
		String startTime;
		String endTime;
		
		public LimitsTimePeriodChild(String startTime, String endTime, String... text) {
			super(R.layout.limits_edittext_double_item, text);
			this.startTime = startTime;
			this.endTime = endTime;
		}
	}
	
	class LimitsLinkChild extends LimitsListChild{
		
		Class<?> linkActivityClass;
		
		public LimitsLinkChild(Class<?> linkActivityClass, String... text) {
			super(R.layout.limits_link_item, text);
			this.linkActivityClass = linkActivityClass;
		}
	}
	
	class GetLimitsTask extends BaseRequestAsyncTask{
		
		public GetLimitsTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			llProgress.setVisibility(View.VISIBLE);
			content.setVisibility(View.GONE);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			llProgress.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
//	        requestParams.add(new BasicNameValuePair("driver_id", ""+driver.id));
//			return super.doInBackground(params);
			status = 200;
			return Utils.getJSONObjectFromAssets(getActivity(), "limits.json");
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			
			ArrayList<Limit> limits = new ArrayList<Limit>();
			if(responseJSON.has("limits")){
				JSONArray limitsJSON = responseJSON.getJSONArray("limits");
				for (int i = 0; i < limitsJSON.length(); i++) {
					JSONObject lJSON = limitsJSON.getJSONObject(i);
					Limit l = new Limit();
					l.key = lJSON.optString("key");
					l.title = lJSON.optString("title");
					l.type = lJSON.optString("type");
					l.value = lJSON.optString("value");
					l.minValue = lJSON.optString("min_value",lJSON.optString("value_from"));
					l.maxValue = lJSON.optString("max_value",lJSON.optString("value_to"));
					l.step = lJSON.optString("step");
					l.active = lJSON.optBoolean("active");
					limits.add(l);
				}
			}
			
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			dbHelper.saveLimits(driver.id, limits);
			dbHelper.close();
			
			updateLimits();
			
			super.onSuccess(responseJSON);
		}
	}
	
	private boolean showSavedToast = true;
	
	class SetLimitsTask extends BasePostRequestAsyncTask{
		
		public SetLimitsTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("driver_id", ""+driver.id));

//	        try{
//		        requestParams.add(new BasicNameValuePair("max_speed_limit", ""+groups.get(0).enabled));
//		        requestParams.add(new BasicNameValuePair("daily_mileage_limit", ""+groups.get(1).enabled));
//		        requestParams.add(new BasicNameValuePair("harsh_way", ""+groups.get(2).enabled));
//		        requestParams.add(new BasicNameValuePair("is_driving_between", ""+groups.get(3).enabled));
//		        requestParams.add(new BasicNameValuePair("is_geofence", ""+groups.get(4).enabled));
//		        requestParams.add(new BasicNameValuePair("low_fuel", ""+groups.get(5).enabled));
//		        requestParams.add(new BasicNameValuePair("safe_driving", ""+groups.get(6).enabled));
//		        requestParams.add(new BasicNameValuePair("tow_alerts", ""+groups.get(7).enabled));
//		        
//		        requestParams.add(new BasicNameValuePair("max_speed", ""+((LimitsSingleValueChild)groups.get(0).childs.get(0)).value));
//		        requestParams.add(new BasicNameValuePair("daily_mileage", ""+((LimitsSingleValueChild)groups.get(1).childs.get(0)).value));
//	
//		        requestParams.add(new BasicNameValuePair("driving_after", ""+((LimitsTimePeriodChild)groups.get(3).childs.get(0)).startTime));
//		        requestParams.add(new BasicNameValuePair("driving_before", ""+((LimitsTimePeriodChild)groups.get(3).childs.get(0)).endTime));
//	        }
//	        catch(NullPointerException | IndexOutOfBoundsException e){
//	        	e.printStackTrace();
//	        	showSavedToast = false;
//	        }
	        
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			if(showSavedToast && getActivity()!=null)
				Toast.makeText(getActivity(), "Limits saved", Toast.LENGTH_SHORT).show();
			super.onSuccess(responseJSON);
		}
	}
}
