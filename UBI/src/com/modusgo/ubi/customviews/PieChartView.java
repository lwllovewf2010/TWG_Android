package com.modusgo.ubi.customviews;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class PieChartView extends View{
	
	private final RectF mCircleBounds = new RectF();
	private final Paint mBackgroundColorPaint = new Paint();
	private final Paint mBackgroundColorPaint2 = new Paint();
	
	private final RectF mInsideCircleBounds = new RectF();
	
	PieSector[] pieSectors;
	
	private static final int START_ANGLE = 270; 

	public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mBackgroundColorPaint.setColor(Color.WHITE);
		mBackgroundColorPaint.setAntiAlias(true);
		mBackgroundColorPaint.setStyle(Paint.Style.FILL);

		mBackgroundColorPaint2.setColor(Color.YELLOW);
		mBackgroundColorPaint2.setAntiAlias(true);
		mBackgroundColorPaint2.setStyle(Paint.Style.FILL);
	}
	
	public PieChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PieChartView(Context context) {
		this(context,null);
	}
	
	public void setChartSectors(PieSector[] pieSectors){
		float sum = 0;
		for (PieSector ps : pieSectors) {
			sum+=ps.value;
		}
		for (PieSector ps : pieSectors) {
			ps.valueDegree = ps.value/sum*360;
		}
		this.pieSectors = pieSectors;
		invalidate();
	}
	
	public void animateChartSectors(final PieSector[] pieSectors){
		for (int i = 0; i < this.pieSectors.length; i++) {
			
			final ValueAnimator va = ValueAnimator.ofFloat(this.pieSectors[i].value, pieSectors[i].value);
			va.setDuration(500);
			va.setInterpolator(new LinearInterpolator());

			final int fi = i;
			va.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(final ValueAnimator animation) {
					PieChartView.this.pieSectors[fi].value = (Float)animation.getAnimatedValue();
					if(fi==PieChartView.this.pieSectors.length-1)
						setChartSectors(PieChartView.this.pieSectors);
				}
			});
			va.start();
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		float startAngle = START_ANGLE;
		
		canvas.drawArc(mCircleBounds, 0, 360 , true, mBackgroundColorPaint);
		if(pieSectors!=null){
			if(pieSectors.length==1){
				canvas.drawArc(mCircleBounds, 0, 360, true, pieSectors[0].colorPaint);
			}
			else{
				for (PieSector ps : pieSectors) {
					canvas.drawArc(mCircleBounds, startAngle, ps.valueDegree -1.5f, true, ps.colorPaint);
					startAngle += ps.valueDegree;
				}
			}
		}
		canvas.drawArc(mInsideCircleBounds, 0, 360 , true, mBackgroundColorPaint);
		super.onDraw(canvas);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int width = measureWidth(widthMeasureSpec);
		int height = measureHeight(heightMeasureSpec);
		
		int minSide = Math.min(width, height);
		mCircleBounds.set((width-minSide)/2, (height-minSide)/2, (width-minSide)/2+minSide, (height-minSide)/2+minSide);
		
		int insideMinSide = Math.min((int) (width*0.63f), (int) (height*0.63f));
		mInsideCircleBounds.set((width-insideMinSide)/2, (height-insideMinSide)/2, (width-insideMinSide)/2+insideMinSide, (height-insideMinSide)/2+insideMinSide);
		
		setMeasuredDimension(width,height);
	}
	
	private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;         
        } else {
            //Calculate the width by wider element
            result = 100;//(int) (getPaddingLeft()+getPaddingRight()+Math.max(mCircleBounds.width()+mStrokeWidth, mSubtitlePaint.measureText(mSubTitle)) + 1);
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
            result = 100;//(int)(getPaddingTop() + getPaddingBottom() + mCircleBounds.height() + mStrokeWidth + mSubtitlePaint.getFontSpacing() + mSubtitlePadding + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
    
    public class PieSector{
    	float value;
    	float valueDegree;
    	Paint colorPaint;
    	
    	public PieSector(){}
    	
    	public PieSector(float value, int color){
    		this.value = value;
    		colorPaint = new Paint();
    		colorPaint.setColor(color);
    		colorPaint.setAntiAlias(true);
    		colorPaint.setStyle(Paint.Style.FILL);
    	}
    }

}


