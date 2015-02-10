package com.modusgo.twg;

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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.modusgo.twg.R;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.LimitsContract.LimitsEntry;
import com.modusgo.twg.db.VehicleContract.VehicleEntry;
import com.modusgo.twg.requesttasks.BasePostRequestAsyncTask;
import com.modusgo.twg.requesttasks.BaseRequestAsyncTask;
import com.modusgo.twg.utils.Utils;
import com.modusgo.twg.utils.Vehicle;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class LimitsFragment extends Fragment {
	
	Vehicle vehicle;
	
	LinearLayout content;
	LinearLayout llProgress;
	LayoutInflater inflater;
	
	ArrayList<Limit> limits;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout) inflater.inflate(
				R.layout.fragment_limits, container, false);

		this.inflater = inflater; 
		
		((MainActivity) getActivity()).setActionBarTitle("LIMITS");
		
		vehicle = ((DriverActivity)getActivity()).vehicle;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		rootView.findViewById(R.id.btnSwitchDriverMenu).setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_SWITCH_DRIVER_MENU_BUTTON_COLOR, Constants.SWITCH_DRIVER_BUTTON_BG_COLOR)));
		rootView.findViewById(R.id.bottom_line).setBackgroundColor(Color.parseColor(prefs.getString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, Constants.LIST_HEADER_LINE_COLOR)));
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(vehicle.name);
		
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    if(vehicle.photo == null || vehicle.photo.equals(""))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(vehicle.photo, imagePhoto, options);
	    }
		
		rootView.findViewById(R.id.btnSwitchDriverMenu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				((DriverActivity)getActivity()).menu.toggle();
			}
		});

		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);

		llProgress = (LinearLayout)rootView.findViewById(R.id.llProgress);
		content = (LinearLayout)rootView.findViewById(R.id.llContent);
		
		return rootView;
	}
	
	private void updateLimits(){
		content.removeAllViews();
		
		for (final Limit limit : limits) {
			View groupView = inflater.inflate(R.layout.limits_toggle_button_item, content, false);
			final LinearLayout childLayout = (LinearLayout) groupView.findViewById(R.id.llChilds);
			final ToggleButton btnToggle = (ToggleButton) groupView.findViewById(R.id.btnToggle);
			final LimitsListChild child;
			final View childView;
			
			btnToggle.setChecked(limit.active);
			
			if(limit.active){
				childLayout.setVisibility(View.VISIBLE);
			}
			
			groupView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					btnToggle.performClick();
				}
			});
			
			if(limit.type.equals("flag")){
				//groups.add(new LimitsListGroup(limit.title, limit.active));
			}
			else if(limit.type.equals("number_picker") || limit.type.equals("number_input")){
				int value = 0;
				try{
					value = (int) Float.parseFloat(limit.value);
				}
				catch(NumberFormatException e){
					e.printStackTrace();
				}
				
				child = new LimitsSingleValueChild(value, "Set max to");
				childView = inflater.inflate(child.layoutId, childLayout, false);
				
				childView.setClickable(true);
				//Block parent click listener
				childView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
					}
				});
				
				TextView tvChildTitle = (TextView) childView.findViewById(R.id.tvTitle);
				tvChildTitle.setText(child.text[0]);
				
				final TextView tvSingleValue = (TextView)childView.findViewById(R.id.tvValue);
				tvSingleValue.setText(((LimitsSingleValueChild)child).value+" ");
				tvSingleValue.setOnClickListener(new OnClickListener() {		
					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						// Get the layout inflater
						LayoutInflater inflater = getActivity().getLayoutInflater();
						
						// Inflate and set the layout for the dialog
						// Pass null as the parent view because its going in the dialog layout
						final View dialogContentView = inflater.inflate(R.layout.dialog_numbers, null);
						((TextView)dialogContentView.findViewById(R.id.editValue)).setText(""+((LimitsSingleValueChild)child).value);
						builder.setView(dialogContentView)
						// Add action buttons
						.setTitle(((TextView)childView.findViewById(R.id.tvValue)).getText())
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								final int value = Integer.valueOf(((TextView)dialogContentView.findViewById(R.id.editValue)).getText().toString());
								
								new SetLimitsTask(getActivity()){
									
									@Override
									protected void onPreExecute() {
										//btnToggle.setEnabled(false);
										Utils.enableDisableViewGroup(childLayout, false);
										requestParams.add(new BasicNameValuePair("vehicle_id",""+vehicle.id));
										requestParams.add(new BasicNameValuePair("key",limit.key));
										requestParams.add(new BasicNameValuePair("value",""+value));
										requestParams.add(new BasicNameValuePair("active",btnToggle.isChecked() ? "true" : "false"));

										tvSingleValue.setText(""+value);
										super.onPreExecute();
									}
									
									@Override
									protected void onSuccess(JSONObject responseJSON) throws JSONException {

										int newValue;
										try{
											newValue = (int) Float.parseFloat(limit.value);
										}
										catch(NumberFormatException e){
											newValue = 0;
										}
										
										limit.key = responseJSON.optString("key");
										limit.title = responseJSON.optString("title");
										limit.type = responseJSON.optString("type");
										limit.value = responseJSON.optString("value");
										limit.minValue = responseJSON.optString("min_value",responseJSON.optString("value_from"));
										limit.maxValue = responseJSON.optString("max_value",responseJSON.optString("value_to"));
										limit.step = responseJSON.optString("step");
										limit.active = responseJSON.optBoolean("active");
										
										DbHelper dbHelper = DbHelper.getInstance(getActivity());
										dbHelper.saveLimits(vehicle.id, limits);
										dbHelper.close();
										
										
										try{
											newValue = (int) Float.parseFloat(limit.value);
										}
										catch(NumberFormatException e){ 
											e.printStackTrace();
										}

										((LimitsSingleValueChild)child).value = newValue;
										tvSingleValue.setText(""+newValue);
											
										super.onSuccess(responseJSON);
									}
									
									@Override
									protected void onError(String message) {
										tvSingleValue.setText(limit.value);
										//super.onError(message);
									}
									
									@Override
									protected void onPostExecute(JSONObject result) {
										super.onPostExecute(result);
										//btnToggle.setEnabled(true);
										Utils.enableDisableViewGroup(childLayout, true);
									}
								}.execute("vehicles/"+vehicle.id+"/limits.json");
								
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
				childLayout.addView(childView);
				System.out.println("numbers "+(null==childView) + " and count = " + childLayout.getChildCount());
			}
			else if(limit.type.equals("time_between_picker")){
				child = new LimitsTimePeriodChild(limit.minValue, limit.maxValue, "Between", "and");
				childView = inflater.inflate(child.layoutId, childLayout, false);
				
				childView.setClickable(true);
				//Block parent click listener
				childView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
					}
				});
				
				TextView tvChildTitle = (TextView) childView.findViewById(R.id.tvTitle);
				tvChildTitle.setText(child.text[0]);
				
				SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
				final Calendar startCalendar = Calendar.getInstance();
				final Calendar endCalendar = Calendar.getInstance();
				try {
					startCalendar.setTime(sdf.parse(((LimitsTimePeriodChild)child).startTime));
					endCalendar.setTime(sdf.parse(((LimitsTimePeriodChild)child).endTime));
				} catch (ParseException e) {
					e.printStackTrace();
					startCalendar.set(2014, 0, 1, 10, 0);
					endCalendar.set(2014, 0, 1, 20, 0);
				}
				
				if(child.text[1]!=null){
					((TextView) childView.findViewById(R.id.tvTitle2)).setText(child.text[1]);
				}
				
				final TextView tvValue = (TextView)childView.findViewById(R.id.tvValue);
				tvValue.setText(getTime12String(startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE)));
				tvValue.setOnClickListener(new OnClickListener() {		
					@Override
					public void onClick(View v) {
						TimePickerDialog tpd = new TimePickerDialog(getActivity(), new OnTimeSetListener(){
							@Override
							public void onTimeSet(TimePicker arg0, final int hourOfDay, final int minutes) {
								new SetLimitsTask(getActivity()){
									
									@Override
									protected void onPreExecute() {
										btnToggle.setEnabled(false);
										Utils.enableDisableViewGroup(childLayout, false);
										requestParams.add(new BasicNameValuePair("vehicle_id",""+vehicle.id));
										requestParams.add(new BasicNameValuePair("key",limit.key));
										requestParams.add(new BasicNameValuePair("value_from",getTime12String(hourOfDay, minutes)));
										requestParams.add(new BasicNameValuePair("active",btnToggle.isChecked() ? "true" : "false"));
										
										tvValue.setText(getTime12String(hourOfDay, minutes));
										super.onPreExecute();
									}
									
									@Override
									protected void onSuccess(JSONObject responseJSON) throws JSONException {
										
										limit.key = responseJSON.optString("key");
										limit.title = responseJSON.optString("title");
										limit.type = responseJSON.optString("type");
										limit.value = responseJSON.optString("value");
										limit.minValue = responseJSON.optString("min_value",responseJSON.optString("value_from"));
										limit.maxValue = responseJSON.optString("max_value",responseJSON.optString("value_to"));
										limit.step = responseJSON.optString("step");
										limit.active = responseJSON.optBoolean("active");
										
										DbHelper dbHelper = DbHelper.getInstance(getActivity());
										dbHelper.saveLimits(vehicle.id, limits);
										dbHelper.close();

										startCalendar.set(2014, 0, 1, hourOfDay, minutes);
										tvValue.setText(getTime12String(hourOfDay, minutes));
										((LimitsTimePeriodChild)child).startTime = getTime12String(hourOfDay, minutes);
											
										super.onSuccess(responseJSON);
									}
									
									@Override
									protected void onError(String message) {
										tvValue.setText(getTime12String(startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE)));
										super.onError(message);
									}
									
									@Override
									protected void onPostExecute(JSONObject result) {
										super.onPostExecute(result);
										btnToggle.setEnabled(true);
										Utils.enableDisableViewGroup(childLayout, true);
									}
								}.execute("vehicles/"+vehicle.id+"/limits.json");
							}
						}, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), false);
						tpd.show();
					}
				});
				
				final TextView tvValue2 = (TextView)childView.findViewById(R.id.tvValue2);
				tvValue2.setText(getTime12String(endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE)));
				tvValue2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						TimePickerDialog tpd = new TimePickerDialog(getActivity(), new OnTimeSetListener(){
							@Override
							public void onTimeSet(TimePicker arg0, final int hourOfDay, final int minutes) {
								new SetLimitsTask(getActivity()){
									
									@Override
									protected void onPreExecute() {
//										btnToggle.setEnabled(false);
										Utils.enableDisableViewGroup(childLayout, false);
										requestParams.add(new BasicNameValuePair("vehicle_id",""+vehicle.id));
										requestParams.add(new BasicNameValuePair("key",limit.key));
										requestParams.add(new BasicNameValuePair("value_to",getTime12String(hourOfDay, minutes)));
										requestParams.add(new BasicNameValuePair("active",btnToggle.isChecked() ? "true" : "false"));
										
										tvValue2.setText(getTime12String(hourOfDay, minutes));
										super.onPreExecute();
									}
									
									@Override
									protected void onSuccess(JSONObject responseJSON) throws JSONException {
										
										limit.key = responseJSON.optString("key");
										limit.title = responseJSON.optString("title");
										limit.type = responseJSON.optString("type");
										limit.value = responseJSON.optString("value");
										limit.minValue = responseJSON.optString("min_value",responseJSON.optString("value_from"));
										limit.maxValue = responseJSON.optString("max_value",responseJSON.optString("value_to"));
										limit.step = responseJSON.optString("step");
										limit.active = responseJSON.optBoolean("active");
										
										DbHelper dbHelper = DbHelper.getInstance(getActivity());
										dbHelper.saveLimits(vehicle.id, limits);
										dbHelper.close();

										endCalendar.set(2014, 0, 1, hourOfDay, minutes);
										tvValue2.setText(getTime12String(hourOfDay, minutes));
										((LimitsTimePeriodChild)child).endTime = getTime12String(hourOfDay, minutes);
											
										super.onSuccess(responseJSON);
									}
									
									@Override
									protected void onError(String message) {
										tvValue2.setText(getTime12String(endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE)));
										super.onError(message);
									}
									
									@Override
									protected void onPostExecute(JSONObject result) {
										super.onPostExecute(result);
//										btnToggle.setEnabled(true);
										Utils.enableDisableViewGroup(childLayout, true);
									}
								}.execute("vehicles/"+vehicle.id+"/limits.json");
							}
						}, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false);
						tpd.show();
					}
				});
				childLayout.addView(childView);
			}
			else if(limit.type.equals("geofence")){
				child = new LimitsLinkChild(GeofenceActivity.class, "Set geofence");
				childView = inflater.inflate(child.layoutId, childLayout, false);
				
				childView.setClickable(true);
				//Block parent click listener
				childView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
					}
				});
				
				TextView tvChildTitle = (TextView) childView.findViewById(R.id.tvTitle);
				tvChildTitle.setText(child.text[0]);
				
				LinearLayout llContent = (LinearLayout) childView.findViewById(R.id.llContent);
				
				llContent.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(getActivity(), ((LimitsLinkChild)child).linkActivityClass);
						i.putExtra(VehicleEntry._ID, vehicle.id);
						startActivity(i);
					}
				});
				childLayout.addView(childView);
			}
			
			btnToggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					new SetLimitsTask(getActivity()){
						
						@Override
						protected void onPreExecute() {
							btnToggle.setEnabled(false);
							Utils.enableDisableViewGroup(childLayout, false);
							requestParams.add(new BasicNameValuePair("vehicle_id",""+vehicle.id));
							requestParams.add(new BasicNameValuePair("key",limit.key));
							requestParams.add(new BasicNameValuePair("active",btnToggle.isChecked() ? "true" : "false"));
							super.onPreExecute();
						}
						
						@Override
						protected void onSuccess(JSONObject responseJSON) throws JSONException {
							limit.key = responseJSON.optString("key");
							limit.title = responseJSON.optString("title");
							limit.type = responseJSON.optString("type");
							limit.value = responseJSON.optString("value");
							limit.minValue = responseJSON.optString("min_value",responseJSON.optString("value_from"));
							limit.maxValue = responseJSON.optString("max_value",responseJSON.optString("value_to"));
							limit.step = responseJSON.optString("step");
							limit.active = responseJSON.optBoolean("active");
							
							DbHelper dbHelper = DbHelper.getInstance(getActivity());
							dbHelper.saveLimits(vehicle.id, limits);
							dbHelper.close();
							
							if(limit.active){
								btnToggle.setChecked(true);
								childLayout.setVisibility(View.VISIBLE);
							}
							else{
								btnToggle.setChecked(false);
								childLayout.setVisibility(View.GONE);						
							}
								
							super.onSuccess(responseJSON);
						}
						
						@Override
						protected void onError(String message) {
							btnToggle.setChecked(limit.active);
							super.onError(message);
						}
						
						@Override
						protected void onPostExecute(JSONObject result) {
							super.onPostExecute(result);
							btnToggle.setEnabled(true);
							Utils.enableDisableViewGroup(childLayout, true);
						}
					}.execute("vehicles/"+vehicle.id+"/limits.json");
				}
			});
			
			((TextView)groupView.findViewById(R.id.tvTitle)).setText(limit.title);
			btnToggle.refreshDrawableState();
			btnToggle.invalidate();
			
			content.addView(groupView);
			content.invalidate();
			
		}
		
		if(vehicle.limitsBlocked){
			Utils.enableDisableViewGroup(content, false);
			View blockView = inflater.inflate(R.layout.limits_blocked_item, content, false);
			((TextView)blockView.findViewById(R.id.tvTitle)).setText("Blocked by "+vehicle.limitsBlockedBy);
			content.addView(blockView, 0);
		}
	}
	
	private ArrayList<Limit> getLimitsFromDB(){
		DbHelper dbHelper = DbHelper.getInstance(getActivity());
		SQLiteDatabase db = dbHelper.openDatabase();
		
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
				LimitsEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, null);
		
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
		dbHelper.closeDatabase();
		dbHelper.close();
		
		return limits;
	}
	
	private String getTime12String(int hourOfDay, int minutes){
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
	
	private String getTime24String(int hourOfDay, int minutes){

	    String strHrsToShow = hourOfDay<10 ? "0"+hourOfDay : ""+hourOfDay;
	    String strMntToShow = minutes<10 ? "0"+minutes : ""+minutes; 

	    return strHrsToShow+":"+strMntToShow+":00";
	}
	
	@Override
	public void onResume() {
		limits = getLimitsFromDB();
		if(limits.size()>0){
			updateLimits();
			content.invalidate();
		}
		
		new GetLimitsTask(getActivity()).execute("vehicles/"+vehicle.id+"/limits.json");
		
		Utils.gaTrackScreen(getActivity(), "Limits Screen");
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
			if(content.getChildCount()<=0){
				llProgress.setVisibility(View.VISIBLE);
				content.setVisibility(View.GONE);
			}
			else
				Utils.enableDisableViewGroup(content, false);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			llProgress.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
			Utils.enableDisableViewGroup(content, true);
			super.onPostExecute(result);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("driver_id", ""+vehicle.id));
			return super.doInBackground(params);
//			status = 200;
//			return Utils.getJSONObjectFromAssets(getActivity(), "limits.json");
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			if(responseJSON.has("limits")){
				limits.clear();
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
			dbHelper.saveLimits(vehicle.id, limits);
			dbHelper.close();
			
			updateLimits();
			
			super.onSuccess(responseJSON);
		}
	}
	
	class SetLimitsTask extends BasePostRequestAsyncTask{
		
		public SetLimitsTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("driver_id", ""+vehicle.id));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onError(String message) {
			//super.onError(message);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
//			if(getActivity()!=null)
//				Toast.makeText(getActivity(), "Limits saved", Toast.LENGTH_SHORT).show();
			super.onSuccess(responseJSON);
		}
	}
}
