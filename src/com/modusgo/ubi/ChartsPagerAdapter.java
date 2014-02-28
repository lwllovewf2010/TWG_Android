package com.modusgo.ubi;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ChartsPagerAdapter extends FragmentStatePagerAdapter {
    
	ArrayList<TitledFragment> charts = new ArrayList<TitledFragment>();
	
	public ChartsPagerAdapter(FragmentManager fm, ArrayList<TitledFragment> charts) {
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
