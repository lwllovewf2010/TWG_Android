package com.modusgo.ubi;

import java.util.Locale;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DiagnosticDetailActivity extends MainActivity {
	
	public static final String EXTRA_DTC = "diagnosticsTroubleCodeInfo";
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	
	DiagnosticsTroubleCode dtc;
    
    TextView tvCode;
    TextView tvImportance;
    TextView tvDescription;
    TextView tvLaborHours;
    TextView tvEstLaborCost;
    TextView tvEstPartsCost;
    TextView tvEstTotalCost;
    LinearLayout llList;
    ScrollView scrollView;
    Button btnFindMechanic;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_diagnostic_detail);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("DIAGNOSTIC DETAILS");
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
			dtc = (DiagnosticsTroubleCode) savedInstanceState.getSerializable(EXTRA_DTC);
		}
		else if(getIntent()!=null){
			driverIndex = getIntent().getIntExtra("id",0);
			dtc = (DiagnosticsTroubleCode) getIntent().getSerializableExtra(EXTRA_DTC);
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);

		tvCode = (TextView) findViewById(R.id.tvCode);
		tvImportance = (TextView) findViewById(R.id.tvImportance);
		tvDescription = (TextView) findViewById(R.id.tvDescription);
		tvLaborHours = (TextView) findViewById(R.id.tvLaborHours);
		tvEstLaborCost = (TextView) findViewById(R.id.tvEstLaborCost);
		tvEstPartsCost = (TextView) findViewById(R.id.tvEstPartsCost);
		tvEstTotalCost = (TextView) findViewById(R.id.tvEstTotalCost);
		llList = (LinearLayout)findViewById(R.id.llList);
		scrollView = (ScrollView)findViewById(R.id.svContent);
		btnFindMechanic = (Button)findViewById(R.id.btnFindMechanic);
		
		tvCode.setText(dtc.code+" - "+dtc.description);
		tvDescription.setText(dtc.full_description);
		tvLaborHours.setText(dtc.labor_hours);
		tvEstLaborCost.setText("$"+dtc.labor_cost);
		tvEstPartsCost.setText("$"+dtc.parts_cost);
		tvEstTotalCost.setText("$"+dtc.total_cost);
		
		switch (dtc.importance.toLowerCase(Locale.US)) {
		case "high":
			tvImportance.setTextColor(Color.parseColor("#ee4e43"));
			tvImportance.setText("High importance");
			break;
		case "medium":
			tvImportance.setTextColor(Color.parseColor("#FBB040"));
			tvImportance.setText("Medium importance");
			break;
		case "low":
			tvImportance.setTextColor(Color.parseColor("#00AEEF"));
			tvImportance.setText("Low importance");
			break;
		default:
			break;
		}
		
		LayoutInflater inflater = getLayoutInflater();
		
		if(dtc.parts != null && !dtc.parts.equals("")){
			View rowView = inflater.inflate(R.layout.diagnostics_detail_item, llList, false);
			TextView tvTitle = (TextView) rowView.findViewById(R.id.tvTitle);
			TextView tvText = (TextView) rowView.findViewById(R.id.tvText);
			
			tvTitle.setText("Parts");
			tvText.setText(dtc.parts);
			llList.addView(rowView);
		}
		
		if(dtc.details != null && !dtc.details.equals("")){
			View rowView = inflater.inflate(R.layout.diagnostics_detail_item, llList, false);
			TextView tvTitle = (TextView) rowView.findViewById(R.id.tvTitle);
			TextView tvText = (TextView) rowView.findViewById(R.id.tvText);
			
			tvTitle.setText("Details");
			tvText.setText(dtc.details);
			llList.addView(rowView);
		}
		
		if(dtc.conditions != null && !dtc.conditions.equals("")){
			View rowView = inflater.inflate(R.layout.diagnostics_detail_item, llList, false);
			TextView tvTitle = (TextView) rowView.findViewById(R.id.tvTitle);
			TextView tvText = (TextView) rowView.findViewById(R.id.tvText);
			
			tvTitle.setText("Conditions");
			tvText.setText(dtc.conditions);
			llList.addView(rowView);
		}
		
		btnFindMechanic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(DiagnosticDetailActivity.this, FindMechanicActivity.class));
			}
		});
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		outState.putSerializable(EXTRA_DTC, dtc);
		super.onSaveInstanceState(outState);
	}
}