package mobi.wrt.android.smartcontacts.tracker;

import android.app.Activity;
import android.app.Application;

import com.flurry.android.FlurryAgent;

import java.util.HashMap;

import by.istin.android.xcore.analytics.AbstractTracker;
import mobi.wrt.android.smartcontacts.BuildConfig;
import mobi.wrt.android.smartcontacts.Constants;

/**
 * Created by uladzimir_klyshevich on 4/21/15.
 */
public class FlurryTracker extends AbstractTracker {

    private String mKey;

    public FlurryTracker(String key) {
        this.mKey = key;
    }

    @Override
    public void track(HashMap params) {
        FlurryAgent.logEvent("a", params);
    }

    @Override
    public void track(String action) {
        FlurryAgent.logEvent(action);
    }

    @Override
    public void track(String action, HashMap<String, String> params) {
        FlurryAgent.logEvent(action, params);
    }

    @Override
    public void onCreate(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onCreate(Application application) {
        FlurryAgent.setLogEnabled(BuildConfig.DEBUG);
        FlurryAgent.setCaptureUncaughtExceptions(false);
        FlurryAgent.setVersionName(Constants.ANALYTICS_VERSION_NAME);
        FlurryAgent.setReportLocation(false);
        FlurryAgent.init(application, mKey);
    }

}
