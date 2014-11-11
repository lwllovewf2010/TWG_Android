package com.modusgo.ubi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;

public class RecallActivity extends MainActivity {
	
	public static final String EXTRA_RECALL = "recallInfo";
	
	long driverId = 0;
	
	Recall recall;
    
    TextView tvCode;
    TextView tvDate;
    LinearLayout llInfoList;
    LinearLayout llList;
    ScrollView scrollView;
    Button btnContactService;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_recall);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Recall Notice");
		getActionBar().getCustomView().setBackgroundColor(Color.parseColor("#ef4136"));
		
		if(savedInstanceState!=null){
			driverId = savedInstanceState.getLong(VehicleEntry._ID);
			recall = (Recall) savedInstanceState.getSerializable(EXTRA_RECALL);
		}
		else if(getIntent()!=null){
			driverId = getIntent().getLongExtra(VehicleEntry._ID,0);
			recall = (Recall) getIntent().getSerializableExtra(EXTRA_RECALL);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		driver = dHelper.getDriverShort(driverId);
		dHelper.close();

		tvCode = (TextView) findViewById(R.id.tvCode);
		tvDate = (TextView) findViewById(R.id.tvDate);
		llInfoList = (LinearLayout)findViewById(R.id.llInfoList);
		llList = (LinearLayout)findViewById(R.id.llList);
		scrollView = (ScrollView)findViewById(R.id.svContent);
		btnContactService = (Button)findViewById(R.id.btnContactService);
		
		btnContactService.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, "#f15b2a")));
		btnContactService.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, "#edf1f9")));
		
		tvCode.setText("Recall id - "+recall.recall_id);
		
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
		try {
			tvDate.setText("Recall Date: "+sdfTo.format(sdfFrom.parse(recall.created_at)));
		} catch (ParseException e) {
			tvDate.setText("Recall Date: "+recall.created_at);
			e.printStackTrace();
		}
		
		LayoutInflater inflater = getLayoutInflater();
		
		if(recall.consequence != null && !recall.consequence.equals("")){
			View rowView = inflater.inflate(R.layout.diagnostics_detail_item, llList, false);
			TextView tvTitle = (TextView) rowView.findViewById(R.id.tvTitle);
			TextView tvText = (TextView) rowView.findViewById(R.id.tvText);
			
			tvTitle.setText("Consequences");
			tvText.setText(recall.consequence);
			llInfoList.addView(rowView);
		}
		
		if(recall.corrective_action != null && !recall.corrective_action.equals("")){
			View rowView = inflater.inflate(R.layout.diagnostics_detail_item, llList, false);
			TextView tvTitle = (TextView) rowView.findViewById(R.id.tvTitle);
			TextView tvText = (TextView) rowView.findViewById(R.id.tvText);
			
			tvTitle.setText("Corrective action");
			tvText.setText(recall.corrective_action);
			llInfoList.addView(rowView);
		}
		
		if(recall.defect_description != null && !recall.defect_description.equals("")){
			View rowView = inflater.inflate(R.layout.diagnostics_detail_item, llList, false);
			TextView tvTitle = (TextView) rowView.findViewById(R.id.tvTitle);
			TextView tvText = (TextView) rowView.findViewById(R.id.tvText);
			
			tvTitle.setText("Defect description");
			tvText.setText(recall.defect_description);
			llList.addView(rowView);
		}
		
		btnContactService.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent callIntent = new Intent(Intent.ACTION_VIEW);          
	            callIntent.setData(Uri.parse("tel:1-800-392-3673"));          
	            startActivity(callIntent);  
			}
		});
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(VehicleEntry._ID, driverId);
		outState.putSerializable(EXTRA_RECALL, recall);
		super.onSaveInstanceState(outState);
	}
}