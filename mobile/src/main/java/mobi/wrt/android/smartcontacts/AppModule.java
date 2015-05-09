package mobi.wrt.android.smartcontacts;

import android.content.Context;

import by.istin.android.xcore.XCoreHelper;
import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.source.impl.http.HttpAndroidDataSource;
import mobi.wrt.android.smartcontacts.ads.AdsProcessor;
import mobi.wrt.android.smartcontacts.config.ConfigProcessor;
import mobi.wrt.android.smartcontacts.gcm.RegisterDeviceProcessor;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.tracker.FlurryTracker;
import mobi.wrt.android.smartcontacts.tracker.GoogleTracker;

/**
 * Created by IstiN on 31.01.2015.
 */
public class AppModule extends XCoreHelper.BaseModule {

    @Override
    protected void onCreate(Context context) {
        registerAppService(new ContactHelper());
        registerAppService(new AdsProcessor());
        registerAppService(new ConfigProcessor());
        registerAppService(new RegisterDeviceProcessor());
        registerAppService(new HttpAndroidDataSource());
        ITracker tracker = ITracker.Impl.newInstance();
        if (!BuildConfig.DEBUG) {
            tracker.addTracker(new FlurryTracker(BuildConfig.FLURRY_KEY));
            tracker.addTracker(new GoogleTracker(BuildConfig.GOOGLE_ANALYTICS_KEY));
        }
        registerAppService(tracker);
        tracker.onCreate((Application)context);
    }

}
