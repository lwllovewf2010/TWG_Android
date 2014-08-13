package com.modusgo.ubi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TripFragment extends Fragment {
	
	public static final String SAVED_TRIP_ID = "tripId";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.fragment_trip, container, false);
		
		/*try{
			((MainActivity)getActivity()).setNavigationDrawerItemSelected(MenuItems.SETTINGS.toInt());
		}
		catch(ClassCastException e){
			e.printStackTrace();
		}*/
		
		return rootView;
	}
}