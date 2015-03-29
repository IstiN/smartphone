package mobi.wrt.android.smartcontacts.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.XCoreHelper;
import by.istin.android.xcore.utils.AppUtils;
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
        String resultUri = initPhotoUri(phone);
        if (resultUri.equals(StringUtil.EMPTY)) {
            return null;
        }
        return resultUri;
    }

    public Long getContactId(String phone) {
        return mContactIdCache.get(phone);
    }

    public String initPhotoUri(String phone) {
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
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor c = ContextHolder.get().getContentResolver().query(uri, PROJECTION, null, null, null);
        if (c.moveToFirst()) {
            String photoUri = CursorUtils.getString(ContactsContract.Contacts.PHOTO_URI, c);
            Long id = CursorUtils.getLong(ContactsContract.Contacts._ID, c);
            mContactIdCache.put(number, id);
            CursorUtils.close(c);
            return photoUri == null ? StringUtil.EMPTY : photoUri;
        } else {
            CursorUtils.close(c);
        }
        return StringUtil.EMPTY;
    }
}
