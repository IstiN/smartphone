package mobi.wrt.android.smartcontacts;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Iterator;
import java.util.Set;

import by.istin.android.xcore.utils.Log;

/**
 * Created by IstiN on 15.12.13.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GCMIntentService will handle the intent.
        // Explicitly specify that GCMIntentService will handle the intent.
        Log.xd(this, "new message " + intent.getAction());
        Bundle extras = intent.getExtras();
        Log.xd(this, "new message " + extras);
        if (extras != null) {
            Set<String> strings = extras.keySet();
            Iterator<String> iterator = strings.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Log.xd(this, "key " + key + " " + extras.get(key));
            }
        }
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
