package com.modusgo.ubi.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.farmers.ubi.R;

public class ExpandablePanel extends LinearLayout {
	 
    private final int mHandleId;
    private final int mContentId;
    private final int mIndicatorId;
    private final int mTitleId;
    private final Drawable mIndicatorDrawableOpened;
    private final Drawable mIndicatorDrawableClosed;
    private final String mTitleClosed;
    private final String mTitleOpened;
 
    // Contains references to the handle and content views
    private View mHandle;
    private View mContent;
    private ImageView mIndicator;
    private TextView mTitle;
 
    // Does the panel start expanded?
    private boolean mExpanded = false;
    // The height of the content when collapsed
    private int mCollapsedHeight = 0;
    // The full expanded height of the content (calculated)
    private int mContentHeight = 0;
    // How long the expand animation takes
    private int mAnimationDuration = 0;
 
    // Listener that gets fired onExpand and onCollapse
    private OnExpandListener mListener;
 
    public ExpandablePanel(Context context) {
        this(context, null);
    }
 
    /**
     * The constructor simply validates the arguments being passed in and
     * sets the global variables accordingly. Required attributes are 
     * 'handle' and 'content'
     */
    public ExpandablePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mListener = new DefaultOnExpandListener();
 
        TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.ExpandablePanel, 0, 0);
 
        // How high the content should be in "collapsed" state
        mCollapsedHeight = (int) a.getDimension(
                    R.styleable.ExpandablePanel_collapsedHeight, 0.0f);
 
        // How long the animation should take
        mAnimationDuration = a.getInteger(
                    R.styleable.ExpandablePanel_animationDuration, 500);
 
        int handleId = a.getResourceId(
                    R.styleable.ExpandablePanel_handle, 0);
 
//        if (handleId == 0) {
//            throw new IllegalArgumentException(
//                "The handle attribute is required and must refer "
//                    + "to a valid child.");
//        }
 
        int contentId = a.getResourceId(
                        R.styleable.ExpandablePanel_content, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException(
                        "The content attribute is required and must " +
                        "refer to a valid child.");
        }
        
        int indicatorId = a.getResourceId(
                R.styleable.ExpandablePanel_indicatorImageView, 0);
        
        mTitleId = a.getResourceId(
                R.styleable.ExpandablePanel_handleTitleView, 0);
        
        mIndicatorDrawableOpened = a.getDrawable(R.styleable.ExpandablePanel_indicatorDrawableOpened);
        mIndicatorDrawableClosed = a.getDrawable(R.styleable.ExpandablePanel_indicatorDrawableClosed);
        mTitleOpened = a.getString(R.styleable.ExpandablePanel_handleTextOpened);
        mTitleClosed = a.getString(R.styleable.ExpandablePanel_handleTextClosed);
 
        mHandleId = handleId;
        mContentId = contentId;
        mIndicatorId = indicatorId;
 
        a.recycle();
    }
 
    // Some public setters for manipulating the
    // ExpandablePanel programmatically
    public void setOnExpandListener(OnExpandListener listener) {
        mListener = listener; 
    }
 
    public void setCollapsedHeight(int collapsedHeight) {
        mCollapsedHeight = collapsedHeight;
    }
 
    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }
 
    /**
     * This method gets called when the View is physically
     * visible to the user
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
 
        mHandle = findViewById(mHandleId);
//        if (mHandle == null) {
//            throw new IllegalArgumentException(
//                "The handle attribute is must refer to an"
//                    + " existing child.");
//        }
 
        mContent = findViewById(mContentId);
        if (mContent == null) {
            throw new IllegalArgumentException(
                "The content attribute must refer to an"
                    + " existing child.");
        }
        
        mIndicator = (ImageView) findViewById(mIndicatorId);
        
        mTitle = (TextView) findViewById(mTitleId);
 
        // This changes the height of the content such that it
        // starts off collapsed
        android.view.ViewGroup.LayoutParams lp = 
                                    mContent.getLayoutParams();
        lp.height = mCollapsedHeight;
        mContent.setLayoutParams(lp);
 
        // Set the OnClickListener of the handle view
        if(mHandle!=null)
        	mHandle.setOnClickListener(new PanelToggler());
    }
 
    /**
     * This is where the magic happens for measuring the actual
     * (un-expanded) height of the content. If the actual height
     * is less than the collapsedHeight, the handle will be hidden.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec,
                                            int heightMeasureSpec) {
        // First, measure how high content wants to be
        mContent.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
        mContentHeight = mContent.getMeasuredHeight();
        Log.v("cHeight", mContentHeight+"");
        Log.v("cCollapseHeight", mCollapsedHeight+"");
 
        if(mHandle!=null){
	        if (mContentHeight < mCollapsedHeight) {
	            mHandle.setVisibility(View.GONE);
	        } else {
	            mHandle.setVisibility(View.VISIBLE);
	        }
        }
 
        // Then let the usual thing happen
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    private boolean animInProcess = false;
    
    public void expand(){
    	Animation a;
        if (!mExpanded && !animInProcess) {
        	if(mIndicator!=null && mIndicatorDrawableOpened!=null){
        		mIndicator.setImageDrawable(mIndicatorDrawableOpened);
        	}
        	if(mTitle!=null && mTitleOpened!=null)
        		mTitle.setText(mTitleOpened);
           a = new ExpandAnimation(mCollapsedHeight, mContentHeight);
           a.setDuration(mAnimationDuration);
           a.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					animInProcess = true;
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {				
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					animInProcess = false;
					mExpanded = true;
					if(mListener!=null)
						mListener.onExpand(mHandle, mContent);
				}
			});
           mContent.startAnimation(a);
        }
    }
    
    public void collapse(){
    	Animation a;
        if (mExpanded && !animInProcess) {
        	if(mIndicator!=null && mIndicatorDrawableClosed!=null){
        		mIndicator.setImageDrawable(mIndicatorDrawableClosed);
        	}
        	if(mTitle!=null && mTitleClosed!=null)
        		mTitle.setText(mTitleClosed);
           a = new ExpandAnimation(mContentHeight, mCollapsedHeight);
           a.setDuration(mAnimationDuration);
           a.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					animInProcess = true;
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {				
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					animInProcess = false;
					mExpanded = false;
					if(mListener!=null)
						mListener.onCollapse(mHandle, mContent);
				}
			});
           mContent.startAnimation(a);
        }
    }
    
    public boolean isExpanded(){
    	return mExpanded;
    }
 
    /**
     * This is the on click listener for the handle.
     * It basically just creates a new animation instance and fires
     * animation.
     */
    private class PanelToggler implements OnClickListener {
        public void onClick(View v) {
            if (!mExpanded) {
            	expand();
            } else {
            	collapse();
            }
        }
    }
 
    /**
     * This is a private animation class that handles the expand/collapse
     * animations. It uses the animationDuration attribute for the length 
     * of time it takes.
     */
    private class ExpandAnimation extends Animation {
        private final int mStartHeight;
        private final int mDeltaHeight;
 
        public ExpandAnimation(int startHeight, int endHeight) {
            mStartHeight = startHeight;
            mDeltaHeight = endHeight - startHeight;
        }
 
        @Override
        protected void applyTransformation(float interpolatedTime, 
                                                 Transformation t) {
            android.view.ViewGroup.LayoutParams lp = 
                                          mContent.getLayoutParams();
            lp.height = (int) (mStartHeight + mDeltaHeight *
                                                   interpolatedTime);
            mContent.setLayoutParams(lp);
        }
 
        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
 
    /**
     * Simple OnExpandListener interface
     */
    public interface OnExpandListener {
        public void onExpand(View handle, View content); 
        public void onCollapse(View handle, View content);
    }
 
    private class DefaultOnExpandListener implements OnExpandListener {
        public void onCollapse(View handle, View content) {}
        public void onExpand(View handle, View content) {}
    }
}
