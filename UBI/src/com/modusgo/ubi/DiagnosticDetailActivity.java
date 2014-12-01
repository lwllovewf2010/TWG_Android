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

import com.modusgo.ubi.utils.Utils;

public class DiagnosticDetailActivity extends MainActivity {
	
	public static final String EXTRA_DTC = "diagnosticsTroubleCodeInfo";
	
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
			dtc = (DiagnosticsTroubleCode) savedInstanceState.getSerializable(EXTRA_DTC);
		}
		else if(getIntent()!=null){
			dtc = (DiagnosticsTroubleCode) getIntent().getSerializableExtra(EXTRA_DTC);
		}

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
		
		tvCode.setText(dtc.code);
		tvDescription.setText(dtc.description);
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
		
		if(prefs.getBoolean(Constants.PREF_FIND_MECHANIC_ENABLED, false)){
			btnFindMechanic.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(DiagnosticDetailActivity.this, FindMechanicActivity.class));
				}
			});
			btnFindMechanic.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, Constants.BUTTON_BG_COLOR)));
			try{
				btnFindMechanic.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, Constants.BUTTON_TEXT_COLOR)));
			}
		    catch(Exception e){
		    	e.printStackTrace();
		    }
		}
		else{
			btnFindMechanic.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onResume() {
        Utils.gaTrackScreen(this, "Diagnostic Details Screen");
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(EXTRA_DTC, dtc);
		super.onSaveInstanceState(outState);
	}
}