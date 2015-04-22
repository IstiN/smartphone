package mobi.wrt.android.smartcontacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.mobileapptracker.Tracker;

/**
 * Created by uladzimir_klyshevich on 4/22/15.
 */
public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new CampaignTrackingReceiver().onReceive(context, intent);
        new Tracker().onReceive(context, intent);
    }

}
