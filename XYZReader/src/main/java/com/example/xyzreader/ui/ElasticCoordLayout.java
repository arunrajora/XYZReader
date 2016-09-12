package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.example.xyzreader.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajor on 12-Sep-16.
 */
public class ElasticCoordLayout extends FrameLayout {
	private float lastDir=0;
	private float dismissFraction=-1;
	private float dismissScale=1;
	private float elacticity=0.8f;
	private float totalDragged;
	private float dismissDist=Float.MAX_VALUE;
	private boolean dragUp=false;
	private boolean dragDown=false;
	private List<ElasticCallback> mCallbacks;
	public ElasticCoordLayout(Context context) {
		this(context,null,0,0);
	}

	public ElasticCoordLayout(Context context, AttributeSet attrs) {
		this(context, attrs,0,0);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ElasticCoordLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr,0);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ElasticCoordLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		final TypedArray values=getContext().obtainStyledAttributes(attrs,
				R.styleable.ElasticDragDismissFrameLayout,0,0);
		if(values.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissDistance))
			dismissDist=values.getDimensionPixelSize(R.styleable.ElasticDragDismissFrameLayout_dragDismissDistance,0);

		else if(values.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissFraction))
			dismissFraction=values.getFloat(R.styleable.ElasticDragDismissFrameLayout_dragDismissFraction,-1);


		if(values.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragElasticity))
			elacticity=values.getFloat(R.styleable.ElasticDragDismissFrameLayout_dragElasticity,0.8f);

		if(values.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissScale))
			dismissScale=values.getFloat(R.styleable.ElasticDragDismissFrameLayout_dragDismissScale,1);

		values.recycle();
	}


	@Override
	public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
		return (nestedScrollAxes & View.SCROLL_AXIS_VERTICAL)!=0;
	}

	@Override
	public void onStopNestedScroll(View child) {
		Log.d("findmee",String.valueOf(lastDir)+" "+String.valueOf(dragDown)+" "+String.valueOf(dragUp)+" "+String.valueOf(dismissDist)+" "+String.valueOf(totalDragged));
		if(dismissDist<=Math.abs(totalDragged) && ((lastDir<0 && dragDown)|| (lastDir>0 && dragUp))){
			dispatchDismissedCallbacks();
		}
		else {
			animate().scaleY(1).scaleX(1).setDuration(250).translationY(0)
			.setInterpolator(AnimationUtils.loadInterpolator(getContext(),android.R.interpolator.linear_out_slow_in))
			.start();
			totalDragged=0;
			dragUp=dragDown=false;
			dispatchCallbacks(0,0,0,0);
		}
		lastDir=0;
	}

	@Override
	public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
		scrollByDistance(dyUnconsumed);
	}

	@Override
	public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
		if(dragDown && dy>0 || dragUp && dy<0){
			scrollByDistance(dy);
			consumed[1]=dy;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (dismissFraction>0)
			dismissDist=dismissFraction*h;
	}

	private void scrollByDistance(int mScroll){
		totalDragged+=mScroll;
		if (mScroll!=0){
			lastDir=mScroll;
			if(!(dragUp || dragDown))
			{
				dragDown|=(mScroll<0);
				dragUp|=(mScroll>0);
				setPivotY(mScroll<0?getHeight():0);
			}
			float df=(float)Math.log10(1+(Math.abs(totalDragged)/dismissDist));
			float dTo=(dragUp?-1:1)*df*dismissDist*elacticity;
			setTranslationY(dTo);
			final float scale=1-((1-dismissScale)*df);
			setScaleY(scale);
			setScaleX(scale);
			if((dragDown && totalDragged>=0) || (dragUp && totalDragged<=0)){
				totalDragged=df=dTo=0;
				dragUp=dragDown=false;
				setScaleY(1);
				setScaleX(1);
				setTranslationY(0);
			}
			dispatchCallbacks(df,dTo,Math.min(Math.abs(totalDragged)/dismissDist,1),totalDragged);
		}
	}
	private void dispatchCallbacks(float offsetFraction,float offsetPixels,float fraction,float offset){
		if(mCallbacks!=null){
			for(ElasticCallback callback:mCallbacks){
				callback.onDrag(offsetFraction,offsetPixels
				,fraction,offset);
			}
		}
	}
	private void dispatchDismissedCallbacks(){
		if(mCallbacks!=null){
			for(ElasticCallback callback:mCallbacks){
				callback.onDismissedByDrag();
			}
		}
	}
	public void addCallback(ElasticCallback callback){
		if (mCallbacks==null) mCallbacks=new ArrayList<>();
		mCallbacks.add(callback);
	}
	public void removeCallback(ElasticCallback callback){
		if (mCallbacks!=null && mCallbacks.size()>0)
			mCallbacks.remove(callback);
	}
	public static abstract class ElasticCallback{
		void onDrag(float offsetFraction,float offsetPixels,float fraction,float offset){}
		void onDismissedByDrag(){}
	}
}
