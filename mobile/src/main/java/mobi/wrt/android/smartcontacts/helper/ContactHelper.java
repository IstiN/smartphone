package mobi.wrt.android.smartcontacts.helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;

import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.XCoreHelper;
import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.ui.DialogBuilder;
import by.istin.android.xcore.utils.AppUtils;
import by.istin.android.xcore.utils.ContentUtils;
import by.istin.android.xcore.utils.CursorUtils;
import by.istin.android.xcore.utils.StringUtil;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.utils.ColorUtils;

/**
 * Created by IstiN on 03.02.2015.
 */
public class ContactHelper implements XCoreHelper.IAppServiceKey {

    public static final String APP_SERVICE_KEY = "xcore:app:contacthelper";
    public static final String[] PROJECTION = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.PHOTO_URI};

    public static final String[] PHONES_PROJECTION = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY, ContactsContract.CommonDataKinds.Phone.TYPE};
    private static String PHONES_SELECTION = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " = 1";

    public static final String[] EMAILS_PROJECTION = new String[]{ContactsContract.CommonDataKinds.Email.ACCOUNT_TYPE_AND_DATA_SET};
    private static String EMAILS_SELECTION = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";



    public static ContactHelper get(Context context) {
        return AppUtils.get(context, APP_SERVICE_KEY);
    }

    private Map<String, String> mPhotoUriCache = new ConcurrentHashMap<>();

    private Map<String, Long> mContactIdCache = new ConcurrentHashMap<>();

    @Override
    public String getAppServiceKey() {
        return APP_SERVICE_KEY;
    }

    public String getContactPhotoUri(String phone) {
        String resultUri = initPhotoAndContactIdUri(phone);
        if (resultUri.equals(StringUtil.EMPTY)) {
            return null;
        }
        return resultUri;
    }

    public Long getContactId(String phone) {
        return mContactIdCache.get(phone);
    }

    public String initPhotoAndContactIdUri(String phone) {
        String resultUri = mPhotoUriCache.get(phone);
        if (resultUri == null) {
            resultUri = getContactIdFromNumber(phone);
            if (resultUri.equals(StringUtil.EMPTY)) {
                ColorUtils.getColorCircle(ContextHolder.get().getResources().getDimensionPixelSize(R.dimen.icon_size), phone);
            }
            mPhotoUriCache.put(phone, resultUri);
        }
        return resultUri;
    }

    private String getContactIdFromNumber(String number) {
        if (StringUtil.isEmpty(number)) {
            return StringUtil.EMPTY;
        }
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        try {
            Cursor c = ContextHolder.get().getContentResolver().query(uri, PROJECTION, null, null, null);
            if (!CursorUtils.isEmpty(c) && c.moveToFirst()) {
                String photoUri = CursorUtils.getString(ContactsContract.Contacts.PHOTO_URI, c);
                Long id = CursorUtils.getLong(ContactsContract.Contacts._ID, c);
                mContactIdCache.put(number, id);
                CursorUtils.close(c);
                return photoUri == null ? StringUtil.EMPTY : photoUri;
            } else {
                CursorUtils.close(c);
            }
        } catch (IllegalArgumentException e) {
            Crashlytics.logException(new IllegalArgumentException("number:" + number, e));
        }
        return StringUtil.EMPTY;
    }

    public List<ContentValues> getPhonesById(Long id) {
        String[] projection = PHONES_PROJECTION;
        //String[] projection = null;
        return ContentUtils.getEntities(ContextHolder.get(), projection, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, PHONES_SELECTION, new String[]{String.valueOf(id)});
    }

    public List<ContentValues> getEmailsById(Long id) {
        String[] projection = EMAILS_PROJECTION;
        //String[] projection = null;
        return ContentUtils.getEntities(ContextHolder.get(), projection, ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, EMAILS_SELECTION, new String[]{String.valueOf(id)});
    }

    public CharSequence getPhoneTypeLabel(Integer value) {
        if (value == null) {
            return StringUtil.EMPTY;
        }
        Context context = ContextHolder.get();
        return ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), value, StringUtil.EMPTY);
    }

    public void removeCallLog(final Context context, final Long id, final Runnable runnable) {
        final Handler handler = new Handler();
        DialogBuilder.confirm(context, context.getString(R.string.clearCallLogConfirmation_title), context.getString(R.string.recentCalls_removeFromRecentList), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ITracker.Impl.get(context).track("removeCallLog");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ContextHolder.get().getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + "=" + String.valueOf(id), null);
                        if (runnable != null) {
                            handler.post(runnable);
                        }
                    }
                }).start();
            }
        });
    }

    public void removePhoneCache(String phone) {
        mContactIdCache.remove(phone);
        mPhotoUriCache.remove(phone);
    }
}
