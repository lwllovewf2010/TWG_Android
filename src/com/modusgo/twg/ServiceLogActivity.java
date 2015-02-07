package com.modusgo.twg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.modusgo.adapters.TWGInfoArrayAdapter;
import com.modusgo.twg.R;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.DTCContract.DTCEntry;
import com.modusgo.twg.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.twg.db.ServicePerformedContract.ServicePerformedEntry;
import com.modusgo.twg.utils.ServicePerformed;
import com.modusgo.twg.utils.TWGListItem;
import com.modusgo.twg.utils.TWGListItem.twg_list_item_type;

public class ServiceLogActivity extends MainActivity
{
	public static final String VEHICLE_ID = "vehicleId";

	private static final String TAG = "ServiceLogActivity";

	private SwipeRefreshLayout lRefresh;
	private DbHelper dbHelper = null;
	private SQLiteDatabase db = null;
	private Cursor c = null;
	View rootView = null;
	ListView infoList = null;
	public Long vehicleId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.activity_service_log_view);
		super.onCreate(savedInstanceState);

		vehicleId = getIntent().getLongExtra(VEHICLE_ID, 0);

		showBusyDialog(R.string.GatheringDiagnosticInformation);

		dbHelper = DbHelper.getInstance(this);
		db = dbHelper.openDatabase();

		updateInfo();
	}

	protected void updateInfo()
	{
		hideBusyDialog();

		// -----------------------------Service Performed----------------
		String orderBy = ServicePerformedEntry.COLUMN_NAME_DESCRIPTION + " DESC, "
				+ ServicePerformedEntry.COLUMN_NAME_DATE + " DESC";
		String groupBy = ServicePerformedEntry.COLUMN_NAME_DESCRIPTION;
		Cursor c = db.query(ServicePerformedEntry.TABLE_NAME, new String[]
		{ ServicePerformedEntry.COLUMN_NAME_DESCRIPTION, ServicePerformedEntry.COLUMN_NAME_DATE,
				ServicePerformedEntry.COLUMN_NAME_LOCATION, ServicePerformedEntry.COLUMN_NAME_MILAGE, }, null, null,
				null, null, orderBy);

		final ArrayList<TWGListItem> info_list = new ArrayList<TWGListItem>();

		// Calendar date = Calendar.getInstance();
		// SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		// date.roll(Calendar.YEAR, -1);
		// for(int iType = 0; iType < service.length; iType++)
		// {
		// info_list.add(new TWGListItem(twg_list_item_type.li_service_log_hdr,
		// service[iType][1]));
		// date = Calendar.getInstance();
		// date.roll(Calendar.YEAR, -1);
		// for(int iEntry = 0; iEntry < 4; iEntry++)
		// {
		// date.roll(Calendar.MONTH, 1);
		// String dateText = sdf.format(date.getTime());
		if(c.moveToFirst())
		{
			info_list.add(new TWGListItem(twg_list_item_type.li_alert_hdr, null));

			while(!c.isAfterLast())
			{
				String descr = c.getString(c.getColumnIndex(ServicePerformedEntry.COLUMN_NAME_DESCRIPTION));
				info_list.add(new TWGListItem(twg_list_item_type.li_alert_subhdr, descr));

				ServicePerformed serviceEntry = new ServicePerformed(descr, 
						c.getString(c.getColumnIndex(ServicePerformedEntry.COLUMN_NAME_DATE)), 
						c.getString(c.getColumnIndex(ServicePerformedEntry.COLUMN_NAME_LOCATION)), 
						c.getLong(c.getColumnIndex(ServicePerformedEntry.COLUMN_NAME_MILAGE)));
				
				info_list.add(new TWGListItem(twg_list_item_type.li_service_log_item, serviceEntry));
				
				c.moveToNext();
			}
		}

		setActionBarTitle(getResources().getString(R.string.ServiceLog));
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		infoList = (ListView) findViewById(R.id.service_log_info_list);

		final TWGInfoArrayAdapter info_adapter = new TWGInfoArrayAdapter(getApplicationContext(),
				R.layout.twg_info_list_item, info_list);
		infoList.setAdapter(info_adapter);

		Button addButton = (Button) findViewById(R.id.service_log_add_button);
		addButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				CompleteServiceDialog completeServiceFragment = new CompleteServiceDialog();
				FragmentManager fragmentManager = getSupportFragmentManager();
				completeServiceFragment.show(fragmentManager, "CompleteService");
			}
		});

	}
}
