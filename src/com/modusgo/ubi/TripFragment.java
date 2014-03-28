package com.modusgo.ubi;

import com.modusgo.ubi.MainActivity.MenuItems;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TripFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.fragment_trip, container, false);
		
		try{
			((MainActivity)getActivity()).setNavigationDrawerItemSelected(MenuItems.TRIPS.toInt());
		}
		catch(ClassCastException e){
			e.printStackTrace();
		}
		
		return rootView;
	}
}