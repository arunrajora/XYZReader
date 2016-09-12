package com.example.xyzreader.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;

/**
 * Created by rajor on 12-Sep-16.
 */
public class ElasticDismissCallbackHandler extends ElasticCoordLayout.ElasticCallback{
	private final Activity mActivity;
	private int navBarAlpha;
	private int statusBarAlpha;
	private final boolean fadeNavBar;
	public ElasticDismissCallbackHandler(Activity activity){
		mActivity=activity;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			navBarAlpha=Color.alpha(activity.getWindow().getNavigationBarColor());
			statusBarAlpha= Color.alpha(activity.getWindow().getStatusBarColor());
		}
		fadeNavBar=isNavBarOnBottom(activity);
	}
	public static boolean isNavBarOnBottom(Context context){
		final DisplayMetrics dm =context.getResources().getDisplayMetrics();
		return (!(dm.widthPixels!=dm.heightPixels && context.getResources().getConfiguration().smallestScreenWidthDp<600)) || (dm.widthPixels<dm.heightPixels);
	}
	@Override
	void onDrag(float offsetFraction, float offsetPixels, float fraction, float offset) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if(offsetPixels>0){
				mActivity.getWindow().setStatusBarColor(modifyAlpha(mActivity.getWindow().getStatusBarColor(),(int)(statusBarAlpha*(1-offset))));
			}
			if(offsetPixels>0)
			{
				mActivity.getWindow().setStatusBarColor(modifyAlpha(mActivity.getWindow().getStatusBarColor(),statusBarAlpha));
				mActivity.getWindow().setNavigationBarColor(modifyAlpha(mActivity.getWindow().getNavigationBarColor(),navBarAlpha));
			}
			else if(fadeNavBar){
				mActivity.getWindow().setNavigationBarColor(modifyAlpha(mActivity.getWindow().getNavigationBarColor(),(int)(navBarAlpha*(1-offset))));
			}
		}
	}

	@Override
	void onDismissedByDrag() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mActivity.finishAfterTransition();
		}
		else {
			mActivity.finish();
		}
	}
	public static int modifyAlpha(int color,int alpha){
		return (color & 0x00ffffff)|(alpha<<24);
	}
}
