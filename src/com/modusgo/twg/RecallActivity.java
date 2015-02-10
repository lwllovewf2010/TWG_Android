package com.modusgo.twg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.modusgo.twg.R;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.VehicleContract.VehicleEntry;
import com.modusgo.twg.utils.Recall;
import com.modusgo.twg.utils.Utils;

public class RecallActivity extends MainActivity {
	
	public static final String EXTRA_RECALL = "recallInfo";
	
	long driverId = 0;
	
	Recall recall;
    
    TextView tvCode;
    TextView tvDate;
    LinearLayout llInfoList;
    LinearLayout llList;
    ScrollView scrollView;
    private Button btnContact = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_recall);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Recall Notice");
		
		if(savedInstanceState!=null){
			driverId = savedInstanceState.getLong(VehicleEntry._ID);
			recall = (Recall) savedInstanceState.getSerializable(EXTRA_RECALL);
		}
		else if(getIntent()!=null){
			driverId = getIntent().getLongExtra(VehicleEntry._ID,0);
			recall = (Recall) getIntent().getSerializableExtra(EXTRA_RECALL);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		vehicle = dHelper.getVehicleShort(driverId);
		dHelper.close();

		tvCode = (TextView) findViewById(R.id.tvCode);
		tvDate = (TextView) findViewById(R.id.tvDate);
		llInfoList = (LinearLayout)findViewById(R.id.llInfoList);
		llList = (LinearLayout)findViewById(R.id.llList);
		scrollView = (ScrollView)findViewById(R.id.svContent);
		btnContact = (Button)findViewById(R.id.contact_btn);
		
		btnContact.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				String uri = "tel: 18005551212";
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(uri));
				startActivity(intent);
			}
		});
		tvCode.setText("Recall id - "+recall.recall_id);
		
		if(!TextUtils.isEmpty(recall.created_at)){
			SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
			SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
			
			TimeZone tzFrom = TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE);
			sdfFrom.setTimeZone(tzFrom);
			TimeZone tzTo = TimeZone.getTimeZone(prefs.getString(Constants.PREF_TIMEZONE_OFFSET, Constants.DEFAULT_TIMEZONE));
			sdfTo.setTimeZone(tzTo);
			
			try {
				tvDate.setText("Recall Date: "+sdfTo.format(sdfFrom.parse(recall.created_at)));
			} catch (ParseException e) {
				tvDate.setText("Recall Date: "+recall.created_at);
				e.printStackTrace();
			}
		}
		else
			tvDate.setVisibility(View.GONE);
		
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
	}
	
	@Override
	protected void onResume() {
        Utils.gaTrackScreen(this, "Recall Screen");
		super.onResume();
	}
	
	@Override
	protected void setActionBarAppearance() {
		getActionBar().getCustomView().setBackgroundColor(Color.parseColor("#ef4136"));
		tvActionBarTitle.setTextColor(Color.parseColor("#FFFFFF"));
		Mode mMode = Mode.SRC_ATOP;
	    getResources().getDrawable(R.drawable.ic_arrow_left).setColorFilter(Color.parseColor("#FFFFFF"),mMode);
	    getResources().getDrawable(R.drawable.ic_menu).setColorFilter(Color.parseColor("#FFFFFF"),mMode);
	    getResources().getDrawable(R.drawable.ic_menu_close).setColorFilter(Color.parseColor("#FFFFFF"),mMode);
	    getResources().getDrawable(R.drawable.ic_map).setColorFilter(Color.parseColor("#FFFFFF"),mMode);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(VehicleEntry._ID, driverId);
		outState.putSerializable(EXTRA_RECALL, recall);
		super.onSaveInstanceState(outState);
	}
}