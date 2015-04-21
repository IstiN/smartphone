package mobi.wrt.android.smartcontacts;

import android.content.Context;

import by.istin.android.xcore.XCoreHelper;
import by.istin.android.xcore.analytics.ITracker;
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
        ITracker tracker = ITracker.Impl.newInstance();
        tracker.addTracker(new FlurryTracker("WTGTFV2VFJQQGQCC724X"));
        tracker.addTracker(new GoogleTracker("UA-62124640-1"));
        registerAppService(tracker);
        tracker.onCreate((Application)context);
    }

}
