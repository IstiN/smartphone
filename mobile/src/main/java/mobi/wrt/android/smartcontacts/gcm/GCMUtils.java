package mobi.wrt.android.smartcontacts.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Locale;

import by.istin.android.xcore.Core;
import by.istin.android.xcore.callable.ISuccess;
import by.istin.android.xcore.preference.PreferenceHelper;
import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.source.impl.http.HttpAndroidDataSource;
import by.istin.android.xcore.utils.Log;
import by.istin.android.xcore.utils.StringUtil;
import mobi.wrt.android.smartcontacts.BuildConfig;

/**
 * Created by IstiN on 15.12.13.
 */
public class GCMUtils {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String GCM_REGISTERED = "GCM_REGISTERED";

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    public static String SENDER_ID = BuildConfig.GOOGLE_SENDER_ID;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "GCMUtils";

    public static boolean checkPlayServices(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS && context instanceof Activity) {
            Activity activity = (Activity)context;
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                /*GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) context,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();*/
            } else {
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public static void register(Context context) {
        boolean isGcmRegistered = PreferenceHelper.getBoolean(GCM_REGISTERED, false);
        if (checkPlayServices(context) && !isGcmRegistered) {
            String regid = getRegistrationId(context);
            if (regid.isEmpty()) {
                registerInBackground(context);
            } else {
                sendRegistrationIdToBackend(context, regid);
            }
        }
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.d(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private static void sendRegistrationIdToBackend(Context context, String registerId) {
        // Your implementation here.
        Log.d(TAG, "send request to register gcm");

        String registerEncodedId = StringUtil.encode(registerId);
        String resultUrl ="http://ads.wrt.mobi/register?regId=" + registerEncodedId+"&ai=" + context.getPackageName()+"&l=" + Locale.getDefault().getLanguage();
        String postUrl = HttpAndroidDataSource.DefaultHttpRequestBuilder.getUrl(resultUrl, HttpAndroidDataSource.DefaultHttpRequestBuilder.Type.POST);
        DataSourceRequest pDataSourceRequest = new DataSourceRequest(postUrl);
        pDataSourceRequest.setCacheable(false);
        pDataSourceRequest.setForceUpdateData(true);
        Core.ExecuteOperationBuilder executeOperationBuilder = new Core.ExecuteOperationBuilder<RegisterDeviceProcessor.Response>()
                .setDataSourceServiceListener(new Core.SimpleDataSourceServiceListener() {
                    @Override
                    public void onDone(Bundle bundle) {
                        Log.d("gcm", "gcm registered");
                        PreferenceHelper.set(GCM_REGISTERED, true);
                    }
                })
                .setSuccess(new ISuccess<RegisterDeviceProcessor.Response>() {
                    @Override
                    public void success(RegisterDeviceProcessor.Response vkResponse) {
                        Log.d("gcm", "gcm registered");
                        PreferenceHelper.set(GCM_REGISTERED, true);
                    }
                })
                .setDataSourceRequest(pDataSourceRequest)
                .setDataSourceKey(HttpAndroidDataSource.SYSTEM_SERVICE_KEY)
                .setProcessorKey(RegisterDeviceProcessor.APP_SERVICE_KEY)
                ;
        Core.get(context).execute(executeOperationBuilder.build());
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.d(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private static void registerInBackground(final Context context) {
        new AsyncTask<Void,Void,String>() {

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    GoogleCloudMessaging gcm = null;
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    String regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend(context, regid);

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d(TAG, msg);
            }
        }.execute(null, null, null);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(context.getClass().getSimpleName(),
                Context.MODE_PRIVATE);
    }

}
