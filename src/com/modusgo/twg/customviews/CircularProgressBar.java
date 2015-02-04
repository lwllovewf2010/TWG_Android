/*
 * Copyright 2013 Leon Cheng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.modusgo.twg.customviews;



import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import com.modusgo.twg.R;

public class CircularProgressBar extends ProgressBar{
	private static final String TAG = "CircularProgressBar";

	private static final int STROKE_WIDTH = 20;

	private String mTitle = "";		
	private String mSubTitle = "";

	private int mStrokeWidth = STROKE_WIDTH;
	private int mSubtitlePadding = 0;

	private final RectF mCircleBounds = new RectF();

	private final Paint mProgressColorPaint = new Paint();
	private final Paint mBackgroundColorPaint = new Paint();
	private final Paint mInnerBackgroundColorPaint = new Paint();
	private final Paint mTitlePaint = new Paint(); 
	private final Paint mSubtitlePaint = new Paint();
	private float mRadius = 0;


	public interface ProgressAnimationListener{
		public void onAnimationStart();
		public void onAnimationFinish();
		public void onAnimationProgress(int progress);
	}

	public CircularProgressBar(Context context) {
		super(context);
		init(null, 0);
	}

	public CircularProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public CircularProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	public void init(AttributeSet attrs, int style){
		
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.CircularProgressBar, style, 0);

		mProgressColorPaint.setColor(a.getColor(R.styleable.CircularProgressBar_progressColor, Color.CYAN));
		mBackgroundColorPaint.setColor(a.getColor(R.styleable.CircularProgressBar_backgroundColor, Color.WHITE));
		mInnerBackgroundColorPaint.setColor(a.getColor(R.styleable.CircularProgressBar_innerBackgroundColor, Color.GRAY));
		mTitlePaint.setColor(a.getColor(R.styleable.CircularProgressBar_titleColor, Color.WHITE));
		mSubtitlePaint.setColor(a.getColor(R.styleable.CircularProgressBar_subtitleColor, Color.WHITE));
		
		String t = a.getString(R.styleable.CircularProgressBar_cpb_title);
		if(t!=null)
			mTitle = t;
		else
			mTitle=getProgress()+"%";

		t = a.getString(R.styleable.CircularProgressBar_cpb_subtitle);
		if(t!=null)
			mSubTitle = t;

		mStrokeWidth = a.getDimensionPixelSize(R.styleable.CircularProgressBar_cpb_strokeWidth, STROKE_WIDTH);
		mRadius = a.getDimensionPixelSize(R.styleable.CircularProgressBar_cpb_radius, 20);
		mSubtitlePadding = a.getDimensionPixelSize(R.styleable.CircularProgressBar_cpb_subtitlePadding, 0);
			
		a.recycle();

		mCircleBounds.set(mStrokeWidth/2, mStrokeWidth/2, mRadius*2-mStrokeWidth/2, mRadius*2-mStrokeWidth/2); 
		
		mProgressColorPaint.setAntiAlias(true);
		mProgressColorPaint.setStyle(Paint.Style.STROKE);
		mProgressColorPaint.setStrokeWidth(mStrokeWidth);

		mBackgroundColorPaint.setAntiAlias(true);
		mBackgroundColorPaint.setStyle(Paint.Style.STROKE);
		mBackgroundColorPaint.setStrokeWidth(mStrokeWidth);
		
		mInnerBackgroundColorPaint.setAntiAlias(true);
		mInnerBackgroundColorPaint.setStyle(Paint.Style.FILL);
		
		mTitlePaint.setTextSize(a.getDimensionPixelSize(R.styleable.CircularProgressBar_cpb_titleSize, 60)); 
		mTitlePaint.setStyle(Style.FILL);
		mTitlePaint.setAntiAlias(true);

		mSubtitlePaint.setTextSize(a.getDimensionPixelSize(R.styleable.CircularProgressBar_cpb_subtitleSize, 30));
		mSubtitlePaint.setStyle(Style.FILL);
		mSubtitlePaint.setAntiAlias(true);
		
		float textWidth = mSubtitlePaint.measureText(mSubTitle);
		if(mCircleBounds.height()+mStrokeWidth<textWidth){
			mCircleBounds.set(textWidth/2f-mRadius+mStrokeWidth/2, mStrokeWidth/2, textWidth/2f+mRadius-mStrokeWidth/2, mRadius*2-mStrokeWidth/2); 
		}
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		canvas.drawArc(mCircleBounds, 0, 360 , false, mBackgroundColorPaint);
		canvas.drawCircle(mCircleBounds.centerX(), mCircleBounds.centerY(), mRadius-mStrokeWidth*2f, mInnerBackgroundColorPaint);

		int prog = getProgress();
		float scale = getMax() > 0 ? (float)prog/getMax() *360: 0;

		canvas.drawArc(mCircleBounds, 270, scale , false, mProgressColorPaint);


		//if(!TextUtils.isEmpty(mTitle)){
			Rect b1 = new Rect();
			mTitlePaint.getTextBounds(mTitle, 0, mTitle.length(), b1);
			int xPos =  (int)(getMeasuredWidth()/2 - mTitlePaint.measureText(mTitle) / 2);
			int yPos = (int) ((mCircleBounds.height()+mStrokeWidth)/2f+b1.height()/2);
			canvas.drawText(mTitle, xPos, yPos, mTitlePaint); 

			Rect b = new Rect();
			mSubtitlePaint.getTextBounds(mSubTitle, 0, mSubTitle.length(), b);
			//float textHeight = (mSubtitlePaint.descent() + mSubtitlePaint.ascent());
			yPos = (int) (mCircleBounds.height()+mStrokeWidth+b.height()+mSubtitlePadding-mSubtitlePaint.getFontMetrics().descent);//+= titleHeight;
			xPos = (int)(getMeasuredWidth()/2 - mSubtitlePaint.measureText(mSubTitle) / 2);
			canvas.drawText(mSubTitle, xPos, yPos, mSubtitlePaint);
		//}		

		super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		/*Rect b = new Rect();
		mSubtitlePaint.getTextBounds("Lg", 0, 1, b);
		final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);//b.height()
		final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int min = Math.min(width, height);
		setMeasuredDimension(min+2*STROKE_WIDTH, min+2*STROKE_WIDTH+b.height());*/

		//mCircleBounds.set(STROKE_WIDTH, STROKE_WIDTH, min+STROKE_WIDTH, min+STROKE_WIDTH);
		setMeasuredDimension(measureWidth(widthMeasureSpec),measureHeight(heightMeasureSpec));
	}
	
	private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
            
            mCircleBounds.set(specSize/2f-mRadius+mStrokeWidth/2, mStrokeWidth/2, specSize/2f+mRadius-mStrokeWidth/2, mRadius*2-mStrokeWidth/2); 
        } else {
            //Calculate the width by wider element
            result = (int) (getPaddingLeft()+getPaddingRight()+Math.max(mCircleBounds.width()+mStrokeWidth, mSubtitlePaint.measureText(mSubTitle)) + 1);
        	//Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
        } else {
            //Measure the height
            result = (int)(getPaddingTop() + getPaddingBottom() + mCircleBounds.height() + mStrokeWidth + mSubtitlePaint.getFontSpacing() + mSubtitlePadding + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		mTitle = progress+"%";
		// the setProgress super will not change the details of the progress bar
		// anymore so we need to force an update to redraw the progress bar
		invalidate();
	}

	public void animateProgressTo(final int start, final int end, final ProgressAnimationListener listener){
		if(start!=0)
			setProgress(start);

		final ObjectAnimator progressBarAnimator = ObjectAnimator.ofFloat(this, "animateProgress", start, end);
		progressBarAnimator.setDuration(1500);
		//		progressBarAnimator.setInterpolator(new AnticipateOvershootInterpolator(2f, 1.5f));
		progressBarAnimator.setInterpolator(new LinearInterpolator());

		progressBarAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(final Animator animation) {
			}

			@Override
			public void onAnimationEnd(final Animator animation) {
				CircularProgressBar.this.setProgress(end);
				if(listener!=null)
					listener.onAnimationFinish();
			}

			@Override
			public void onAnimationRepeat(final Animator animation) {
			}

			@Override
			public void onAnimationStart(final Animator animation) {
				if(listener!=null)
					listener.onAnimationStart();
			}
		});

		progressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				int progress = ((Float) animation.getAnimatedValue()).intValue();
				if(progress!=CircularProgressBar.this.getProgress()){
					Log.d(TAG, progress + "");
					CircularProgressBar.this.setProgress(progress);
					if(listener!=null)
						listener.onAnimationProgress(progress);					
				}
			}
		});
		progressBarAnimator.start();
	}

	public synchronized void setTitle(String title){
		this.mTitle = title;
		invalidate();
	}

	public synchronized void setSubTitle(String subtitle){
		this.mSubTitle = subtitle;
		invalidate();
	}

	public synchronized void setSubTitleColor(int color){
		mSubtitlePaint.setColor(color);
		invalidate();
	}

	public synchronized void setTitleColor(int color){
		mTitlePaint.setColor(color);
		invalidate();
	}
	
	public synchronized void setSubTitleTypeface(Typeface tf){
		mSubtitlePaint.setTypeface(tf);
		invalidate();
	}

	public synchronized void setTitleTypeface(Typeface tf){
		mTitlePaint.setTypeface(tf);
		invalidate();
	}

	public String getTitle(){
		return mTitle;
	}
	
	@Override
	public void setAlpha(float alpha){
		int a = Math.round(alpha*255);
		mBackgroundColorPaint.setAlpha(a);
		mInnerBackgroundColorPaint.setAlpha(a);
		mProgressColorPaint.setAlpha(a);
		mSubtitlePaint.setAlpha(a);
		mTitlePaint.setAlpha(a);
	}
}
