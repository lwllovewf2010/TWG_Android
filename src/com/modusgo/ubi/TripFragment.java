package com.modusgo.ubi;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.modusgo.ubi.MainActivity.MenuItems;

public class TripFragment extends Fragment {
	
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