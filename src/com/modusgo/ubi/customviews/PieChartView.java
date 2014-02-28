package com.modusgo.ubi.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View{
	
	private final RectF mCircleBounds = new RectF();
	private final Paint mBackgroundColorPaint = new Paint();
	private final Paint mBackgroundColorPaint2 = new Paint();

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
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawArc(mCircleBounds, 0, 270 , true, mBackgroundColorPaint);
		canvas.drawArc(mCircleBounds, 270, 90 , true, mBackgroundColorPaint2);
		super.onDraw(canvas);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int width = measureWidth(widthMeasureSpec);
		int height = measureWidth(heightMeasureSpec);
		
		int minSide = Math.min(width, height);
		mCircleBounds.set(0, 0, minSide, minSide);
		
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

}
