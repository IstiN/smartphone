package mobi.wrt.android.smartcontacts.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.melnykov.fab.FloatingActionButton;

import java.util.HashSet;
import java.util.Set;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.utils.Log;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.ads.AdsProvider;
import mobi.wrt.android.smartcontacts.fragments.ContactsFragment;
import mobi.wrt.android.smartcontacts.fragments.PhoneFragment;
import mobi.wrt.android.smartcontacts.fragments.RecentFragment;
import mobi.wrt.android.smartcontacts.fragments.SearchFragment;
import mobi.wrt.android.smartcontacts.fragments.SmartFragment;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;
import mobi.wrt.android.smartcontacts.view.DrawerArrowDrawable;
import mobi.wrt.android.smartcontacts.view.GroupOnScrollListener;
import mobi.wrt.android.smartcontacts.view.SlidingTabLayout;


public class MainActivity extends BaseControllerActivity implements IFloatHeader {

    private SlidingTabLayout mSlidingTabLayout;

    private ViewPager mViewPager;

    private RecyclerView.OnScrollListener mFloatHeaderScrollListener;


    private Set<RecyclerView> mRecyclerViews = new HashSet<>();

    private int mAdditionalAdapterHeight;

    private FloatingActionButton mFloatingActionButton;

    private boolean isFabClicked = false;

    private AdsProvider mAdsProvider = new AdsProvider();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdsProvider.onCreate(this);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    if (!mFloatingActionButton.isVisible()) {
                        mFloatingActionButton.show(true);
                    }
                }
            }
        });
        findViewById(R.id.search_input).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchInputClick(v);
            }
        });
        DrawerArrowDrawable drawable = new DrawerArrowDrawable(this, this);
        ImageView menuButton = (ImageView) findViewById(R.id.arrow);
        menuButton.setImageDrawable(drawable);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTracker().track("onHomeHamburgerClick");
                ((DrawerLayout)findViewById(R.id.drawer)).openDrawer(Gravity.START);
            }
        });
        final View headerContainer = findViewById(R.id.header);
        final View floatHeaderContainer = findViewById(R.id.float_header);
        ViewTreeObserver viewTreeObserver = headerContainer.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = headerContainer.getViewTreeObserver();
                if (viewTreeObserver.isAlive()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this);
                    } else {
                        viewTreeObserver.removeGlobalOnLayoutListener(this);
                    }
                }
                int height = headerContainer.getHeight();
                if (height > 0) {
                    mAdditionalAdapterHeight = height;
                }
                Log.xd(MainActivity.this, "height " + height);
            }
        });
        final int defaultMargin = getResources().getDimensionPixelSize(R.dimen.default_margin_8);
        final int defaultHorizontalMargin = getResources().getDimensionPixelSize(R.dimen.default_margin_16);
        final int defaultMarginSmall = getResources().getDimensionPixelSize(R.dimen.default_margin_4);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new SmartFragment();
                    case 1:
                        return new RecentFragment();
                    case 2:
                        return new ContactsFragment();
                }
                throw new IllegalStateException("check fragment count");
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.speed_dial);
                    case 1:
                        return getString(R.string.recent);
                    case 2:
                        return getString(R.string.contacts);
                }
                return null;
            }

            @Override
            public int getCount() {
                return 3;
            }

        });

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int prevValue = 0;

            private int maxValue = -1;

            private int fabMargin = -1;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    if (fabMargin == -1) {
                        int width = mFloatingActionButton.getWidth();
                        if (width > 0) {
                            fabMargin = width + defaultHorizontalMargin * 2;
                            maxValue = mViewPager.getWidth() - fabMargin;
                        }
                    }
                    if (positionOffsetPixels > maxValue) {
                        positionOffsetPixels = maxValue;
                    }
                    if (prevValue < positionOffsetPixels || prevValue > positionOffsetPixels) {
                        int value = positionOffsetPixels / 2;
                        mFloatingActionButton.animate().translationX(value).setDuration(0l).start();
                    }
                }
                prevValue = positionOffsetPixels;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPhone(null);
                getTracker().track("onShowPhone:floatButton");
            }

        });

        mFloatHeaderScrollListener = new RecyclerView.OnScrollListener() {

            private RecyclerView mCurrentView;

            private int maxHeight;

            private LinearLayout.LayoutParams mHeaderLayoutParams;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 1) {
                    mCurrentView = recyclerView;
                    maxHeight = - (defaultMarginSmall + floatHeaderContainer.getHeight() - defaultMargin);
                    mHeaderLayoutParams = (LinearLayout.LayoutParams) floatHeaderContainer.getLayoutParams();
                } else if (newState == 0) {
                    mCurrentView = null;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView != mCurrentView) {
                    return;
                }
                int newTopMargin = mHeaderLayoutParams.topMargin - dy;
                if (newTopMargin > 0) {
                    newTopMargin = 0;
                } else {
                    if (newTopMargin < maxHeight) {
                        newTopMargin = maxHeight;
                    }
                }
                if (mHeaderLayoutParams.topMargin == newTopMargin) {
                    return;
                }
                mHeaderLayoutParams.topMargin = newTopMargin;
                floatHeaderContainer.setLayoutParams(mHeaderLayoutParams);
                for (RecyclerView view : mRecyclerViews) {
                    if (view != recyclerView) {
                        RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
                        //if (layoutManager instanceof LinearLayoutManager) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                            if (firstVisibleItemPosition == 0) {
                                //we don't need to scroll if we already don't see first item
                                linearLayoutManager.scrollToPositionWithOffset(0, newTopMargin);
                            }
                        /*} else if (layoutManager instanceof StaggeredGridLayoutManager) {
                            Log.xd(MainActivity.this, "new " + newTopMargin);
                            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                            try {
                                int[] firstVisibleItemPositions = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(null);
                                if (firstVisibleItemPositions != null) {
                                    for (int pos : firstVisibleItemPositions) {
                                        if (pos < 3) {
                                            //we don't need to scroll if we already don't see first item
                                            staggeredGridLayoutManager.scrollToPositionWithOffset(0, newTopMargin);
                                            break;
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                                //staggeredgrid manager has a bug
                            }
                        }*/
                    }
                }
            }

        };
        proceedIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        proceedIntent(intent);
    }

    private void proceedIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && data.getScheme().equalsIgnoreCase("tel")) {
            String path = data.getSchemeSpecificPart();
            showPhone(path);
            getTracker().track("onShowPhone:intent");
        }
    }

    private void showPhone(final String phoneNumber) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null && fragment instanceof PhoneFragment) {
            ((PhoneFragment) fragment).updatePhone(phoneNumber);
            return;
        }
        if (isFabClicked) {
            return;
        }
        isFabClicked = true;

        mAdsProvider.onFabClick(this);

        mFloatingActionButton.hide(true);
        mFloatingActionButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                PhoneFragment fragment = new PhoneFragment();
                if (phoneNumber != null) {
                    Bundle args = new Bundle();
                    args.putString(PhoneFragment.EXTRA_PHONE, phoneNumber);
                    fragment.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction().addToBackStack(null).add(R.id.container, fragment).commit();
            }
        }, 200l);
        mFloatingActionButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                isFabClicked = false;
            }
        }, 500l);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            if (fragment instanceof SearchFragment) {
                getTracker().track("onSearchCloseBackClick");
                ((SearchFragment)fragment).closeSearch();
            } else if (fragment instanceof PhoneFragment) {
                ((PhoneFragment)fragment).closePhone();
                mAdsProvider.onPhoneClosed(this);
            }
        } else {
            mAdsProvider.onBackPressed(this);
            super.onBackPressed();
        }
    }

    public void onSearchInputClick(View view) {
        getTracker().track("onSearchInputClick");
        getSupportFragmentManager().beginTransaction().addToBackStack(null).add(R.id.container, new SearchFragment()).commit();
    }

    public void onRecentMoreClick(View view) {
        getTracker().track("onRecentMoreClick");
        startActivity(new Intent(this, RecentActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public int attach(RecyclerView.OnScrollListener scrollListener, RecyclerView recyclerView) {
        if (scrollListener != null) {
            recyclerView.setOnScrollListener(new GroupOnScrollListener(scrollListener, mFloatHeaderScrollListener));
        } else {
            recyclerView.setOnScrollListener(mFloatHeaderScrollListener);
        }

        mRecyclerViews.add(recyclerView);
        return mAdditionalAdapterHeight;
    }

    @Override
    public void addTopView(View view) {
    }

    @Override
    public void removeTopView(View view) {
    }
}
