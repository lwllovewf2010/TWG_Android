package com.modusgo.twg.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;

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
	
	public static void expand(final View v) {
	    v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    final int targetHeight = v.getMeasuredHeight();

	    v.getLayoutParams().height = 0;
	    v.setVisibility(View.VISIBLE);
	    Animation a = new Animation()
	    {
	        @Override
	        protected void applyTransformation(float interpolatedTime, Transformation t) {
	            v.getLayoutParams().height = interpolatedTime == 1
	                    ? LayoutParams.WRAP_CONTENT
	                    : (int)(targetHeight * interpolatedTime);
	            v.requestLayout();
	        }

	        @Override
	        public boolean willChangeBounds() {
	            return true;
	        }
	    };

	    // 1dp/ms
	    a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
	    v.startAnimation(a);
	}

	public static void collapse(final View v) {
	    final int initialHeight = v.getMeasuredHeight();

	    Animation a = new Animation()
	    {
	        @Override
	        protected void applyTransformation(float interpolatedTime, Transformation t) {
	            if(interpolatedTime == 1){
	                v.setVisibility(View.GONE);
	            }else{
	                v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
	                v.requestLayout();
	            }
	        }

	        @Override
	        public boolean willChangeBounds() {
	            return true;
	        }
	    };

	    // 1dp/ms
	    a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
	    v.startAnimation(a);
	}

}
