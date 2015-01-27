package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.HashMap;

import com.modusgo.adapters.TWGInfoArrayAdapter;
import com.modusgo.ubi.AlertsFragment.GetDiagnosticsTask;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.MaintenanceContract.MaintenanceEntry;
import com.modusgo.ubi.utils.Maintenance;
import com.modusgo.ubi.utils.TWGListItem;
import com.modusgo.ubi.utils.Utils;
import com.modusgo.ubi.utils.TWGListItem.twg_list_item_type;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ServiceFragment extends Fragment
{
	private Vehicle vehicle;
	private SharedPreferences prefs;
	private SwipeRefreshLayout lRefresh;
	private DbHelper dbHelper = null;
	private SQLiteDatabase db = null;
	private Cursor c = null;
	private MainActivity main = null;
	View rootView = null;
	ListView infoList = null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_diagnostics, container, false);
		
		main = (MainActivity) getActivity();
		rootView = (LinearLayout) inflater.inflate(R.layout.alerts_fragment, container, false);
		main.setActionBarTitle("Alerts");
		prefs = PreferenceManager.getDefaultSharedPreferences(main);
		vehicle = ((DriverActivity) getActivity()).vehicle;

		main.setActionBarTitle(main.getResources().getString(R.string.Service));
		
	    prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		vehicle = ((DriverActivity)getActivity()).vehicle;
		
		dbHelper = DbHelper.getInstance(getActivity());
		db = dbHelper.openDatabase();
		
		infoList = (ListView) rootView.findViewById(R.id.alerts_info_list);
//		main.showBusyDialog(R.string.GatheringDiagnosticInformation);

//		new GetMaintenanceTask(getActivity()).execute("vehicles/" + vehicle.id
//				+ "/diagnostics.json");

		updateInfo();
		
		return rootView;

	}

	protected void updateInfo()
	{

	//--------------------------------------------- Maintenances ------------------------------------
	c = db.query(MaintenanceEntry.TABLE_NAME, 
			new String[]{
			MaintenanceEntry._ID,
			MaintenanceEntry.COLUMN_NAME_CREATED_AT,
			MaintenanceEntry.COLUMN_NAME_DESCRIPTION,
			MaintenanceEntry.COLUMN_NAME_IMPORTANCE,
			MaintenanceEntry.COLUMN_NAME_MILEAGE,
			MaintenanceEntry.COLUMN_NAME_PRICE
			}, 
			MaintenanceEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, MaintenanceEntry.COLUMN_NAME_MILEAGE + " DESC");
	
	final ArrayList<TWGListItem> info_list = new ArrayList<TWGListItem>();

	if(c.moveToFirst())
	{
		while(!c.isAfterLast()){
			final Maintenance maintenance = new Maintenance(c.getLong(c.getColumnIndex(MaintenanceEntry._ID)),
					c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_CREATED_AT)), 
					c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_DESCRIPTION)), 
					c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_IMPORTANCE)), 
					c.getString(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_MILEAGE)), 
					c.getFloat(c.getColumnIndex(MaintenanceEntry.COLUMN_NAME_PRICE)));
			info_list.add(new TWGListItem(twg_list_item_type.li_service_log_item,	maintenance));
			c.moveToNext();
		}
		final TWGInfoArrayAdapter info_adapter = new TWGInfoArrayAdapter(
				getActivity(), R.layout.twg_info_list_item, info_list);
		infoList.setAdapter(info_adapter);
	}
	c.close();
	
	}
}
