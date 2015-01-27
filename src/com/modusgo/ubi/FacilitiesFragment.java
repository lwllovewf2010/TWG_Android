package com.modusgo.ubi;

import com.google.android.gms.maps.MapFragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FacilitiesFragment extends Fragment {

	private Vehicle vehicle;
	private SharedPreferences prefs;
	private View rootView = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		MainActivity main = (MainActivity)getActivity();
		
//		View rootView = inflater.inflate(R.layout.facilities_view, container, false);

		main.setActionBarTitle("Facilities");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(main);

		vehicle = ((DriverActivity)getActivity()).vehicle;
		
		/**************DEBUGGING ONLY ***************/
		String loc = "CarMax\r\n2000 Frontage Road \r\nNorthbrook, IL \r\n60062";
		/**************DEBUGGING ONLY ***************/


//		TextView tv = (TextView) findViewById(R.id.find_mechanic_details);
//		tv.setText(loc);
//		
//		viewFlipper.setDisplayedChild(findMechanicViewIndex);
//		
//		mapFragment = (MapFragment) getFragmentManager()
//                .findFragmentById(R.id.find_mechanic_map);
//        mapFragment.getMapAsync(this);

		
		
		
		
		
		return rootView;
	}
}
