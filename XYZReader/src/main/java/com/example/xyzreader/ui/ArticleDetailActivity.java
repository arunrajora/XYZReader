package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.ArcMotion;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;
	private ElasticCoordLayout elasticCoordinatorLayout;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;
	public View imgContainer;
	public int mPosition;
	public String mSendId;
	private ElasticCoordLayout.ElasticCallback elasticDismissCallbackHandler;
	private final SharedElementCallback mCallback=new SharedElementCallback() {
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		@Override
		public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
			if( imgContainer!=null){
				names.clear();
				sharedElements.clear();
				mSendId=imgContainer.getTransitionName();
				names.add(mSendId);
				sharedElements.put(mSendId,imgContainer);
			}

		}
	};

	@Override
	protected void onPause() {
		elasticCoordinatorLayout.removeCallback(elasticDismissCallbackHandler);
		super.onPause();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void finishAfterTransition() {
		Intent data=new Intent();
		if(mPager!=null && mPager.getChildAt(mPosition)!=null)
			imgContainer=mPager.getChildAt(mPosition).findViewById(R.id.photo_container);
		if (imgContainer!=null)
		{
			data.putExtra("position",mPosition);
			data.putExtra("tag",imgContainer.getTransitionName());
			setResult(RESULT_OK,data);
		}
		super.finishAfterTransition();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("tag",mSendId);
		super.onSaveInstanceState(outState);
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setEnterSharedElementCallback(mCallback);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			Slide slide = new Slide();
			slide.addTarget(R.id.max_width_linear_layout);
			slide.addTarget(R.id.share_fab);
			slide.setInterpolator(AnimationUtils.loadInterpolator(this,android.R.interpolator.linear_out_slow_in));
			getWindow().setEnterTransition(slide);
			Fade fade=new Fade();
			fade.setDuration(100);
			getWindow().setExitTransition(fade);
		}


		supportPostponeEnterTransition();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);
	    getLoaderManager().initLoader(0, null, this);
		elasticDismissCallbackHandler=new ElasticDismissCallbackHandler(this);
		elasticCoordinatorLayout=(ElasticCoordLayout) findViewById(R.id.elastic);
        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setPageTransformer(true,new zoomOutPageTransformer());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                mUpButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
					if(mPager!=null && mPager.getChildAt(position)!=null)
					{
						imgContainer=mPager.getChildAt(position).findViewById(R.id.photo_container);
					}
				}
				mPosition=position;
			}
        });

        mUpButtonContainer = findViewById(R.id.up_container);

        mUpButton = findViewById(R.id.action_up);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    view.onApplyWindowInsets(windowInsets);
                    mTopInset = windowInsets.getSystemWindowInsetTop();
                    mUpButtonContainer.setTranslationY(mTopInset+10);
                    return windowInsets;
                }
            });
        }
		if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
				mSendId=getIntent().getStringExtra("tagtag");
				mPosition=mPager.getCurrentItem();
			}
        }
		else
		{
			mSendId=savedInstanceState.getString("tag");
		}
    }
	@Override
	protected void onResume(){
		super.onResume();
		elasticCoordinatorLayout.addCallback(elasticDismissCallbackHandler);
	}

	@Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }
        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
