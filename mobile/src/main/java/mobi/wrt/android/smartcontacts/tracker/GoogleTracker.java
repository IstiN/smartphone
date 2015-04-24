package mobi.wrt.android.smartcontacts.tracker;

import android.app.Activity;
import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import by.istin.android.xcore.analytics.AbstractTracker;
import mobi.wrt.android.smartcontacts.Constants;

/**
 * Created by uladzimir_klyshevich on 4/21/15.
 */
public class GoogleTracker extends AbstractTracker {

    private String mKey;

    private Tracker mTracker;

    public GoogleTracker(String key) {
        this.mKey = key;
    }

    @Override
    public void track(HashMap params) {
        mTracker.send(params);
    }

    @Override
    public void track(String action) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("action")
                .setAction(action)
                .build());
    }

    @Override
    public void track(String action, HashMap<String, String> params) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("action")
                .setAction(action)
                .setAll(params).build());
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
        GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(application);
        mTracker = googleAnalytics.newTracker(mKey);
        mTracker.setAppVersion(Constants.ANALYTICS_VERSION_NAME);
    }

}
