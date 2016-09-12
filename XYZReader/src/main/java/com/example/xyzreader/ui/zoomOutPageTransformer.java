package com.example.xyzreader.ui;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by rajor on 10-Sep-16.
 */
public class zoomOutPageTransformer implements ViewPager.PageTransformer {

	@Override
	public void transformPage(View page, float position) {
		int pw=page.getWidth();
		int ph=page.getHeight();
		if(position>=-1 && position<=1){
				float sf=(float) Math.max(0.85,1-Math.abs(position));
				float vm=ph*(1-sf)/2;
				float hm=pw*(1-sf)/2;
				page.setTranslationX((position<0?1:-1)*(hm-vm)/2);
				page.setScaleX(sf);
				page.setScaleY(sf);
		}

	}
}
