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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DiagnosticsFragment extends Fragment{

	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;

	ScrollView svContent;
	LinearLayout llInfo;
	LinearLayout llContent;
	LinearLayout llProgress;
	LayoutInflater inflater;
	
	ImageView imageDTCAlert;
	TextView tvLastCheckup;
	TextView tvStatus;
	
	ArrayList<DiagnosticsTroubleCode> dtcs;
	ArrayList<Recall> recalls;
	ArrayList<Maintenance> maintenances;
	ArrayList<WarrantyInformation> warrantyInformations;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_diagnostics, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("DIAGNOSTICS");
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
		}
		else if(getArguments()!=null){
			driverIndex = getArguments().getInt("id");
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    if(driver.imageUrl == null || driver.imageUrl.equals(""))
	    	imagePhoto.setImageResource(driver.imageId);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(driver.imageUrl, imagePhoto, options);
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
		
		imageDTCAlert = (ImageView) rootView.findViewById(R.id.imageAlerts);
		tvLastCheckup = (TextView) rootView.findViewById(R.id.tvLastCheckup);
		tvStatus = (TextView) rootView.findViewById(R.id.tvStatus);
		
		
		new GetDiagnosticsTask(getActivity()).execute("drivers/"+driver.id+"/diagnostics.json");
		
		return rootView;
	}
	
	private void updateInfo(){
		
		//--------------------------------------------- DTC ------------------------------------
		
		if(dtcs!=null && dtcs.size()>0){
			imageDTCAlert.setImageResource(R.drawable.ic_alerts_red_big);
			llContent.addView(inflater.inflate(R.layout.diagnostics_header, llContent, false));
			
			for (final DiagnosticsTroubleCode dtc : dtcs) {
				View rowView = inflater.inflate(R.layout.diagnostics_item, llContent, false);
				TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
				TextView tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
				TextView tvImportance = (TextView) rowView.findViewById(R.id.tvImportance);
				tvCode.setText(dtc.code);
				tvDescription.setText(dtc.description);
				tvImportance.setText(dtc.importance);
				switch (dtc.importance.toLowerCase()) {
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
			}
		}
		else{
			imageDTCAlert.setImageResource(R.drawable.ic_alerts_green_big);
		}

		//--------------------------------------------- Recalls ------------------------------------
		
		if(recalls!=null && recalls.size()>0){
			llContent.addView(inflater.inflate(R.layout.recall_header, llContent, false));
			
			for (final Recall recall : recalls) {
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
			}
		}
		
		//--------------------------------------------- Maintenances ------------------------------------
		
		if(maintenances!=null && maintenances.size()>0){
			View header = inflater.inflate(R.layout.diagnostics_header, llContent, false);
			((TextView) header.findViewById(R.id.tvTitle)).setText("Scheduled Maintenance");
			llContent.addView(header);
			
			for (Maintenance maintenance : maintenances) {
				View rowView = inflater.inflate(R.layout.diagnostics_item, llContent, false);
				TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
				TextView tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
				TextView tvImportance = (TextView) rowView.findViewById(R.id.tvImportance);
				View imageArrow = rowView.findViewById(R.id.imageArrow);
				imageArrow.setVisibility(View.GONE);
				tvCode.setText(maintenance.mileage);
				tvDescription.setText(maintenance.description);
				tvImportance.setText(maintenance.importance);
				switch (maintenance.importance.toLowerCase()) {
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
			}
		}

		//--------------------------------------------- Warranty Information ------------------------------------
		
		if(warrantyInformations!=null && warrantyInformations.size()>0){
			View header = inflater.inflate(R.layout.diagnostics_header, llContent, false);
			((TextView) header.findViewById(R.id.tvTitle)).setText("Warranty Information");
			llContent.addView(header);
			
			for (WarrantyInformation warrantyInformation : warrantyInformations) {
				View rowView = inflater.inflate(R.layout.diagnostics_item, llContent, false);
				TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
				TextView tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
				TextView tvImportance = (TextView) rowView.findViewById(R.id.tvImportance);
				View imageArrow = rowView.findViewById(R.id.imageArrow);
				imageArrow.setVisibility(View.GONE);
				tvCode.setText(warrantyInformation.mileage);
				tvDescription.setText(warrantyInformation.description);
				tvImportance.setVisibility(View.GONE);
				llContent.addView(rowView);
			}
		}
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		super.onSaveInstanceState(outState);
	}
	
	class Maintenance {
		String created_at;
		String description;
		String importance;
		String mileage;
		String price;
		
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
	
	class WarrantyInformation {
		String created_at;
		String description;
		String mileage;
		
		public WarrantyInformation(String created_at, String description,
				String mileage) {
			super();
			this.created_at = created_at;
			this.description = description;
			this.mileage = mileage;
		}
	}
	
	class GetDiagnosticsTask extends BaseRequestAsyncTask{
		
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());
		
		public GetDiagnosticsTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			llProgress.setVisibility(View.VISIBLE);
			svContent.setVisibility(View.GONE);
			llInfo.setVisibility(View.GONE);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			llProgress.setVisibility(View.GONE);
			svContent.setVisibility(View.VISIBLE);
			llInfo.setVisibility(View.VISIBLE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("driver_id", ""+driver.id));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			String lastCheckup = responseJSON.getString("last_checkup");
			try {
				tvLastCheckup.setText(sdfTo.format(sdfFrom.parse(lastCheckup)));
			} catch (ParseException e) {
				tvLastCheckup.setText(lastCheckup);
				e.printStackTrace();
			}
			tvStatus.setText(responseJSON.getString("status_diagnostics"));
			
			JSONArray dtcsJSON = responseJSON.getJSONArray("diagnostics_trouble_codes");
			dtcs = new ArrayList<DiagnosticsTroubleCode>();
			for (int i = 0; i < dtcsJSON.length(); i++) {
				
				JSONObject dtc = dtcsJSON.getJSONObject(i);
				
				dtcs.add(new DiagnosticsTroubleCode(
						dtc.getString("code"), 
						dtc.getString("conditions"), 
						dtc.getString("created_at"), 
						dtc.getString("description"), 
						dtc.getString("details"), 
						dtc.getString("full_description"), 
						dtc.getString("importance"), 
						dtc.getString("labor_cost"), 
						dtc.getString("labor_hours"), 
						dtc.getString("parts"), 
						dtc.getString("parts_cost"), 
						dtc.getString("total_cost")));
			}
			
			JSONArray recallsJSON = responseJSON.getJSONArray("recall_updates");
			recalls = new ArrayList<Recall>();
			for (int i = 0; i < recallsJSON.length(); i++) {
				
				JSONObject recall = recallsJSON.getJSONObject(i);
				
				recalls.add(new Recall(
						recall.getString("consequence"), 
						recall.getString("corrective_action"), 
						recall.getString("created_at"), 
						recall.getString("defect_description"), 
						recall.getString("description"), 
						recall.getString("recall_id")));
			}
			
			JSONArray maintenancesJSON = responseJSON.getJSONArray("vehicle_maintenances");
			maintenances = new ArrayList<Maintenance>();
			for (int i = 0; i < maintenancesJSON.length(); i++) {
				
				JSONObject maintenance = maintenancesJSON.getJSONObject(i);
				
				maintenances.add(new Maintenance(
						maintenance.getString("created_at"), 
						maintenance.getString("description"), 
						maintenance.getString("importance"), 
						maintenance.getString("mileage"), 
						maintenance.getString("price")));
			}
			
			JSONArray warrantyInformationsJSON = responseJSON.getJSONArray("diagnostics_warranty_informations");
			warrantyInformations = new ArrayList<WarrantyInformation>();
			for (int i = 0; i < warrantyInformationsJSON.length(); i++) {
				
				JSONObject warrantyInformation = warrantyInformationsJSON.getJSONObject(i);
				
				warrantyInformations.add(new WarrantyInformation(
						warrantyInformation.getString("created_at"), 
						warrantyInformation.getString("description"), 
						warrantyInformation.getString("mileage")));
			}
			
			updateInfo();
			super.onSuccess(responseJSON);
		}
	}
}
