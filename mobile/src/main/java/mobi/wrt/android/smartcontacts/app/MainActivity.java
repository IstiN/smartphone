package mobi.wrt.android.smartcontacts.app;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.melnykov.fab.FloatingActionButton;
import com.mobileapptracker.MobileAppTracker;

import java.util.HashSet;
import java.util.Set;

import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.utils.Intents;
import by.istin.android.xcore.utils.Log;
import by.istin.android.xcore.utils.ManifestMetadataUtils;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.BuildConfig;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.ads.AdsProvider;
import mobi.wrt.android.smartcontacts.drawer.DrawerInitializer;
import mobi.wrt.android.smartcontacts.fragments.ContactsFragment;
import mobi.wrt.android.smartcontacts.fragments.PhoneFragment;
import mobi.wrt.android.smartcontacts.fragments.RecentFragment;
import mobi.wrt.android.smartcontacts.fragments.SearchFragment;
import mobi.wrt.android.smartcontacts.fragments.SmartFragment;
import mobi.wrt.android.smartcontacts.gcm.GCMUtils;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;
import mobi.wrt.android.smartcontacts.utils.ThemeUtils;
import mobi.wrt.android.smartcontacts.view.DrawerArrowDrawable;
import mobi.wrt.android.smartcontacts.view.GroupOnScrollListener;
import mobi.wrt.android.smartcontacts.view.SlidingTabLayout;


public class MainActivity extends BaseControllerActivity implements IFloatHeader {

    public static final int REQUEST_CODE_PLUS = 0;
    public static final int REQUEST_CODE_RESOLUTION = 1;
    public static final int REQUEST_CODE_THEME = 2;
    private SlidingTabLayout mSlidingTabLayout;

    private ViewPager mViewPager;

    private RecyclerView.OnScrollListener mFloatHeaderScrollListener;


    private Set<RecyclerView> mRecyclerViews = new HashSet<>();

    private int mAdditionalAdapterHeight;

    private FloatingActionButton mFloatingActionButton;

    private boolean isFabClicked = false;

    private AdsProvider mAdsProvider = new AdsProvider();

    public MobileAppTracker mobileAppTracker = null;

    private CallbackManager callbackManager;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress = false;

    private DrawerInitializer mDrawerInitializer = new DrawerInitializer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        GCMUtils.register(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.xd(MainActivity.this, "plus onConnected");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.xd(MainActivity.this, "plus onConnectionSuspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.xd(MainActivity.this, "plus onConnectionFailed " + connectionResult);
                        if (!mIntentInProgress && !connectionResult.hasResolution()) {
                            try {
                                mIntentInProgress = true;
                                PendingIntent resolution = connectionResult.getResolution();
                                if (resolution != null) {
                                    IntentSender intentSender = resolution.getIntentSender();
                                    if (intentSender != null) {
                                        startIntentSenderForResult(intentSender,
                                                REQUEST_CODE_RESOLUTION, null, 0, 0, 0);
                                    }
                                }
                            } catch (IntentSender.SendIntentException e) {
                                // The intent was canceled before it was sent.  Return to the default
                                // state and attempt to connect to get an updated ConnectionResult.
                                mIntentInProgress = false;
                                mGoogleApiClient.connect();
                            }
                        }
                    }
                })
                .addApi(Plus.API)
                //.addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        mAdsProvider.onCreate(this);
        setContentView(R.layout.activity_main);

        // Initialize MAT
        mobileAppTracker = MobileAppTracker.init(getApplicationContext(),
                BuildConfig.MAT_ADVERTISER_ID,
                BuildConfig.MAT_CONVERSION_KEY);


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

        mDrawerInitializer.init(this, (ListView) findViewById(R.id.left_drawer));
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
                        return getString(R.string.tab_speed_dial);
                    case 1:
                        return getString(R.string.tab_recents);
                    case 2:
                        return getString(R.string.tab_all_contacts);
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
                    if (mCurrentView != null) {
                        return;
                    }
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
                        view.scrollBy(dx, dy);
                    }
                }
            }

        };
        proceedIntent(getIntent());
        if (UiUtil.hasKitKat() && !UiUtil.hasL()) {
            View mainContainer = findViewById(R.id.main_container);
            mainContainer.setPadding(0, UiUtil.getStatusBarHeight(this), 0, 0);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        proceedIntent(intent);
    }

    private void proceedIntent(Intent intent) {
        if (intent != null) {
            String link = intent.getStringExtra("link");
            if (!StringUtil.isEmpty(link)) {
                Intents.openBrowser(this, link);
                finish();
                return;
            }
            Uri data = intent.getData();
            if (data != null && data.getScheme().equalsIgnoreCase("tel")) {
                String path = data.getSchemeSpecificPart();
                showPhone(path);
                getTracker().track("onShowPhone:intent");
            }
            String action = intent.getAction();
            String type = intent.getType();
            if (action != null && "com.android.phone.action.RECENT_CALLS".equals(action)) {
                mViewPager.setCurrentItem(1);
            } else if ("android.intent.action.VIEW".equals(action) && "vnd.android.cursor.dir/calls".equals(type)) {
                mViewPager.setCurrentItem(1);
            } else if (type != null && "vnd.android.cursor.dir/calls".equals(type)) {
                mViewPager.setCurrentItem(1);
                onBackPressed();
            }
            removeMissedCallNotifications(this);
        }
    }

    /** Removes the missed call notifications. */
    public static void removeMissedCallNotifications(final Context context) {
        // Clear the list of new missed calls.
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put(CallLog.Calls.NEW, 0);
                values.put(CallLog.Calls.IS_READ, 1);
                StringBuilder where = new StringBuilder();
                where.append(CallLog.Calls.NEW);
                where.append(" = 1 AND ");
                where.append(CallLog.Calls.TYPE);
                where.append(" = ?");
                context.getContentResolver().update(CallLog.Calls.CONTENT_URI, values, where.toString(),
                        new String[]{ Integer.toString(CallLog.Calls.MISSED_TYPE) });
            }
        }).start();
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
                if (isFinishing()) {
                    return;
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

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get source of open for app re-engagement
        mobileAppTracker.setReferralSources(this);
        // MAT will not function unless the measureSession call is included
        mobileAppTracker.measureSession();

        String facebookId = ManifestMetadataUtils.getString(this, "com.facebook.sdk.ApplicationId");
        AppEventsLogger.activateApp(this, facebookId);
        Log.xd(this, facebookId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }


    @Override
    public void attach(RecyclerView.OnScrollListener scrollListener, RecyclerView recyclerView) {
        if (scrollListener != null) {
            recyclerView.setOnScrollListener(new GroupOnScrollListener(scrollListener, mFloatHeaderScrollListener));
        } else {
            recyclerView.setOnScrollListener(mFloatHeaderScrollListener);
        }
        recyclerView.setPadding(recyclerView.getPaddingTop(), mAdditionalAdapterHeight, recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());
        mRecyclerViews.add(recyclerView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION) {
            mIntentInProgress = false;
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else if (requestCode == REQUEST_CODE_THEME && resultCode == RESULT_OK) {
            ThemeUtils.ThemeValue themeValue = ThemeUtils.ThemeValue.values()[data.getIntExtra(ThemesActivity.EXTRA_THEME_ORDINAL_KEY, 0)];
            ITracker.Impl.get(this).track("theme:"+themeValue.name());
            ThemeUtils.setTheme(this, themeValue);
        } else if (requestCode == REQUEST_CODE_PLUS) {
            mDrawerInitializer.refreshButtons();
        }
    }
}
