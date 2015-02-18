package mobi.wrt.android.smartcontacts.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.RecentFragment;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 18.02.2015.
 */
public class RecentActivity extends BaseControllerActivity implements IFloatHeader {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        if (savedInstanceState == null) {
            RecentFragment fragment = new RecentFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(RecentFragment.EXTRA_IS_LIMIT, false);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }
    }

    @Override
    public int attach(RecyclerView recyclerView) {
        return 0;
    }

    @Override
    public void addTopView(View view) {

    }

    @Override
    public void removeTopView(View view) {

    }
}
