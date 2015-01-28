package com.modusgo.ubi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
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

import com.modusgo.adapters.TWGInfoArrayAdapter;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.utils.ServiceEntry;
import com.modusgo.ubi.utils.TWGListItem;
import com.modusgo.ubi.utils.TWGListItem.twg_list_item_type;

public class ServiceLogActivity extends MainActivity
{
	public static final String EXTRA_SERVICE_INFO = "serviceInfo";
	

	private Vehicle vehicle;
	private SharedPreferences prefs;
	private SwipeRefreshLayout lRefresh;
	private DbHelper dbHelper = null;
	private SQLiteDatabase db = null;
	private Cursor c = null;
	View rootView = null;
	ListView infoList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_service_log_view);
		super.onCreate(savedInstanceState);

		/***************************DEBUGGING ONLY**********************/
		String service[][] =
			{
				{"54235", "Engine Air Filter Replaced", "(Selling Dealer)"},
				{"51645", "Timing Belt Replaced", "Other: Bob's Auto"},
				{"51645", "Tires Replaced", "Other: Bob's Auto"},
				{"45089", "Oil Changed", "Self-Performed"},
				{"41521", "Tires Rotated", "Selling Dealer"}
			};
		final ArrayList<TWGListItem> info_list = new ArrayList<TWGListItem>();

		Calendar date = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		date.roll(Calendar.YEAR, -1);
		for(int iBlock = 0; iBlock < 4; iBlock++)
		{
			date.roll(Calendar.MONTH, 1);
			String dateText = sdf.format(date.getTime());
			info_list.add(new TWGListItem(twg_list_item_type.li_service_log_hdr, dateText));
			
			for(int iEntry = 0; iEntry < 5; iEntry++)
			{
				long interval = iEntry*1000;
				ServiceEntry serviceEntry = new ServiceEntry(service[iEntry][1], interval, date, service[iEntry][2], Long.parseLong(service[iEntry][0]));
				info_list.add(new TWGListItem(twg_list_item_type.li_service_log_item, serviceEntry));
			}
		}
		
		setActionBarTitle(getResources().getString(R.string.ServiceLog));
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		dbHelper = DbHelper.getInstance(this);
		db = dbHelper.openDatabase();

		infoList = (ListView)findViewById(R.id.service_log_info_list);
		
		final TWGInfoArrayAdapter info_adapter = new TWGInfoArrayAdapter(getApplicationContext(), R.layout.twg_info_list_item,
				info_list);
		infoList.setAdapter(info_adapter);

	}
}
