package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.modusgo.demo.R;
import com.modusgo.ubi.db.DTCContract.DTCEntry;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.MaintenanceContract.MaintenanceEntry;
import com.modusgo.ubi.db.RecallContract.RecallEntry;
import com.modusgo.ubi.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.ubi.db.WarrantyInfoContract.WarrantyInfoEntry;
import com.modusgo.ubi.utils.AnimationUtils;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DiagnosticsFragment extends Fragment{
	
	private static final String ERROR_STATUS_MESSAGE = "Gathering diagnostic information...";

	Driver driver;

	ScrollView svContent;
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_diagnostics, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("DIAGNOSTICS");
		
	    prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
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

		this.inflater = inflater;
		
		svContent = (ScrollView) rootView.findViewById(R.id.svContent);
		llInfo = (LinearLayout) rootView.findViewById(R.id.llInfo);
		llContent = (LinearLayout) rootView.findViewById(R.id.llContent);
		llProgress = (LinearLayout) rootView.findViewById(R.id.llProgress);
		llOdometer = (LinearLayout) rootView.findViewById(R.id.llOdometer);
		
		imageDTCAlert = (ImageView) rootView.findViewById(R.id.imageAlerts);
		tvLastCheckup = (TextView) rootView.findViewById(R.id.tvLastCheckup);
		tvStatus = (TextView) rootView.findViewById(R.id.tvStatus);
		editOdometer = (EditText) rootView.findViewById(R.id.odometer);
		
		if(driver.odometer<=0){
		
			llInfo.setVisibility(View.GONE);
			llOdometer.setVisibility(View.VISIBLE);
			
			rootView.findViewById(R.id.btnSubmit).setOnClickListener(new OnClickListener() {
				
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
							
							driver.odometer = odo;
							
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
									new GetDiagnosticsTask(getActivity()).execute("vehicles/"+driver.id+"/diagnostics.json");
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
			if(!prefs.getString(Constants.PREF_DIAGNOSTICS_STATUS+driver.id, "").equals("")){
				updateInfo();
			}
			else{
				new GetDiagnosticsTask(getActivity()).execute("vehicles/"+driver.id+"/diagnostics.json");				
			}
		}
		
		return rootView;
	}
	
	private void updateInfo(){
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());

		String lastCheckup = prefs.getString(Constants.PREF_DIAGNOSTICS_CHECKUP_DATE+driver.id, "N/A");
		try {
			tvLastCheckup.setText(sdfTo.format(sdfFrom.parse(lastCheckup)));
		} catch (ParseException e) {
			tvLastCheckup.setText(lastCheckup);
			e.printStackTrace();
		}
		tvStatus.setText(prefs.getString(Constants.PREF_DIAGNOSTICS_STATUS+driver.id,ERROR_STATUS_MESSAGE));
		
		//--------------------------------------------- DTC ------------------------------------
		DbHelper dbHelper = DbHelper.getInstance(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor c = db.query(DTCEntry.TABLE_NAME, 
				new String[]{
				DTCEntry.COLUMN_NAME_CODE,
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
				ScoreGraphEntry.COLUMN_NAME_DRIVER_ID + " = " + driver.id, null, null, null, null);
		
		if(c.moveToFirst()){
			imageDTCAlert.setImageResource(R.drawable.ic_alerts_red_big);
			llContent.addView(inflater.inflate(R.layout.diagnostics_header, llContent, false));
			
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
				View rowView = inflater.inflate(R.layout.diagnostics_item, llContent, false);
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
				RecallEntry.COLUMN_NAME_CONSEQUENCE,
				RecallEntry.COLUMN_NAME_CORRECTIVE_ACTION,
				RecallEntry.COLUMN_NAME_CREATED_AT,
				RecallEntry.COLUMN_NAME_DEFECT_DESCRIPTION,
				RecallEntry.COLUMN_NAME_DESCRIPTION,
				RecallEntry.COLUMN_NAME_RECALL_ID
				}, 
				RecallEntry.COLUMN_NAME_DRIVER_ID + " = " + driver.id, null, null, null, null);
		
		if(c.moveToFirst()){
			llContent.addView(inflater.inflate(R.layout.recall_header, llContent, false));
			
			while (!c.isAfterLast()) {
				final Recall recall = new Recall(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5));
				View rowView = inflater.inflate(R.layout.diagnostics_item, llContent, false);
				TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
				TextView tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
				TextView tvImportance = (TextView) rowView.findViewById(R.id.tvImportance);
				tvCode.setText(recall.recall_id);
				tvDescription.setText(recall.description);
				tvImportance.setVisibility(View.GONE);
				llContent.addView(rowView);
				
				rowView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(getActivity(), RecallActivity.class);
						i.putExtra(RecallActivity.EXTRA_RECALL, recall);
						startActivity(i);
					}
				});
				c.moveToNext();
			}
		}
		c.close();
		
		//--------------------------------------------- Maintenances ------------------------------------
		c = db.query(MaintenanceEntry.TABLE_NAME, 
				new String[]{
				MaintenanceEntry.COLUMN_NAME_CREATED_AT,
				MaintenanceEntry.COLUMN_NAME_DESCRIPTION,
				MaintenanceEntry.COLUMN_NAME_IMPORTANCE,
				MaintenanceEntry.COLUMN_NAME_MILEAGE,
				MaintenanceEntry.COLUMN_NAME_PRICE
				}, 
				MaintenanceEntry.COLUMN_NAME_DRIVER_ID + " = " + driver.id, null, null, null, null);
		
		if(c.moveToFirst()){
			View header = inflater.inflate(R.layout.diagnostics_header, llContent, false);
			((TextView) header.findViewById(R.id.tvTitle)).setText("Scheduled Maintenance");
			llContent.addView(header);
			
			while(!c.isAfterLast()){
				Maintenance maintenance = new Maintenance(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
				View rowView = inflater.inflate(R.layout.diagnostics_item, llContent, false);
				TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
				TextView tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
				TextView tvImportance = (TextView) rowView.findViewById(R.id.tvImportance);
				View imageArrow = rowView.findViewById(R.id.imageArrow);
				imageArrow.setVisibility(View.GONE);
				tvCode.setText(maintenance.mileage);
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
				WarrantyInfoEntry.COLUMN_NAME_DRIVER_ID + " = " + driver.id, null, null, null, null);
		
		if(c.moveToFirst()){
			View header = inflater.inflate(R.layout.diagnostics_header, llContent, false);
			((TextView) header.findViewById(R.id.tvTitle)).setText("Warranty Information");
			llContent.addView(header);
			
			while(!c.isAfterLast()){
				WarrantyInformation wi = new WarrantyInformation(c.getString(0), c.getString(1), c.getString(2));
				View rowView = inflater.inflate(R.layout.diagnostics_item, llContent, false);
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
		public String created_at;
		public String description;
		public String importance;
		public String mileage;
		public String price;
		
		public Maintenance(String created_at, String description,
				String importance, String mileage, String price) {
			super();
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
	
	class GetDiagnosticsTask extends BaseRequestAsyncTask{
		
		Animation fadeInProgress;
		Animation fadeOutProgress;
		Animation fadeInInfo;
		Animation fadeOutInfo;
		Animation fadeInContent;
		Animation fadeOutContent;
		
		public GetDiagnosticsTask(Context context) {
			super(context);
			fadeInProgress = AnimationUtils.getFadeInAnmation(getActivity(), llProgress);
			fadeOutProgress = AnimationUtils.getFadeOutAnmation(getActivity(), llProgress);
			fadeInInfo = AnimationUtils.getFadeInAnmation(getActivity(), llInfo);
			fadeOutInfo = AnimationUtils.getFadeOutAnmation(getActivity(), llInfo);
			fadeInContent = AnimationUtils.getFadeInAnmation(getActivity(), svContent);
			fadeOutContent = AnimationUtils.getFadeOutAnmation(getActivity(), svContent);
		}
		
		@Override
		protected void onPreExecute() {
			llProgress.startAnimation(fadeInProgress);
			llInfo.startAnimation(fadeOutInfo);
			svContent.startAnimation(fadeOutContent);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			llProgress.startAnimation(fadeOutProgress);
			llInfo.startAnimation(fadeInInfo);
			svContent.startAnimation(fadeInContent);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("driver_id", ""+driver.id));
	        requestParams.add(new BasicNameValuePair("mileage", ""+driver.odometer));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onError(String message) {
			tvLastCheckup.setText("N/A");
			tvStatus.setText(ERROR_STATUS_MESSAGE);
//			super.onError(message);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			dbHelper.saveDriver(driver);
			
			System.out.println(responseJSON);
			
			if(responseJSON.has("diagnostics")){
				JSONObject diagnosticsJSON = responseJSON.getJSONObject("diagnostics");
				
				Editor e = prefs.edit();
				e.putString(Constants.PREF_DIAGNOSTICS_STATUS+driver.id, Utils.fixTimezoneZ(diagnosticsJSON.optString("last_checkup")));
				e.putString(Constants.PREF_DIAGNOSTICS_STATUS+driver.id, diagnosticsJSON.optString("status_diagnostics",ERROR_STATUS_MESSAGE));
				e.commit();
				
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
									dtc.optString("importance"), 
									dtc.optString("labor_cost"), 
									dtc.optString("labor_hours"), 
									dtc.optString("parts"), 
									dtc.optString("parts_cost"), 
									dtc.optString("total_cost")));
						}
						dbHelper.saveDTCs(driver.id, dtcs);
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
									recall.optString("consequence"), 
									recall.optString("corrective_action"), 
									Utils.fixTimezoneZ(recall.optString("created_at")), 
									recall.optString("defect_description"), 
									recall.optString("description"), 
									recall.optString("recall_id")));
						}
						dbHelper.saveRecalls(driver.id, recalls);
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
									Utils.fixTimezoneZ(maintenance.optString("created_at")), 
									maintenance.optString("description"), 
									maintenance.optString("importance"), 
									maintenance.optString("mileage"), 
									maintenance.optString("price")));
						}
						dbHelper.saveMaintenances(driver.id, maintenances);
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
						dbHelper.saveWarrantyInformation(driver.id, warrantyInformation);
					}
				}
				
				updateInfo();
			}
			dbHelper.close();
			
			super.onSuccess(responseJSON);
		}
	}
}
