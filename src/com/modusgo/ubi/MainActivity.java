package com.modusgo.ubi;

import java.util.ArrayList;

import com.viewpagerindicator.CirclePageIndicator;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity /*implements OnClickListener*/ {

	Fragment frag1;
	ChartFragment frag2;
	FragmentTransaction fTrans;
	CheckBox chbStack;
	
	ArrayList<TitledFragment> charts;
	
	ChartsPagerAdapter mChartsPagerAdapter;
    ViewPager mViewPager;	
	
    ImageView arrowPrev;
    ImageView arrowNext;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    getSupportActionBar().setTitle("Dashboard");
	    //getSupportActionBar().set
	    //getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.appBg));
	    
	    ArrayList<Driver> drivers = new ArrayList<Driver>();
	    drivers.add(new Driver("Mary","B"));
	    drivers.add(new Driver("Kate","A"));
	    drivers.add(new Driver("John","C"));
	    drivers.add(new Driver("Philip","B"));
	    drivers.add(new Driver("Marky","B"));
	    
	    DriversPagerAdapter driversPagerAdapter = new DriversPagerAdapter(getSupportFragmentManager(),drivers);
	    
	    ViewPager pagerDrivers = (ViewPager)findViewById(R.id.pager_drivers);
	    pagerDrivers.setAdapter(driversPagerAdapter);
	    
	    //Bind the title indicator to the adapter
        CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pagerDrivers);
        indicator.setSnap(true);
        
        final float density = getResources().getDisplayMetrics().density;
        indicator.setRadius(4 * density);
        indicator.setPageColor(0x33FFFFFF);//unactive circle color
        indicator.setFillColor(0xFFFFFFFF);
        indicator.setStrokeWidth(0);
        
	    		
	    String[] names = new String[]{"Kate","Mary","John","Philip","Marky"};
	    
	    charts = new ArrayList<TitledFragment>();
	    charts.add(new ChartFragment("Yippie", new float[]{107f,712f,215f,510f,510f},names));//4th equals to zero 'cause it is the average value and counts in Chart constructor
	    charts.add(new ChartFragment("Ki", new float[]{15f,19f,16f,15f},names));
	    charts.add(new ChartFragment("Yay", new float[]{33f,26f,18f},names));
	    charts.add(new ChartFragment("Mr. Willis", new float[]{59f,65f},names));
	    
	    arrowPrev = (ImageView)findViewById(R.id.arrowPrev);
	    arrowNext = (ImageView)findViewById(R.id.arrowNext);
	    
	    /*fTrans = getSupportFragmentManager().beginTransaction();
	    fTrans.replace(R.id.charts, charts.get(0).fragment);
		fTrans.commit();*/
		
		mChartsPagerAdapter = new ChartsPagerAdapter(getSupportFragmentManager(),charts);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mChartsPagerAdapter);
        
        showHideArrows(mViewPager.getCurrentItem());
        
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
        
        
        
		/*((Button)findViewById(R.id.button1)).setOnClickListener(this);
		((Button)findViewById(R.id.button2)).setOnClickListener(this);
		((Button)findViewById(R.id.button3)).setOnClickListener(this);*/
	    
	    /*final float scale = getResources().getDisplayMetrics().density;
	    
	    for (final Chart chart : charts) {
	    	final Button chartBtn = new Button(getApplicationContext());
	    	chartBtn.setText(chart.name);
	    	chartBtn.setWidth((int) (160 * scale + 0.5f));
	    	
	    	chartBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int[] loc = new int[2];
					chartBtn.getLocationOnScreen(loc);
					
					fTrans = getSupportFragmentManager().beginTransaction();
					
					HorizontalScrollView scrollView = (HorizontalScrollView)findViewById(R.id.horizontalScrollView1);
					int scrollToPosX = (int)(chartBtn.getLeft()-chartBtn.getWidth()/2f);
					if(scrollToPosX<scrollView.getScrollX())
						fTrans.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
					else
						fTrans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
						
					scrollView.smoothScrollTo(scrollToPosX,0);
					
					fTrans.replace(R.id.charts, chart.fragment);
					if (chbStack.isChecked()) fTrans.addToBackStack(null);
					fTrans.commit();
				}
			});
	    	
			((LinearLayout)findViewById(R.id.chartButtons)).addView(chartBtn);
		}
	    
	    chbStack = (CheckBox)findViewById(R.id.chbStack);*/
		
		Typeface robotoThin = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		
		PagerTabStrip pagerTabStrip = (PagerTabStrip)findViewById(R.id.pager_title_strip);
		pagerTabStrip.setTabIndicatorColorResource(R.color.pagerTabStripBg);
		
		Resources r = getResources();
	    int paddingTop = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, r.getDisplayMetrics()));
	       
		for (int i = 0; i < pagerTabStrip.getChildCount(); ++i) {
		    View nextChild = pagerTabStrip.getChildAt(i);
		    if (nextChild instanceof TextView) {
		       TextView textViewToConvert = (TextView) nextChild;
		       textViewToConvert.setTypeface(robotoThin);
		       textViewToConvert.setPadding(0, paddingTop, 0, 0);
		    }
		}
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

	/*public void onClick(View v) {
	    //fTrans = getSupportFragmentManager().beginTransaction();
	    //fTrans.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	    switch (v.getId()) {
	    	case R.id.button1:
	    	case R.id.button2:
	    	case R.id.button3:
	    		startActivity(new Intent(getApplicationContext(), DriverActivity.class));
	    	    break;
		    default:
		    	break;
	    }
	    //if (chbStack.isChecked()) fTrans.addToBackStack(null);
	    	//fTrans.commit();
	}*/

}
