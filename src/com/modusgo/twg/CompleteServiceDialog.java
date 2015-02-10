package com.modusgo.twg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.modusgo.twg.R;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.MaintenanceContract.MaintenanceEntry;
import com.modusgo.twg.db.ServicePerformedContract.ServicePerformedEntry;
import com.modusgo.twg.utils.ServicePerformed;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.method.DateTimeKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
	private ImageButton dateBtn = null;
	private Button doneBtn = null;
	private Button cancelBtn = null;
	private Spinner locationSpinner = null;
	private TextView dateText = null;
	private EditText milageText = null;
	private String dateSelectedString = null;
	private AlertDialog alertDialog = null;
	private ArrayAdapter<String> typeAdapter = null;
	private Bundle bundle = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		bundle = getArguments();
		
		LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.dialog_complete_service, container, false);
		getDialog().setTitle(R.string.EnterServiceCompletion);

		main = ((ServiceLogActivity) getActivity());
		main.setActionBarTitle(getResources().getString(R.string.ServiceLog));

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		dbHelper = DbHelper.getInstance(getActivity());
		db = dbHelper.openDatabase();
		
		//-----------------Other dialog------------------------//
		LayoutInflater li = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dlgView = li.inflate(R.layout.dialog_other_info, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(main);
		builder.setView(dlgView);
		builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.setPositiveButton(R.string.Done, new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Dialog dlg = (Dialog)dialog;
				EditText et = (EditText) dlg.findViewById(R.id.editOtherText);
				String otherText = et.getText().toString();
				if(otherText.length() == 0)
				{
					Toast.makeText(main, getResources().getString(R.string.MissingFieldsErrorMsg), Toast.LENGTH_LONG).show();
				}
				else
				{
					TextView titleView = (TextView) dlg.findViewById(getResources().getIdentifier("alertTitle", "id", "android"));
					String title = titleView.getText().toString();
					if(title.equals(getResources().getString(R.string.EnterOtherTypeInfo)))
					{
						typeList.add(otherText);
						Editor editor = prefs.edit();
						//list is immutable, so create a new one
						Set<String> list = prefs.getStringSet(Constants.PREF_OTHER_TYPES, null);
						if(list == null || !list.contains(otherText))
						{
							TreeSet<String> newList = new TreeSet<String>();
							newList.add(otherText);
							editor.putStringSet(Constants.PREF_OTHER_TYPES, newList);
							editor.commit();
						}
						//not needed
//						typeAdapter.notifyDataSetChanged();
//						typeAdapter.notifyDataSetInvalidated();
						typeSpinner.setSelection((typeList.size()-1));
					}
					else
					{
						locationList.add(otherText);
						Editor editor = prefs.edit();
						//list is immutable, so create a new one
						Set<String> list = prefs.getStringSet(Constants.PREF_OTHER_LOCATIONS, null);
						if(list == null || !list.contains(otherText))
						{
							TreeSet<String> newList = new TreeSet<String>();
							newList.add(otherText);
							editor.putStringSet(Constants.PREF_OTHER_LOCATIONS, newList);
							editor.commit();
						}
						//not needed
//						typeAdapter.notifyDataSetChanged();
//						typeAdapter.notifyDataSetInvalidated();
						locationSpinner.setSelection((locationList.size()-1));
					}
						
				}
			}
		});
		
		alertDialog = builder.create();
		//------------end Other dialog---------------//
		
		Long vehicleId = main.vehicleId;


		typeSpinner = (Spinner) rootView.findViewById(R.id.complete_service_type_spinner);
		dateBtn = (ImageButton) rootView.findViewById(R.id.complete_service_date_btn);
		locationSpinner = (Spinner) rootView.findViewById(R.id.complete_service_location_spinner);
		dateText = (TextView) rootView.findViewById(R.id.complete_service_date);
		milageText = (EditText)rootView.findViewById(R.id.complete_service_milage);
		cancelBtn = (Button)rootView.findViewById(R.id.complete_service_cancel_btn);
		doneBtn = (Button)rootView.findViewById(R.id.complete_service_done_btn);
		
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT);
		String today = sdf.format(now.getTime());
		dateText.setText(today);
		dateSelectedString = today;

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
		
		//Add in "CarMax"
		typeList.add(getResources().getString(R.string.CarMax));
		
		//Add in any user defined types
		Set<String> list = (Set<String>) prefs.getStringSet(Constants.PREF_OTHER_TYPES, null);
		if(list != null && list.size() > 0)
		{
			typeList.addAll(list);
		}

		//Add in "Other"
		typeList.add(getResources().getString(R.string.Other));

		final DatePickerDialog picker = new DatePickerDialog(main, this, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		
		//-----------------type spinner--------------//
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if(position > 0)
				{
					String choice = (String) parent.getItemAtPosition(position);
					if(choice.equals(getResources().getString(R.string.Other)))
					{
						alertDialog.setTitle(R.string.EnterOtherTypeInfo);
						alertDialog.show();
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				// TODO Auto-generated method stub
				
			}
		});		
		
		//-------------location spinner---------------//
		locationSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if(position > 0)
				{
					String choice = (String) parent.getItemAtPosition(position);
					if(choice.equals(getResources().getString(R.string.Other)))
					{
						alertDialog.setTitle(R.string.EnterOtherLocationInfo);
						alertDialog.show();
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				// TODO Auto-generated method stub
				
			}
		});		
		
		//----------------date selected-------------//
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
				Long milage = -1L;
				if(!milageText.getEditableText().toString().equals(""))
				{
					milage = Long.parseLong(milageText.getEditableText().toString());
				}
				if(type.length() == 0 || location.length() == 0 || 
						dateSelectedString == null || dateSelectedString.length() == 0 || 
						milage < 0 )
				{
					Toast rye = Toast.makeText(main, main.getResources().getString(R.string.MissingFieldsErrorMsg), Toast.LENGTH_LONG);
					rye.show();
				}
				else
				{
					ServicePerformed sp = null;
					if(bundle != null)
					{
						ServicePerformed service = (ServicePerformed) bundle.getSerializable("service");
						dbHelper.deleteServicePerformedEvent(service);
					}
					
					sp = new ServicePerformed(type, dateSelectedString, location, milage);
					dbHelper.saveServicePerformedEvent(sp);
					
					main.updateInfo();
					dismiss();
				}
			}
		});
		
		typeAdapter = new ArrayAdapter<String>(main, android.R.layout.simple_spinner_item, (List)typeList);
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(typeAdapter);
			
		locationList = new ArrayList<String>();
		locationList.add("[Selling Dealer]");
		locationList.add("Self-Performed"); 
		//Add in any user defined locations
		list = (Set<String>) prefs.getStringSet(Constants.PREF_OTHER_LOCATIONS, null);
		if(list != null && list.size() > 0)
		{
			locationList.addAll(list);
		}
		locationList.add("Other");
		
		ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(main,
				android.R.layout.simple_spinner_item, (List)locationList);
		locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locationSpinner.setAdapter(locationAdapter);
			
		//-----------------Edit mode-----------------//
		//  get the ServicePerformed from the bundle //
		//  and initialize all the dialog values     //
		//-------------------------------------------//
		if(bundle != null)
		{
			int position = -1;
			ServicePerformed service = (ServicePerformed) bundle.getSerializable("service");
			dateSelectedString = service.date_performed;
			dateText.setText(dateSelectedString);
			if(typeList.contains(service.description))
			{
				typeSpinner.setSelection(typeList.indexOf(service.description));
			}
			if(locationList.contains(service.location_performed))
			{
				locationSpinner.setSelection(locationList.indexOf(service.location_performed));
			}
			milageText.setText(""+service.milage_when_performed);
		}

		

		return rootView;
	}


	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
	{
		dateSelectedString = ""+(monthOfYear+1)+"/"+dayOfMonth+"/"+year; //zero based month
		dateText.setText(dateSelectedString);
	}
	
}
