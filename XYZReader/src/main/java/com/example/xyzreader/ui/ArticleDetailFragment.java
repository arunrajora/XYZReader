package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.Layout;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.transition.ArcMotion;
import android.transition.Slide;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.AndroidAuthenticator;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
	private CoordinatorLayout mMetaBarCoordinatorLayout;
    private int mMutedColor = 0xFF333333;
    private NestedScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;
	/**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);
        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                mTopInset = insets.top;
            }
        });

        mScrollView = (NestedScrollView) mRootView.findViewById(R.id.scrollview);
		mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
			@Override
			public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				mScrollY = mScrollView.getScrollY();
				float f=updateStatusBar();
				int rid=getResources().getIdentifier("status_bar_height", "dimen", "android");

				if(f>=1.0){
					int disp=getResources().getDimensionPixelSize(R.dimen.detail_card_top_margin);
					disp-=scrollY;
					disp-=getResources().getDimensionPixelSize(rid);
					mMetaBarCoordinatorLayout.setTranslationY(-disp);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						mMetaBarCoordinatorLayout.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
					}
				}
				else{
					mMetaBarCoordinatorLayout.setTranslationY(0);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						mMetaBarCoordinatorLayout.setElevation(0);
					}
				}
			}
		});
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
		mMetaBarCoordinatorLayout= ((CoordinatorLayout) mRootView.findViewById(R.id.meta_bar_coord));

		mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mStatusBarColorDrawable = new ColorDrawable(0);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
	    bindViews();
		updateStatusBar();
        return mRootView;
    }

    private float updateStatusBar() {
        int color = 0;
		float f=0;
		if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            f=progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
			int arr[]=new int[2];
			color = Color.argb((int) (255 * f),
				(int) (Color.red(mMutedColor) * 0.9),
				(int) (Color.green(mMutedColor) * 0.9),
				(int) (Color.blue(mMutedColor) * 0.9));
		}
        mStatusBarColorDrawable.setColor(color);
        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
		return f;
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }
		TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
        if (mCursor != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				mPhotoContainerView.setTransitionName(mCursor.getString(ArticleLoader.Query._ID));
			}
			mPhotoContainerView.setTag(mCursor.getString(ArticleLoader.Query._ID));
			mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
			WindowManager wm = (WindowManager) mRootView.getContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);
			final int width = metrics.widthPixels;
			final FloatingActionButton fab=(FloatingActionButton) mRootView.findViewById(R.id.share_fab);
			final ViewGroup.MarginLayoutParams params=(ViewGroup.MarginLayoutParams) mRootView.findViewById(R.id.share_fab).getLayoutParams();
			final View metbar=mRootView.findViewById(R.id.meta_bar);
			metbar.getViewTreeObserver()
					.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
						@Override
						public void onGlobalLayout() {
							params.rightMargin= getResources().getDimensionPixelSize(R.dimen.fab_margin)+(width-(metbar.getWidth()))/2;
							fab.setLayoutParams(params);
							metbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						}
					});
			titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
								mPhotoView.setImageBitmap(imageContainer.getBitmap());
									Palette p = Palette.generate(bitmap, 12);
								try {
									mMutedColor = p.getDarkMutedColor(p.getDominantColor(getResources().getColor(R.color.palette_placeholder)));
									mRootView.findViewById(R.id.article_body).setBackgroundColor(p.getLightVibrantColor(p.getLightMutedColor(
											getResources().getColor(android.R.color.white))));
									mRootView.findViewById(R.id.meta_bar).setBackgroundColor(mMutedColor);
									Palette.Swatch swatch = p.getDarkMutedSwatch();
									if (swatch != null) {
										((TextView) mRootView.findViewById(R.id.article_title)).setTextColor(swatch.getBodyTextColor());
										((TextView) mRootView.findViewById(R.id.article_byline)).setTextColor(swatch.getTitleTextColor());
									} else {
										swatch = p.getDominantSwatch();
										if (swatch != null) {
											((TextView) mRootView.findViewById(R.id.article_title)).setTextColor(swatch.getBodyTextColor());
											((TextView) mRootView.findViewById(R.id.article_byline)).setTextColor(swatch.getTitleTextColor());
										}
									}
										swatch = p.getLightVibrantSwatch();
										if (swatch != null) {
											((TextView) mRootView.findViewById(R.id.article_body)).setTextColor(swatch.getBodyTextColor());
										} else {
											swatch = p.getLightMutedSwatch();
											if (swatch != null) {
												((TextView) mRootView.findViewById(R.id.article_body)).setTextColor(swatch.getBodyTextColor());
											}

									}
									if(((ArticleDetailActivity) getActivity()).mSendId==mRootView.findViewById(R.id.photo_container).getTransitionName())
									{
										((ArticleDetailActivity) getActivity()).imgContainer=mRootView.findViewById(R.id.photo_container);
									}
									updateStatusBar();
									mRootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
										@Override
										public boolean onPreDraw() {
											mRootView.getViewTreeObserver().removeOnPreDrawListener(this);
											getActivity().startPostponedEnterTransition();
											return true;
										}
									});

								} catch (Exception e) {
									Log.e(TAG,e.toString());
								}
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });

        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }
}
