package mobi.wrt.android.smartcontacts.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.ContactsFragment;
import mobi.wrt.android.smartcontacts.fragments.RecentFragment;
import mobi.wrt.android.smartcontacts.fragments.SmartFragment;
import mobi.wrt.android.smartcontacts.view.SlidingTabLayout;


public class MainActivity extends ActionBarActivity {

    private SlidingTabLayout mSlidingTabLayout;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    }

    public void onContactMoreClick(View view) {

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
}
