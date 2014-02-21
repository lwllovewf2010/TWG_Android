package com.modusgo.ubi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DriverInfoFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.driver_info_fragment, null);
		
		return rootView;
	}

}
