package com.modusgo.ubi;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class DriversPagerAdapter extends FragmentStatePagerAdapter {
	
	ArrayList<DriversFragment> driversPages = new ArrayList<DriversFragment>();
	
	public DriversPagerAdapter(FragmentManager fm, ArrayList<Driver> drivers) {
        super(fm);
        ArrayList <Driver> driversGroup = new ArrayList<Driver>();
        float weight = 0;
        for (int i = 0; i<drivers.size(); i++) {
        	if(drivers.get(i).score.equals("A")){
				weight+=0.3f;
			}
			else{
				weight+=0.2;
			}
        	System.out.println("__________"+weight);
			driversGroup.add(drivers.get(i));
        	

        	if(i==drivers.size()-1){
        		driversPages.add(new DriversFragment(driversGroup));
				weight = 0;
        	}
			
        	if(weight>=0.6f){
        		driversPages.add(new DriversFragment(driversGroup));
				driversGroup = new ArrayList<Driver>();
				weight = 0;
			}
			
			
        	// 2 больших = 0.6
        	// 3 маленьких 0.6
        	// 2 маленьких 1 большой = 0.7
        	
        	//small = 0.2
        	//big = 0.3
		}
        
        
    }

    @Override
    public Fragment getItem(int i) {
        
        return driversPages.get(i);
    }

    @Override
    public int getCount() {
        return driversPages.size();
    }
}
