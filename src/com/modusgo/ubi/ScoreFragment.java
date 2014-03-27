package com.modusgo.ubi;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.modusgo.ubi.customviews.CircularProgressBar;
import com.modusgo.ubi.customviews.ViewPager;

public class ScoreFragment extends Fragment {
	
	ArrayList<TitledFragment> charts;
	
	ChartsPagerAdapter mChartsPagerAdapter;
    ViewPager mViewPager;	
	
    ImageView arrowPrev;
    ImageView arrowNext;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_score, null);
	    
	    //getSupportActionBar().setTitle("Score");
	    //getSupportActionBar().set
	    //getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.appBg));

		Typeface roboto = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
		
		CircularProgressBar c1 = (CircularProgressBar) rootView.findViewById(R.id.circularprogressbar1);
		c1.setProgress(42);
		c1.setTitleTypeface(roboto);
		c1.setSubTitleTypeface(roboto);
		c1.setAlpha(0.5f);
	    
	    CircularProgressBar c2 = (CircularProgressBar) rootView.findViewById(R.id.circularprogressbar2);
	    c2.setProgress(18);
	    c2.setTitleTypeface(roboto);
	    c2.setSubTitleTypeface(roboto);
	    
		
		CircularProgressBar c3 = (CircularProgressBar) rootView.findViewById(R.id.circularprogressbar3);
	    c3.setProgress(71);
	    c3.setTitleTypeface(roboto);
	    c3.setSubTitleTypeface(roboto);
	    c3.setAlpha(0.5f);
		
		
	    
	    arrowPrev = (ImageView)rootView.findViewById(R.id.arrowPrev);
	    arrowNext = (ImageView)rootView.findViewById(R.id.arrowNext);
	    
	    String[] names = new String[]{"Kate","Mary","John","Philip","Marky"};
	    
	    charts = new ArrayList<TitledFragment>();
	    charts.add(new PieChartFragment("Yippie", new float[]{107f,712f,215f,510f,510f},names));//4th equals to zero 'cause it is the average value and counts in Chart constructor
	    charts.add(new PieChartFragment("Ki", new float[]{15f,19f,16f,15f},names));
	    charts.add(new PieChartFragment("Yay", new float[]{33f,26f,18f},names));
	    charts.add(new PieChartFragment("Mr. Willis", new float[]{59f,65f},names));
	    
	    mChartsPagerAdapter = new ChartsPagerAdapter(getChildFragmentManager(),charts);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mChartsPagerAdapter);
        
        showHideArrows(mViewPager.getCurrentItem());
        
        PagerTabStrip pagerTabStrip = (PagerTabStrip)rootView.findViewById(R.id.pager_title_strip);
		pagerTabStrip.setTabIndicatorColorResource(R.color.pagerTabStripBg);
        
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int pageNum) {
				showHideArrows(pageNum);					
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
        
        return rootView;
	}
	
	private void showHideArrows(int pageNum){
		if(pageNum==0)
			arrowPrev.setVisibility(View.INVISIBLE);
		else
			arrowPrev.setVisibility(View.VISIBLE);
		if(pageNum==charts.size()-1)
			arrowNext.setVisibility(View.INVISIBLE);
		else
			arrowNext.setVisibility(View.VISIBLE);
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

	@Override
	public void onDestroyView() {
		BitmapDrawable bd = new BitmapDrawable(getResources(),b);
		getView().findViewById(R.id.root_layout).setBackground(bd);
		b = null;
		super.onDestroyView();
	}
	
}
