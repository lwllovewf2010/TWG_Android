package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class LimitsFragment extends Fragment {
	
	Driver driver;
	
	LinearLayout content;
	LinearLayout llProgress;
	LayoutInflater inflater;
	
	ArrayList<LimitsListGroup> groups;
	
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
						tvSingleValue.setText(c.value+" "+c.measurePoint);
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
							    		tvSingleValue.setText(value+" "+c.measurePoint);
							    		c.value = value;
							        }
							    })
							    .setNegativeButton("Calcel", new DialogInterface.OnClickListener() {
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
	
	@Override
	public void onPause() {
		if(groups!=null)
			new SetLimitsTask(getActivity()).execute("vehicles/"+driver.id+"/limits.json");
		super.onPause();
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
		String measurePoint;
		
		public LimitsSingleValueChild(int value, String meausrePoint, String text) {
			super(R.layout.limits_edittext_item, text);
			this.value = value;
			this.measurePoint = meausrePoint;
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
	        requestParams.add(new BasicNameValuePair("driver_id", ""+driver.id));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			
			ArrayList<LimitsListChild> maxSpeedLimitsChildren = new ArrayList<LimitsListChild>();
			ArrayList<LimitsListChild> dailyMileageLimitsChildren = new ArrayList<LimitsListChild>();
			ArrayList<LimitsListChild> timeOfDayLimitsChildren = new ArrayList<LimitsListChild>();
			ArrayList<LimitsListChild> geofenceChildren = new ArrayList<LimitsListChild>();
			maxSpeedLimitsChildren.add(new LimitsSingleValueChild(responseJSON.optInt("max_speed"), "MPH", "Set max to"));
			dailyMileageLimitsChildren.add(new LimitsSingleValueChild(responseJSON.optInt("daily_mileage"), "MI", "Set max to"));
			timeOfDayLimitsChildren.add(new LimitsTimePeriodChild(responseJSON.optString("driving_after", "08:00 AM"), responseJSON.optString("driving_before", "09:00 PM"), "Between", "and"));
			
			geofenceChildren.add(new LimitsLinkChild(GeofenceActivity.class, "Set geofence"));
			
			groups = new ArrayList<LimitsListGroup>();
			groups.add(new LimitsListGroup("Max speed limit", responseJSON.optBoolean("max_speed_limit"), maxSpeedLimitsChildren));
			groups.add(new LimitsListGroup("Daily mileage limit", responseJSON.optBoolean("daily_mileage_limit"), dailyMileageLimitsChildren));
			groups.add(new LimitsListGroup("Harsh event alerts", responseJSON.optBoolean("harsh_way")));
			groups.add(new LimitsListGroup("Time of day limits", responseJSON.optBoolean("is_driving_between"), timeOfDayLimitsChildren));
			groups.add(new LimitsListGroup("Geofence", responseJSON.optBoolean("is_geofence"), geofenceChildren));
			groups.add(new LimitsListGroup("Low fuel", responseJSON.optBoolean("low_fuel")));
			groups.add(new LimitsListGroup("Distracted Driving Events", responseJSON.optBoolean("safe_driving")));
			groups.add(new LimitsListGroup("Tow Alerts", responseJSON.optBoolean("tow_alerts")));
			
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
	        

	        try{
		        requestParams.add(new BasicNameValuePair("max_speed_limit", ""+groups.get(0).enabled));
		        requestParams.add(new BasicNameValuePair("daily_mileage_limit", ""+groups.get(1).enabled));
		        requestParams.add(new BasicNameValuePair("harsh_way", ""+groups.get(2).enabled));
		        requestParams.add(new BasicNameValuePair("is_driving_between", ""+groups.get(3).enabled));
		        requestParams.add(new BasicNameValuePair("is_geofence", ""+groups.get(4).enabled));
		        requestParams.add(new BasicNameValuePair("low_fuel", ""+groups.get(5).enabled));
		        requestParams.add(new BasicNameValuePair("safe_driving", ""+groups.get(6).enabled));
		        requestParams.add(new BasicNameValuePair("tow_alerts", ""+groups.get(7).enabled));
		        
		        requestParams.add(new BasicNameValuePair("max_speed", ""+((LimitsSingleValueChild)groups.get(0).childs.get(0)).value));
		        requestParams.add(new BasicNameValuePair("daily_mileage", ""+((LimitsSingleValueChild)groups.get(1).childs.get(0)).value));
	
		        requestParams.add(new BasicNameValuePair("driving_after", ""+((LimitsTimePeriodChild)groups.get(3).childs.get(0)).startTime));
		        requestParams.add(new BasicNameValuePair("driving_before", ""+((LimitsTimePeriodChild)groups.get(3).childs.get(0)).endTime));
	        }
	        catch(NullPointerException | IndexOutOfBoundsException e){
	        	e.printStackTrace();
	        	showSavedToast = false;
	        }
	        
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
