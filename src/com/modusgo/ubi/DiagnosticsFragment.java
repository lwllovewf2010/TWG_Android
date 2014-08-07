package com.modusgo.ubi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DiagnosticsFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_diagnostics, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("DIAGNOSTICS");
		
		Driver driver = DbHelper.getDrivers().get(getArguments().getInt("id", 0));
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		((ImageView)rootView.findViewById(R.id.imagePhoto)).setImageResource(driver.imageId);

		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);
		
		return rootView;
	}
}
