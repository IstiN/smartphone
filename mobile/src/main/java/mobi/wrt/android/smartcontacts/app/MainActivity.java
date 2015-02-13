package mobi.wrt.android.smartcontacts.app;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import by.istin.android.xcore.callable.ISuccess;
import by.istin.android.xcore.ui.DialogBuilder;
import by.istin.android.xcore.utils.ContentUtils;
import by.istin.android.xcore.utils.Intents;
import by.istin.android.xcore.utils.Log;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.ContactsFragment;
import mobi.wrt.android.smartcontacts.fragments.PhoneFragment;
import mobi.wrt.android.smartcontacts.fragments.RecentFragment;
import mobi.wrt.android.smartcontacts.fragments.SmartFragment;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;
import mobi.wrt.android.smartcontacts.view.SlidingTabLayout;


public class MainActivity extends ActionBarActivity implements IFloatHeader {

    private SlidingTabLayout mSlidingTabLayout;

    private ViewPager mViewPager;

    private RecyclerView.OnScrollListener mFloatHeaderScrollListener;

    private Set<View> mFloatHeaders = new HashSet<>();

    private Set<RecyclerView> mRecyclerViews = new HashSet<>();

    private int mAdditionalAdapterHeight;

    private FloatingActionButton mFloatingActionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final View recentCallView = findViewById(R.id.recent_call);
        final int heightRecentCall = getResources().getDimensionPixelSize(R.dimen.height_recent_call);
        final int defaultMargin = getResources().getDimensionPixelSize(R.dimen.default_margin);
        final int defaultHorizontalMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final int defaultMarginSmall = getResources().getDimensionPixelSize(R.dimen.default_margin_small);
        final int heightOfTabs = findViewById(R.id.sliding_tabs).getLayoutParams().height;
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
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
                getSupportFragmentManager().beginTransaction().addToBackStack(null).add(R.id.container, new PhoneFragment()).commit();
            }
        });
        mAdditionalAdapterHeight = heightRecentCall + defaultMargin + defaultMarginSmall + heightOfTabs;
        mFloatHeaderScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) recentCallView.getLayoutParams();
                int newTopMargin = layoutParams.topMargin - dy;
                if (newTopMargin > defaultMargin) {
                    newTopMargin = defaultMargin;
                } else if (newTopMargin < -(defaultMarginSmall + heightRecentCall)) {
                    newTopMargin = -(defaultMarginSmall + heightRecentCall);
                }
                layoutParams.topMargin = newTopMargin;
                recentCallView.setLayoutParams(layoutParams);
                for (RecyclerView view : mRecyclerViews) {
                    if (view != recyclerView) {
                        RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
                        if (layoutManager instanceof LinearLayoutManager) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                            if (firstVisibleItemPosition == 0) {
                                //we don't need to scroll if we already don't see first item
                                linearLayoutManager.scrollToPositionWithOffset(0, newTopMargin - defaultMargin);
                            }
                        }
                    }
                }
            }

        };
    }

    public void onContactMoreClick(View view) {
        final Long id = (Long) view.getTag();
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void onRecentContactClick(View view) {
        makeCall((String)view.getTag());
    }

    public void onContactClick(final View view) {
        final Long id = (Long) view.getTag();
        new Thread(new Runnable() {
            @Override
            public void run() {
                initPhoneNumbers(id, new ISuccess<List<ContentValues>>() {
                    @Override
                    public void success(List<ContentValues> contentValueses) {
                        final String[] numbers = new String[contentValueses.size()];
                        for (int i = 0; i < contentValueses.size(); i++) {
                            numbers[i] = contentValueses.get(i).getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        }
                        DialogBuilder.options(MainActivity.this, R.string.title_choose_number, numbers, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                makeCall(numbers[which]);
                            }
                        });
                    }

                }, new ISuccess<String>() {
                    @Override
                    public void success(String s) {
                        makeCall(s);
                    }
                });
            }
        }).start();
    }

    public void makeCall(String s) {
        String url = "tel:"+s;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        startActivity(intent);
    }

    public void initPhoneNumbers(Long id, final ISuccess<List<ContentValues>> result, final ISuccess<String> success) {
        final List<ContentValues> entities = ContentUtils.getEntities(this, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY}, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id + " AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL", null);
        if (entities != null && !entities.isEmpty()) {
            if (entities.size() == 1) {
                final String phoneNumber = entities.get(0).getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (!StringUtil.isEmpty(phoneNumber)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            success.success(phoneNumber);
                        }
                    });
                }
            } else {
                for (ContentValues phoneValues : entities) {
                    if (phoneValues.getAsInteger(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY) > 0) {
                        final String phone = phoneValues.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (!StringUtil.isEmpty(phone)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    success.success(phone);
                                }
                            });
                        }
                        return;
                    }
                }
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                result.success(entities);
                            }
                        }
                );
            }
        }
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
    public int attach(RecyclerView recyclerView) {
        recyclerView.setOnScrollListener(mFloatHeaderScrollListener);
        mRecyclerViews.add(recyclerView);
        return mAdditionalAdapterHeight;
    }

    @Override
    public void addTopView(View view) {
        mFloatHeaders.add(view);
    }

    @Override
    public void removeTopView(View view) {
        mFloatHeaders.remove(view);
    }
}
