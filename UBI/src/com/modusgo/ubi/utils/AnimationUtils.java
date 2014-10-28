package com.modusgo.ubi.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class AnimationUtils {
	
	private static class MyAnimationListener implements AnimationListener {
	    
		View view;
	    boolean hideView;
	    
	    public MyAnimationListener(View v, boolean hideView) {
			view = v;
			this.hideView = hideView;
		}
	    
	    public void onAnimationEnd(Animation animation) {
	    	if(hideView)
	    		view.setVisibility(View.GONE);
	    	else
	    		view.setVisibility(View.VISIBLE);
	    }
	    
	    public void onAnimationRepeat(Animation animation) {
	    }
	    public void onAnimationStart(Animation animation) {
	    	view.setVisibility(View.VISIBLE);
	    }
	}
	
	public static Animation getFadeInAnmation(Context ctx, View v){
		Animation fadeIn = android.view.animation.AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in);
		fadeIn.setAnimationListener(new MyAnimationListener(v, false));
		return fadeIn;
	}
	
	public static Animation getFadeOutAnmation(Context ctx, View v){
		Animation fadeOut = android.view.animation.AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out);
		fadeOut.setAnimationListener(new MyAnimationListener(v, true));
		return fadeOut;
	}

}
