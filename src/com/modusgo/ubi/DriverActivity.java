package com.modusgo.ubi;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.CirclePageIndicator;

public class DriverActivity extends ActionBarActivity {
	
	DriverInfoAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_driver_info);
	    
	    getSupportActionBar().setTitle("Limits");
	    
	    ArrayList<DriverInfoFragment> pages = new ArrayList<DriverInfoFragment>();
	    pages.add(new DriverInfoFragment());
	    pages.add(new DriverInfoFragment());
	    
	    mAdapter = new DriverInfoAdapter(getSupportFragmentManager(),pages);
	    
	    ViewPager mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        
        //Bind the title indicator to the adapter
        CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.titles);
        indicator.setViewPager(mPager);
        indicator.setSnap(true);
        
        final float density = getResources().getDisplayMetrics().density;
        indicator.setRadius(4 * density);
        indicator.setPageColor(0x33FFFFFF);//unactive circle color
        indicator.setFillColor(0xFFFFFFFF);
        indicator.setStrokeWidth(0);

	}
}

class DriverInfoAdapter extends FragmentStatePagerAdapter {
    
	ArrayList<DriverInfoFragment> pages = new ArrayList<DriverInfoFragment>();
	
	public DriverInfoAdapter(FragmentManager fm, ArrayList<DriverInfoFragment> pages) {
        super(fm);
        this.pages = pages;
    }

    @Override
    public Fragment getItem(int i) {
        return pages.get(i);
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    /*@Override
    public CharSequence getPageTitle(int position) {
        return pages.get(position).title;
    }*/
}