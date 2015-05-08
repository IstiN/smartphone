package mobi.wrt.android.smartcontacts;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import by.istin.android.xcore.preference.PreferenceHelper;
import by.istin.android.xcore.utils.StringUtil;
import mobi.wrt.android.smartcontacts.ads.AdsView;
import mobi.wrt.android.smartcontacts.app.MainActivity;

/**
 * Created by IstiN on 15.12.13.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GCMIntentService";
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = StringUtil.decode(gcm.getMessageType(intent));
        if (extras != null) {
            String extraTitle = extras.getString("title");
            String title = StringUtil.decode(extraTitle);
            String extraType = extras.getString("type");
            String type = StringUtil.decode(extraType);
            String extraMessage = extras.getString("message");
            String message = StringUtil.decode(extraMessage);
            String extraQ = extras.getString("q");
            String q = StringUtil.decode(extraQ);
            sendNotification(this, title, type, message, q);
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    public static void sendNotification(Context context, String title, String type, String message, String q) {
        //need to show ads in any case
        PreferenceHelper.set(AdsView.LAST_ADS_SHOWN, System.currentTimeMillis() - 5* DateUtils.HOUR_IN_MILLIS);

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent;
        if (!StringUtil.isEmpty(type) && type.equalsIgnoreCase("link") && !StringUtil.isEmpty(q)) {
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("link", q);
        } else if (StringUtil.isEmpty(q)) {
            intent = new Intent(context, MainActivity.class);
        } else {
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("is_search", true);
            intent.putExtra("type", type);
            intent.putExtra(SearchManager.QUERY, q);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_communication_call)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
