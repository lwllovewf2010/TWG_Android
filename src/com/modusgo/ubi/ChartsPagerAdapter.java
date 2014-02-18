package com.modusgo.ubi;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ChartsPagerAdapter extends FragmentStatePagerAdapter {
    
	ArrayList<Chart> charts = new ArrayList<Chart>();
	
	public ChartsPagerAdapter(FragmentManager fm, ArrayList<Chart> charts) {
        super(fm);
        this.charts = charts;
    }

    @Override
    public Fragment getItem(int i) {
        
        return charts.get(i).fragment;
    }

    @Override
    public int getCount() {
        return charts.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return charts.get(position).name;
    }
}
