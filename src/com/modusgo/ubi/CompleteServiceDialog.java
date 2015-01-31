package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.MaintenanceContract.MaintenanceEntry;
import com.modusgo.ubi.db.ServicePerformedContract.ServicePerformedEntry;
import com.modusgo.ubi.utils.ServicePerformed;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.method.DateTimeKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class CompleteServiceDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener
{
	SharedPreferences prefs;
	ServiceLogActivity main = null;
	
	int maintenancesCount = 0;
	HashMap<String, Integer> maintenancesByMileage;
	HashMap<String, View> maintenancesHeaders;
	ArrayList<String> typeList = null;
	ArrayList<String> locationList = null;
	int recallCount = 0;
	private DbHelper dbHelper = null;
	private SQLiteDatabase db = null;
	private Cursor c = null;
	private DatePickerDialog picker = null;
	private Spinner typeSpinner = null;
	private Button dateBtn = null;
	private Button doneBtn = null;
	private Button cancelBtn = null;
	private Spinner locationSpinner = null;
	private TextView dateText = null;
	private EditText milageText = null;
	private String dateSelectedString = null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.dialog_complete_service, container, false);
		getDialog().setTitle(R.string.EnterServiceCompletion);

		main = ((ServiceLogActivity) getActivity());
		main.setActionBarTitle("DIAGNOSTICS");

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		dbHelper = DbHelper.getInstance(getActivity());
		db = dbHelper.openDatabase();
		
		Long vehicleId = main.vehicleId;


		typeSpinner = (Spinner) rootView.findViewById(R.id.complete_service_type_spinner);
		dateBtn = (Button) rootView.findViewById(R.id.complete_service_date_btn);
		locationSpinner = (Spinner) rootView.findViewById(R.id.complete_service_location_spinner);
		dateText = (TextView) rootView.findViewById(R.id.complete_service_date);
		milageText = (EditText)rootView.findViewById(R.id.complete_service_milage);
		cancelBtn = (Button)rootView.findViewById(R.id.complete_service_cancel_btn);
		doneBtn = (Button)rootView.findViewById(R.id.complete_service_done_btn);

		// --------------------------------Maintenances----------------------------
		c = db.query(MaintenanceEntry.TABLE_NAME, new String[]
		{ MaintenanceEntry._ID, MaintenanceEntry.COLUMN_NAME_CREATED_AT, MaintenanceEntry.COLUMN_NAME_DESCRIPTION,
				MaintenanceEntry.COLUMN_NAME_IMPORTANCE, MaintenanceEntry.COLUMN_NAME_MILEAGE,
				MaintenanceEntry.COLUMN_NAME_PRICE }, MaintenanceEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicleId,
				null, null, null, MaintenanceEntry.COLUMN_NAME_MILEAGE + " DESC");

		typeList = new ArrayList<String>();
		
		if(c.moveToFirst())
		{
			while(!c.isAfterLast())
			{
				typeList.add(c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_DESCRIPTION)));
				c.moveToNext();
			}
		}
		c.close();
		c = null;
		
		//Add in "Other"
		typeList.add(getResources().getString(R.string.Other));

		Calendar now = Calendar.getInstance();
		final DatePickerDialog picker = new DatePickerDialog(main, this, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		
		dateBtn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				picker.show();
			}
		});
		
		cancelBtn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});
		
		doneBtn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				String type = typeSpinner.getSelectedItem().toString();
				String location = locationSpinner.getSelectedItem().toString();
				Long milage = Long.parseLong(milageText.getEditableText().toString());
				ServicePerformed sp = new ServicePerformed(type, dateSelectedString, "Self-Performed", milage);
				dbHelper.saveServicePerformedEvent(sp);
				dismiss();
			}
		});
		
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(main,
				android.R.layout.simple_spinner_item, (List)typeList);
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(typeAdapter);
			
		locationList = new ArrayList<String>();
		locationList.add("[Selling Dealer]");
		locationList.add("Self-Performed"); 
		locationList.add("Other");
		
		ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(main,
				android.R.layout.simple_spinner_item, (List)locationList);
		locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locationSpinner.setAdapter(locationAdapter);
			
		return rootView;
	}


	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
	{
		dateSelectedString = ""+(monthOfYear+1)+"/"+dayOfMonth+"/"+year; //zero based month
		dateText.setText(dateSelectedString);
	}
	
}
