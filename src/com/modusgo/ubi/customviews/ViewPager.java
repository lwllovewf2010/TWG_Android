package com.modusgo.ubi.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPager extends android.support.v4.view.ViewPager{
	private boolean swipeEnabled;

	public ViewPager(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    this.swipeEnabled = false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (this.swipeEnabled) {
	        return super.onTouchEvent(event);
	    }

	    return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
	    if (this.swipeEnabled) {
	        return super.onInterceptTouchEvent(event);
	    }

	    return false;
	}

	public void setSwipeEnabled(boolean enabled) {
	    this.swipeEnabled = enabled;
	}
}
