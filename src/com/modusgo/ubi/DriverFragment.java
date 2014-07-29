package com.modusgo.ubi;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DriverFragment extends Fragment {
	
	DriverInfoAdapter mAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){
	    //setContentView(R.layout.activity_driver_info);
	    View rootView = inflater.inflate(R.layout.fragment_driver_info, container, false);
		
	    //getActivity().getSupportActionBar().setTitle("Sally");
	    
	    /*ArrayList<DriverInfoFragment> pages = new ArrayList<DriverInfoFragment>();
	    pages.add(new DriverInfoFragment());
	    pages.add(new DriverInfoFragment());
	    
	    mAdapter = new DriverInfoAdapter(getChildFragmentManager(),pages);
	    
	    ViewPager mPager = (ViewPager)rootView.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        
        //Bind the title indicator to the adapter
        CirclePageIndicator indicator = (CirclePageIndicator)rootView.findViewById(R.id.titles);
        indicator.setViewPager(mPager);
        indicator.setSnap(true);
        
        final float density = getResources().getDisplayMetrics().density;
        indicator.setRadius(4 * density);
        indicator.setPageColor(0x33FFFFFF);//unactive circle color
        indicator.setFillColor(0xFFFFFFFF);
        indicator.setStrokeWidth(0);*/
        
	    ((TextView)rootView.findViewById(R.id.tv_driver_name)).setText(getArguments().getString("name"));
	    
	    ArrayList<String> titles = new ArrayList<String>();
        titles.add("Score");
        titles.add("Trips");
        titles.add("Distance");
        titles.add("Drive Time");
        titles.add("Avg. Speed");
        titles.add("Max. Speed");
        
        ArrayList<String> values = new ArrayList<String>();
        values.add(getArguments().getString("score"));
        values.add("2");
        values.add("21");
        values.add("27:14");
        values.add("40");
        values.add("70");
        
        LinearLayout rowLayout = new LinearLayout(getActivity());
    	LinearLayout llBlocks = (LinearLayout)rootView.findViewById(R.id.llBlocks);
    	
        for (int i = 0; i < 6; i++) {
        	
        	if(i%2==0 && i!=0){
        		llBlocks.addView(rowLayout);
        		rowLayout = new LinearLayout(getActivity());
        		System.out.println("---------------------- "+i);
        	}
        	
        	LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    		rowLayout.setLayoutParams(p);
    		
    		LinearLayout block = (LinearLayout) inflater.inflate(R.layout.driver_info_item, rowLayout, false);
    		rowLayout.addView(block);
    		
    		if(i>1){
	    		AlphaAnimation alpha = new AlphaAnimation(0.7F, 0.7F);
	    		alpha.setDuration(0); // Make animation instant
	    		alpha.setFillAfter(true); // Tell it to persist after the animation ends
	    		// And then on your layout
	    		block.startAnimation(alpha);
    		}
    		
        	TextView title = (TextView)block.findViewById(R.id.tvTitle);
        	title.setText(titles.get(i));
        	TextView value = (TextView)block.findViewById(R.id.tvValue);
        	value.setText(values.get(i));
    		System.out.println("---------------------- "+i + " __"+titles.get(i)+"__"+values.get(i));
    		
    		if(titles.get(i).equals("Max. Speed"))
    			title.setBackgroundResource(R.color.driver_stats_block_header_red);
    		
		}
		llBlocks.addView(rowLayout);
        
        
        
		
		//rowLayout.addView(block);
        
		return rootView;
	}
	
	private Bitmap b = null;
	
	@Override
	public void onPause() {
		b = loadBitmapFromView(getView());
		super.onPause();
	}

	public static Bitmap loadBitmapFromView(View v) {
		Bitmap b = Bitmap.createBitmap(v.getWidth(),
				v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		v.layout(0, 0, v.getWidth(),
				v.getHeight());
		v.draw(c);
		return b;
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public void onDestroyView() {
		BitmapDrawable bd = new BitmapDrawable(getResources(),b);
		
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			getView().findViewById(R.id.root_layout).setBackgroundDrawable(bd);
		} else {
			getView().findViewById(R.id.root_layout).setBackground(bd);
		}
		
		b = null;
		super.onDestroyView();
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