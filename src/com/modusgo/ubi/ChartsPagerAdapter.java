package com.modusgo.ubi;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ChartsPagerAdapter extends FragmentStatePagerAdapter {
    
	ArrayList<ChartFragment> charts = new ArrayList<ChartFragment>();
	
	public ChartsPagerAdapter(FragmentManager fm, ArrayList<ChartFragment> charts) {
        super(fm);
        this.charts = charts;
    }

    @Override
    public Fragment getItem(int i) {
        
        return charts.get(i);
    }

    @Override
    public int getCount() {
        return charts.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return charts.get(position).title;
    }
}
