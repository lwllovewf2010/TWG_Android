package com.modusgo.ubi;

import java.util.ArrayList;

import android.graphics.Typeface;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.modusgo.ubi.customviews.CircularProgressBar;

public class ScoreActivity extends ActionBarActivity /*implements OnClickListener*/ {

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
	    setContentView(R.layout.activity_score);
	    
	    getSupportActionBar().setTitle("Score");
	    //getSupportActionBar().set
	    //getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.appBg));

		Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		
		CircularProgressBar c1 = (CircularProgressBar) findViewById(R.id.circularprogressbar1);
		c1.setProgress(42);
		c1.setTitleTypeface(roboto);
		c1.setSubTitleTypeface(roboto);
		c1.setAlpha(0.5f);
	    
	    CircularProgressBar c2 = (CircularProgressBar) findViewById(R.id.circularprogressbar2);
	    c2.setProgress(18);
	    c2.setTitleTypeface(roboto);
	    c2.setSubTitleTypeface(roboto);
	    
		
		CircularProgressBar c3 = (CircularProgressBar) findViewById(R.id.circularprogressbar3);
	    c3.setProgress(71);
	    c3.setTitleTypeface(roboto);
	    c3.setSubTitleTypeface(roboto);
	    c3.setAlpha(0.5f);
		
		
	    
	    arrowPrev = (ImageView)findViewById(R.id.arrowPrev);
	    arrowNext = (ImageView)findViewById(R.id.arrowNext);
	    
	    String[] names = new String[]{"Kate","Mary","John","Philip","Marky"};
	    
	    charts = new ArrayList<TitledFragment>();
	    charts.add(new PieChartFragment("Yippie", new float[]{107f,712f,215f,510f,510f},names));//4th equals to zero 'cause it is the average value and counts in Chart constructor
	    charts.add(new PieChartFragment("Ki", new float[]{15f,19f,16f,15f},names));
	    charts.add(new PieChartFragment("Yay", new float[]{33f,26f,18f},names));
	    charts.add(new PieChartFragment("Mr. Willis", new float[]{59f,65f},names));
	    
	    mChartsPagerAdapter = new ChartsPagerAdapter(getSupportFragmentManager(),charts);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mChartsPagerAdapter);
        
        showHideArrows(mViewPager.getCurrentItem());
        
        PagerTabStrip pagerTabStrip = (PagerTabStrip)findViewById(R.id.pager_title_strip);
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
	
}
