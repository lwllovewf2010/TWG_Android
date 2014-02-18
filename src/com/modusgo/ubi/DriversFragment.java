package com.modusgo.ubi;

import com.modusgo.modusadmin.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DriversFragment extends Fragment {

	final String LOG_TAG = "myLogs";

	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
	    Log.d(LOG_TAG, "Fragment1 onCreateView");
	    return inflater.inflate(R.layout.drivers_fragment, null);
	  }

}
