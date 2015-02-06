package com.modusgo.ubi.jastec;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.modusgo.ubi.MainActivity;
import com.modusgo.ubi.R;

public class LogActivity extends MainActivity {

	public final static String ACTION_ONGING_LOG = "com.modusgo.log.ongoing";
	public final static String ACTION_LOGS = "com.modusgo.log";
	public final static String BROADCAST_INTENT_EXTRA_MESSAGE = "message";
	
	TextView tvOngoingLog;
	EditText editTextLogs;
	
	IntentFilter logIntentFilter;
	IntentFilter logOngoingIntentFilter;
	
	BroadcastReceiver brLog;
	BroadcastReceiver brLogOngoing;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_log);
		super.onCreate(savedInstanceState);
        
		setActionBarTitle("LOGS");

		tvOngoingLog = (TextView) findViewById(R.id.tvOngoingLog);
		editTextLogs = (EditText) findViewById(R.id.editTextLogs);
		
		logIntentFilter = new IntentFilter(ACTION_LOGS);
		logOngoingIntentFilter = new IntentFilter(ACTION_ONGING_LOG);
		
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
		
		brLog = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				String message = sdf.format(new Date()) + " : " + intent.getStringExtra(BROADCAST_INTENT_EXTRA_MESSAGE) + "\n";
				editTextLogs.setText(editTextLogs.getText() + message);
			}
		};
		
		brLogOngoing = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				tvOngoingLog.setText(intent.getStringExtra(BROADCAST_INTENT_EXTRA_MESSAGE));
			}
		};        
	}
	
	@Override
	protected void onResume() {
		registerReceiver(brLog, logIntentFilter);
		registerReceiver(brLogOngoing, logOngoingIntentFilter);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(brLog);
		unregisterReceiver(brLogOngoing);
		super.onPause();
	}

}
