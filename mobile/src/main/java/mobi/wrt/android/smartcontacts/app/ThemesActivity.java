package mobi.wrt.android.smartcontacts.app;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.RecentFragment;
import mobi.wrt.android.smartcontacts.fragments.ThemesFragment;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 18.02.2015.
 */
public class ThemesActivity extends BaseControllerActivity implements IFloatHeader {

    public static final String EXTRA_THEME_ORDINAL_KEY = "theme_ordinal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_fragment);
        if (savedInstanceState == null) {
            ThemesFragment fragment = new ThemesFragment();
            Bundle bundle = new Bundle();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }
    }

    @Override
    public int attach(RecyclerView.OnScrollListener scrollListener, RecyclerView recyclerView) {
        return 0;
    }

    @Override
    public void addTopView(View view) {

    }

    @Override
    public void removeTopView(View view) {

    }
}
