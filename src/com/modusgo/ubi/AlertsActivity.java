package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class AlertsActivity extends MainActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_alerts);
		super.onCreate(savedInstanceState);

		setActionBarTitle("ALERTS");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Driver driver = DbHelper.getDrivers().get(prefs.getInt(Constants.PREF_CURRENT_DRIVER, 0));
		
		((TextView)findViewById(R.id.tvName)).setText(driver.name);
		((ImageView)findViewById(R.id.imagePhoto)).setImageResource(driver.imageId);
		
		findViewById(R.id.btnSwitchDriverMenu).setVisibility(View.GONE);
		
		findViewById(R.id.btnTimePeriod).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createDialog().show();
			}
		});
		
		
		ArrayList<Alert> alerts = new ArrayList<Alert>();
		alerts.add(new Alert(0,"Hard Braking", "07/05/2014 11:27 AM PST"));
		alerts.add(new Alert(1,"Oil Level Low", "07/01/2014 05:00 PM PST"));
		
		AlertsAdapter adapter = new AlertsAdapter(this, R.layout.alerts_item, alerts);
		
		ListView lvAlerts = (ListView)findViewById(R.id.listViewAlerts);
		lvAlerts.setAdapter(adapter);
	}
	
	String[] timePeriods = new String[]{"Last Month", "This Month", "All"};
	
	private Dialog createDialog() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Change time period")
	           .setItems(timePeriods, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	           }
	    });
	    return builder.create();
	}
	
	class Alert{
		
		int type;
		String eventName;
		String date;
		
		public Alert(int type, String eventName, String date) {
			super();
			this.type = type;
			this.eventName = eventName;
			this.date = date;
		}
		
	}
	
	class AlertsAdapter extends ArrayAdapter<Alert>{
		
		public AlertsAdapter(Context context, int resource, List<Alert> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View view = convertView;
		    if (view == null) {
		    	LayoutInflater lInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = lInflater.inflate(R.layout.alerts_item, parent, false);
		    }

		    ((TextView)view.findViewById(R.id.tvEvent)).setText(getItem(position).eventName);		    
		    ((TextView)view.findViewById(R.id.tvDate)).setText(getItem(position).date);
		    
		    switch (getItem(position).type) {
			case 0:
				((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_alerts_red);
				break;
			case 1:
				((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_diagnostics_red);
				break;
			default:
				break;
			}
			
			return view;
		}
		
	}
	
}
