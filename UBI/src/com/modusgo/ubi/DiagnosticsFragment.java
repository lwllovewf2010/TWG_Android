package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.SwipeLayout.DoubleClickListener;
import com.modusgo.ubi.db.DTCContract.DTCEntry;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.MaintenanceContract.MaintenanceEntry;
import com.modusgo.ubi.db.RecallContract.RecallEntry;
import com.modusgo.ubi.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.ubi.db.WarrantyInfoContract.WarrantyInfoEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.AnimationUtils;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DiagnosticsFragment extends Fragment{
	
	private static final String ERROR_STATUS_MESSAGE = "Gathering diagnostic information...";

	Vehicle vehicle;

	SwipeRefreshLayout lRefresh;
	LinearLayout llInfo;
	LinearLayout llContent;
	LinearLayout llProgress;
	LinearLayout llOdometer;
	LayoutInflater inflater;
	
	ImageView imageDTCAlert;
	TextView tvLastCheckup;
	TextView tvStatus;
	EditText editOdometer;
	
	SharedPreferences prefs;
	
	int maintenancesCount = 0;
	HashMap <String, Integer> maintenancesByMileage;
	HashMap <String, View> maintenancesHeaders;
	int recallCount = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_diagnostics, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("DIAGNOSTICS");
		
	    prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		vehicle = ((DriverActivity)getActivity()).vehicle;
		
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
				((DriverActivity)getActivity()).menu.toggle();
			}
		});

		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);

		this.inflater = inflater;
		
		lRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.lRefresh);
		llInfo = (LinearLayout) rootView.findViewById(R.id.llInfo);
		llContent = (LinearLayout) rootView.findViewById(R.id.llContent);
		llProgress = (LinearLayout) rootView.findViewById(R.id.llProgress);
		llOdometer = (LinearLayout) rootView.findViewById(R.id.llOdometer);
		
		imageDTCAlert = (ImageView) rootView.findViewById(R.id.imageAlerts);
		tvLastCheckup = (TextView) rootView.findViewById(R.id.tvLastCheckup);
		tvStatus = (TextView) rootView.findViewById(R.id.tvStatus);
		editOdometer = (EditText) rootView.findViewById(R.id.odometer);
		
		lRefresh.setColorSchemeResources(R.color.ubi_gray, R.color.ubi_green, R.color.ubi_orange, R.color.ubi_red);
		lRefresh.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				new GetDiagnosticsTask(getActivity()).execute("vehicles/"+vehicle.id+"/diagnostics.json");
			}
		});
		
		if(vehicle.odometer<=0){
		
			lRefresh.setVisibility(View.GONE);
			llOdometer.setVisibility(View.VISIBLE);
			
			Button btnSubmit = (Button) rootView.findViewById(R.id.btnSubmit);
			btnSubmit.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, Constants.BUTTON_BG_COLOR)));
			try{
				btnSubmit.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, Constants.BUTTON_TEXT_COLOR)));
			}
		    catch(Exception e){
		    	e.printStackTrace();
		    }
			
			btnSubmit.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try{
						int odo = Integer.parseInt(editOdometer.getText().toString());
						
						if(odo<=0){
							Toast.makeText(getActivity(), "Odometer value is too small", Toast.LENGTH_SHORT).show();
						}
						if(odo>999999){
							Toast.makeText(getActivity(), "Odometer value is too big", Toast.LENGTH_SHORT).show();
						}
						
						if(odo<=999999 && odo>0){
							
							vehicle.odometer = odo;
							
							TranslateAnimation slideDownOdometerLayoutAmination = new TranslateAnimation(
								      TranslateAnimation.RELATIVE_TO_PARENT,0.0f,
								      TranslateAnimation.RELATIVE_TO_PARENT,0.0f,
								      TranslateAnimation.RELATIVE_TO_PARENT,0.0f,
								      TranslateAnimation.RELATIVE_TO_PARENT,1f);
							slideDownOdometerLayoutAmination.setDuration(500);
							slideDownOdometerLayoutAmination.setFillAfter(true);
							slideDownOdometerLayoutAmination.setAnimationListener(new AnimationListener() {
								@Override
								public void onAnimationStart(Animation animation) {	
								}
								@Override
								public void onAnimationRepeat(Animation animation) {	
								}
								@Override
								public void onAnimationEnd(Animation animation) {
									llOdometer.setVisibility(View.GONE);
									lRefresh.startAnimation(AnimationUtils.getFadeInAnmation(getActivity(), lRefresh));
									new GetDiagnosticsTask(getActivity()).execute("vehicles/"+vehicle.id+"/diagnostics.json");
								}
							});
							llOdometer.startAnimation(slideDownOdometerLayoutAmination);
							
							InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

						    // check if no view has focus:
						    View view = getActivity().getCurrentFocus();
						    if (view != null) {
						        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						    }
						}
					}
					catch(NumberFormatException e){
						Toast.makeText(getActivity(), "Incorrect odometer value", Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
		else{
			updateInfo();
			
			if(llContent.getChildCount()==0){
				new GetDiagnosticsTask(getActivity()).execute("vehicles/"+vehicle.id+"/diagnostics.json");
			}
		}
		
		if(!prefs.getBoolean(Constants.PREF_DIAGNOSTICS_DELETE_POPUP_SHOWED, false)){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage("Swipe to delete")
	               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   prefs.edit().putBoolean(Constants.PREF_DIAGNOSTICS_DELETE_POPUP_SHOWED, true).commit();
	                       dialog.dismiss();
	                   }
	               });
	        builder.show();
		}
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		Utils.gaTrackScreen(getActivity(), "Diagnostics Screen");
		super.onResume();
	}
	
	private void updateInfo(){
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());

		try {
			tvLastCheckup.setText(sdfTo.format(sdfFrom.parse(vehicle.carLastCheckup)));
		} catch (ParseException e) {
			tvLastCheckup.setText("N/A");
			e.printStackTrace();
		}
		if(!TextUtils.isEmpty(vehicle.carCheckupStatus))
			tvStatus.setText(vehicle.carCheckupStatus);
		else
			tvStatus.setText(ERROR_STATUS_MESSAGE);
		
		llContent.removeAllViews();
		
		//--------------------------------------------- DTC ------------------------------------
		DbHelper dbHelper = DbHelper.getInstance(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor c = db.query(DTCEntry.TABLE_NAME, 
				new String[]{
				DTCEntry.COLUMN_NAME_CODE,
				DTCEntry.COLUMN_NAME_CONDITIONS,
				DTCEntry.COLUMN_NAME_CREATED_AT,
				DTCEntry.COLUMN_NAME_DESCRIPTION,
				DTCEntry.COLUMN_NAME_DETAILS,
				DTCEntry.COLUMN_NAME_FULL_DESCRIPTION,
				DTCEntry.COLUMN_NAME_IMPORTANCE,
				DTCEntry.COLUMN_NAME_LABOR_COST,
				DTCEntry.COLUMN_NAME_LABOR_HOURS,
				DTCEntry.COLUMN_NAME_PARTS,
				DTCEntry.COLUMN_NAME_PARTS_COST,
				DTCEntry.COLUMN_NAME_TOTAL_COST
				}, 
				ScoreGraphEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, null);
		
		if(c.moveToFirst()){
			imageDTCAlert.setImageResource(R.drawable.ic_alerts_red_big);
			View headerView = inflater.inflate(R.layout.diagnostics_header, llContent, false);
			headerView.findViewById(R.id.bottom_line).setBackgroundColor(Color.parseColor(prefs.getString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, Constants.LIST_HEADER_LINE_COLOR)));
			llContent.addView(headerView);
			
			while (!c.isAfterLast()) {
				final DiagnosticsTroubleCode dtc = new DiagnosticsTroubleCode(c.getString(0), 
						c.getString(1), 
						c.getString(2), 
						c.getString(3), 
						c.getString(4), 
						c.getString(5), 
						c.getString(6), 
						c.getString(7), 
						c.getString(8), 
						c.getString(9), 
						c.getString(10), 
						c.getString(11));
				View rowView = (LinearLayout) inflater.inflate(R.layout.diagnostics_item, llContent, false);
				SwipeLayout swipeLayout = (SwipeLayout) rowView.findViewById(R.id.lSwipe);
				swipeLayout.setSwipeEnabled(false);
				
				TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
				TextView tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
				TextView tvImportance = (TextView) rowView.findViewById(R.id.tvImportance);
				tvCode.setText(dtc.code);
				tvDescription.setText(dtc.description);
				tvImportance.setText(dtc.importance);
				switch (dtc.importance.toLowerCase(Locale.US)) {
				case "high":
					tvImportance.setTextColor(Color.parseColor("#ee4e43"));
					break;
				case "medium":
					tvImportance.setTextColor(Color.parseColor("#FBB040"));
					break;
				case "low":
					tvImportance.setTextColor(Color.parseColor("#00AEEF"));
					break;
				default:
					break;
				}
				
				rowView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(getActivity(), DiagnosticDetailActivity.class);
						i.putExtra(DiagnosticDetailActivity.EXTRA_DTC, dtc);
						startActivity(i);
					}
				});
				
				llContent.addView(rowView);
				c.moveToNext();
			}
		}
		else{
			imageDTCAlert.setImageResource(R.drawable.ic_alerts_green_big);
		}
		c.close();

		//--------------------------------------------- Recalls ------------------------------------
		c = db.query(RecallEntry.TABLE_NAME, 
				new String[]{
				RecallEntry._ID,
				RecallEntry.COLUMN_NAME_CONSEQUENCE,
				RecallEntry.COLUMN_NAME_CORRECTIVE_ACTION,
				RecallEntry.COLUMN_NAME_CREATED_AT,
				RecallEntry.COLUMN_NAME_DEFECT_DESCRIPTION,
				RecallEntry.COLUMN_NAME_DESCRIPTION,
				RecallEntry.COLUMN_NAME_RECALL_ID
				}, 
				RecallEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, null);
		
		if(c.moveToFirst()){
			final View headerView = inflater.inflate(R.layout.recall_header, llContent, false);
			llContent.addView(headerView);
			
			while (!c.isAfterLast()) {
				final Recall recall = new Recall(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6));
				final View rowView = (LinearLayout) inflater.inflate(R.layout.diagnostics_item, llContent, false);
				SwipeLayout swipeLayout = (SwipeLayout) rowView.findViewById(R.id.lSwipe);
				swipeLayout.setSwipeEnabled(true);
				swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
				swipeLayout.findViewById(R.id.btnDelete).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						recallCount--;
						llContent.removeView(rowView);
						new HideRecallTask(getActivity().getApplicationContext(), recall.id).execute("vehicles/"+vehicle.id+"/diagnostics/recall_updates/" + recall.id + "/hide.json");
						if(recallCount==0)
							llContent.removeView(headerView);
					}
				});
				
				TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
				TextView tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
				TextView tvImportance = (TextView) rowView.findViewById(R.id.tvImportance);
				tvCode.setText(recall.recall_id);
				tvDescription.setText(recall.description);
				tvImportance.setVisibility(View.GONE);
				
				rowView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						System.out.println("click");
						Intent i = new Intent(getActivity(), RecallActivity.class);
						i.putExtra(RecallActivity.EXTRA_RECALL, recall);
						startActivity(i);
						
					}
				});
				recallCount++;
				llContent.addView(rowView);
				c.moveToNext();
			}
		}
		c.close();
		
		//--------------------------------------------- Maintenances ------------------------------------
		c = db.query(MaintenanceEntry.TABLE_NAME, 
				new String[]{
				MaintenanceEntry._ID,
				MaintenanceEntry.COLUMN_NAME_CREATED_AT,
				MaintenanceEntry.COLUMN_NAME_DESCRIPTION,
				MaintenanceEntry.COLUMN_NAME_IMPORTANCE,
				MaintenanceEntry.COLUMN_NAME_MILEAGE,
				MaintenanceEntry.COLUMN_NAME_PRICE
				}, 
				MaintenanceEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, MaintenanceEntry.COLUMN_NAME_MILEAGE + " DESC");
		
		if(c.moveToFirst()){
			final View headerView = inflater.inflate(R.layout.diagnostics_header, llContent, false);
			headerView.findViewById(R.id.bottom_line).setBackgroundColor(Color.parseColor(prefs.getString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, Constants.LIST_HEADER_LINE_COLOR)));
			((TextView) headerView.findViewById(R.id.tvTitle)).setText("Scheduled Maintenance");
			llContent.addView(headerView);
			
			String lastMileage = "";
			maintenancesByMileage = new HashMap<String, Integer>();
			maintenancesHeaders = new HashMap<String, View>();
			
			while(!c.isAfterLast()){
				final Maintenance maintenance = new Maintenance(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5));
				
				if(!lastMileage.equals(maintenance.mileage)){
					lastMileage = maintenance.mileage;
					View mileageHeader = inflater.inflate(R.layout.scheduled_maintenance_header, llContent, false);
					String mileageUnit = "";
					if(prefs.getString(Constants.PREF_UNITS_OF_MEASURE, "mile").equals("mile"))
						mileageUnit = "miles";
					else
						mileageUnit = "km";
					((TextView)mileageHeader.findViewById(R.id.tvTitle)).setText(lastMileage + " " + mileageUnit);
					
					maintenancesHeaders.put(maintenance.mileage, mileageHeader);
					llContent.addView(mileageHeader);
				}
				
				if(maintenancesByMileage.containsKey(maintenance.mileage)){
					int value = maintenancesByMileage.get(maintenance.mileage);
					maintenancesByMileage.put(maintenance.mileage, value+1);
				}
				else
					maintenancesByMileage.put(maintenance.mileage, 1);
				
				final SwipeLayout rowView = (SwipeLayout) inflater.inflate(R.layout.scheduled_maintenance_item, llContent, false);
				rowView.setShowMode(SwipeLayout.ShowMode.PullOut);
				rowView.findViewById(R.id.btnDelete).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						maintenancesCount--;
						llContent.removeView(rowView);
						new HideMaintenanceTask(getActivity().getApplicationContext(), maintenance.id).execute("vehicles/"+vehicle.id+"/diagnostics/vehicle_maintenances/" + maintenance.id + "/hide.json");
						
						if(maintenancesByMileage.containsKey(maintenance.mileage)){
							int maintenanceByMileageCount = maintenancesByMileage.get(maintenance.mileage);
							maintenancesByMileage.put(maintenance.mileage, maintenanceByMileageCount-1);
							
							if(maintenancesByMileage.get(maintenance.mileage)==0){
								llContent.removeView(maintenancesHeaders.get(maintenance.mileage));
								maintenancesHeaders.remove(maintenance.mileage);
							}
						}
						
						if(maintenancesCount==0)
							llContent.removeView(headerView);
					}
				});
				
				TextView tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
				TextView tvImportance = (TextView) rowView.findViewById(R.id.tvImportance);
				tvDescription.setText(maintenance.description);
				tvImportance.setText(maintenance.importance);
				switch (maintenance.importance.toLowerCase(Locale.US)) {
				case "high":
					tvImportance.setTextColor(Color.parseColor("#ee4e43"));
					break;
				case "medium":
					tvImportance.setTextColor(Color.parseColor("#FBB040"));
					break;
				case "low":
					tvImportance.setTextColor(Color.parseColor("#00AEEF"));
					break;
				default:
					break;
				}
				maintenancesCount++;
				llContent.addView(rowView);
				c.moveToNext();
			}
		}
		c.close();

		//--------------------------------------------- Warranty Information ------------------------------------
		c = db.query(WarrantyInfoEntry.TABLE_NAME, 
				new String[]{
				WarrantyInfoEntry.COLUMN_NAME_CREATED_AT,
				WarrantyInfoEntry.COLUMN_NAME_DESCRIPTION,
				WarrantyInfoEntry.COLUMN_NAME_MILEAGE
				}, 
				WarrantyInfoEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, null);
		
		if(c.moveToFirst()){
			View headerView = inflater.inflate(R.layout.diagnostics_header, llContent, false);
			headerView.findViewById(R.id.bottom_line).setBackgroundColor(Color.parseColor(prefs.getString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, Constants.LIST_HEADER_LINE_COLOR)));
			((TextView) headerView.findViewById(R.id.tvTitle)).setText("Warranty Information");
			llContent.addView(headerView);
			
			while(!c.isAfterLast()){
				WarrantyInformation wi = new WarrantyInformation(c.getString(0), c.getString(1), c.getString(2));
				View rowView = (LinearLayout) inflater.inflate(R.layout.diagnostics_item, llContent, false);
				SwipeLayout swipeLayout = (SwipeLayout) rowView.findViewById(R.id.lSwipe);
				swipeLayout.setSwipeEnabled(false);
				
				
				TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
				TextView tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
				TextView tvImportance = (TextView) rowView.findViewById(R.id.tvImportance);
				View imageArrow = rowView.findViewById(R.id.imageArrow);
				imageArrow.setVisibility(View.GONE);
				tvCode.setText(wi.mileage);
				tvDescription.setText(wi.description);
				tvImportance.setVisibility(View.GONE);
				llContent.addView(rowView);
				c.moveToNext();
			}
		}
		c.close();
		
	}
	
	public class Maintenance {
		public long id;
		public String created_at;
		public String description;
		public String importance;
		public String mileage;
		public String price;
		
		public Maintenance(long id, String created_at, String description,
				String importance, String mileage, String price) {
			super();
			this.id = id;
			this.created_at = created_at;
			this.description = description;
			this.importance = importance;
			this.mileage = mileage;
			this.price = price;
		}
	}
	
	public class WarrantyInformation {
		public String created_at;
		public String description;
		public String mileage;
		
		public WarrantyInformation(String created_at, String description,
				String mileage) {
			super();
			this.created_at = created_at;
			this.description = description;
			this.mileage = mileage;
		}
	}
	
	class HideMaintenanceTask extends BaseRequestAsyncTask{
		
		long maintenanceId;
		
		public HideMaintenanceTask(Context context, long maintenanceId) {
			super(context);
			this.maintenanceId = maintenanceId;
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("driver_id", ""+vehicle.id));
	        requestParams.add(new BasicNameValuePair("vehicle_maintenance_id", ""+maintenanceId));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			dbHelper.deleteMaintenance(vehicle.id, maintenanceId);
			dbHelper.close();			
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			//Do nothing
		}
	}
	
	class HideRecallTask extends BaseRequestAsyncTask{
		
		long recallUpdateId;
		
		public HideRecallTask(Context context, long recallUpdateId) {
			super(context);
			this.recallUpdateId = recallUpdateId;
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("driver_id", ""+vehicle.id));
	        requestParams.add(new BasicNameValuePair("recall_update_id", ""+recallUpdateId));
	        System.out.println(requestParams);
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			dbHelper.deleteRecall(vehicle.id, recallUpdateId);
			dbHelper.close();			
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			System.out.println(message);
			//Do nothing
		}
	}
	
	class GetDiagnosticsTask extends BaseRequestAsyncTask{
		
		public GetDiagnosticsTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			lRefresh.setRefreshing(true);
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			lRefresh.setRefreshing(false);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
	        requestParams.add(new BasicNameValuePair("mileage", ""+vehicle.odometer));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onError(String message) {
//			super.onError(message);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			System.out.println(responseJSON);
			
			if(responseJSON.has("diagnostics")){
				JSONObject diagnosticsJSON = responseJSON.getJSONObject("diagnostics");
				
//				Editor e = prefs.edit();
//				e.putString(Constants.PREF_DIAGNOSTICS_CHECKUP_DATE+vehicle.id, Utils.fixTimezoneZ(diagnosticsJSON.optString("last_checkup")));
//				System.out.println(Constants.PREF_DIAGNOSTICS_CHECKUP_DATE+vehicle.id+" = "+Utils.fixTimezoneZ(diagnosticsJSON.optString("last_checkup")));
//				e.putString(Constants.PREF_DIAGNOSTICS_STATUS+vehicle.id, diagnosticsJSON.optString("checkup_status",ERROR_STATUS_MESSAGE));
//				e.commit();
				
				if(diagnosticsJSON.has("diagnostics_trouble_codes")){
					Object dtcsObject = diagnosticsJSON.get("diagnostics_trouble_codes");
					if(dtcsObject instanceof JSONArray){
						JSONArray dtcsJSON = (JSONArray)dtcsObject;
						ArrayList<DiagnosticsTroubleCode> dtcs = new ArrayList<DiagnosticsTroubleCode>();
						for (int i = 0; i < dtcsJSON.length(); i++) {
							
							JSONObject dtc = dtcsJSON.getJSONObject(i);
							
							dtcs.add(new DiagnosticsTroubleCode(
									dtc.optString("code"), 
									dtc.optString("conditions"), 
									Utils.fixTimezoneZ(dtc.optString("created_at")), 
									dtc.optString("description"), 
									dtc.optString("details"), 
									dtc.optString("full_description"), 
									dtc.optString("importance_text"), 
									dtc.optString("labor_cost"), 
									dtc.optString("labor_hours"), 
									dtc.optString("parts"), 
									dtc.optString("parts_cost"), 
									dtc.optString("total_cost")));
						}
						vehicle.carDTCCount = dtcs.size();
						
						dbHelper.saveDTCs(vehicle.id, dtcs);
					}
				}
				
				if(diagnosticsJSON.has("recall_updates")){
					Object recallsObject = diagnosticsJSON.get("recall_updates");
					if(recallsObject instanceof JSONArray){
						JSONArray recallsJSON = (JSONArray)recallsObject;
						ArrayList<Recall> recalls = new ArrayList<Recall>();
						for (int i = 0; i < recallsJSON.length(); i++) {
							
							JSONObject recall = recallsJSON.getJSONObject(i);
							
							recalls.add(new Recall(
									recall.optLong("id"),
									recall.optString("consequence"), 
									recall.optString("corrective_action"), 
									Utils.fixTimezoneZ(recall.optString("created_at")), 
									recall.optString("defect_description"), 
									recall.optString("description"), 
									recall.optString("recall_id")));
						}
						dbHelper.saveRecalls(vehicle.id, recalls);
					}
				}
				
				if(diagnosticsJSON.has("vehicle_maintenances")){
					Object maintenancesObject = diagnosticsJSON.get("vehicle_maintenances");
					if(maintenancesObject instanceof JSONArray){
						JSONArray maintenancesJSON = (JSONArray) maintenancesObject;
						ArrayList<Maintenance> maintenances = new ArrayList<Maintenance>();
						for (int i = 0; i < maintenancesJSON.length(); i++) {
							
							JSONObject maintenance = maintenancesJSON.getJSONObject(i);
							
							maintenances.add(new Maintenance(
									maintenance.optLong("id"),
									Utils.fixTimezoneZ(maintenance.optString("created_at")), 
									maintenance.optString("description"), 
									maintenance.optString("importance"), 
									Integer.toString(maintenance.optInt("mileage")), 
									maintenance.optString("price")));
						}
						dbHelper.saveMaintenances(vehicle.id, maintenances);
					}
				}
				
				if(diagnosticsJSON.has("diagnostics_warranty_informations")){
					Object warrantyInformationsObject = diagnosticsJSON.get("diagnostics_warranty_informations");
					if(warrantyInformationsObject instanceof JSONArray){
						JSONArray warrantyInformationsJSON = (JSONArray) warrantyInformationsObject;
						ArrayList<WarrantyInformation> warrantyInformation = new ArrayList<WarrantyInformation>();
						for (int i = 0; i < warrantyInformationsJSON.length(); i++) {
							
							JSONObject wInfoJSON = warrantyInformationsJSON.getJSONObject(i);
							
							warrantyInformation.add(new WarrantyInformation(
									wInfoJSON.optString("created_at"), 
									wInfoJSON.optString("description"), 
									wInfoJSON.optString("mileage")));
						}
						dbHelper.saveWarrantyInformation(vehicle.id, warrantyInformation);
					}
				}
				
				vehicle.carCheckupStatus = diagnosticsJSON.optString("checkup_status");
				vehicle.carLastCheckup = diagnosticsJSON.optString("last_checkup");
				dbHelper.saveVehicle(vehicle);
				
				updateInfo();
			}
			dbHelper.close();
			
			super.onSuccess(responseJSON);
		}
	}
}
