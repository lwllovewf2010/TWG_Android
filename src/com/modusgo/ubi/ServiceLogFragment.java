package com.modusgo.ubi;

import java.util.Calendar;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.utils.ServiceEntry;

public class ServiceLogFragment extends Fragment
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		/***************************DEBUGGING ONLY**********************/
		String service[][] =
			{
				{"54,235", "Engine Air Filter Replaced", "(Selling Dealer)"},
				{"51,645", "Timing Belt Replaced", "Other: Bob's Auto"},
				{"51,645", "Tires Replaced", "Other: Bob's Auto"},
				{"45,089", "Oil Changed", "Self-Performed"},
				{"41,521", "Tires Rotated", "Selling Dealer"}
			};
		ServiceEntry serviceEntry[] = new ServiceEntry[20];
		Calendar date = Calendar.getInstance();
		date.roll(Calendar.YEAR, -1);
		for(int iBlock = 0; iBlock < 4; iBlock++)
		{
			date.roll(Calendar.MONTH, 1);
			for(int iEntry = 0; iEntry < 5; iEntry++)
			{
				long interval = iEntry*1000;
				serviceEntry[iBlock*4+iEntry] = new ServiceEntry(service[iEntry][1], interval, date, service[iEntry][2], Long.parseLong(service[iEntry][0]));
			}
		}
		
		LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment_diagnostics, container, false);

		main = (MainActivity) getActivity();
		rootView = (LinearLayout) inflater.inflate(R.layout.alerts_fragment, container, false);
		main.setActionBarTitle("Alerts");
		prefs = PreferenceManager.getDefaultSharedPreferences(main);
		vehicle = ((DriverActivity) getActivity()).vehicle;

		main.setActionBarTitle(main.getResources().getString(R.string.Diagnostics));

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		vehicle = ((DriverActivity) getActivity()).vehicle;

		dbHelper = DbHelper.getInstance(getActivity());
		db = dbHelper.openDatabase();

		infoList = (ListView) rootView.findViewById(R.id.alerts_info_list);

		
		return rootView;
	}
}
