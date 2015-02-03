package mobi.wrt.android.smartcontacts.helper;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.XCoreHelper;
import by.istin.android.xcore.utils.AppUtils;
import by.istin.android.xcore.utils.StringUtil;

/**
 * Created by IstiN on 03.02.2015.
 */
public class ContactHelper implements XCoreHelper.IAppServiceKey {

    public static final String APP_SERVICE_KEY = "xcore:app:contacthelper";

    public static ContactHelper get(Context context) {
        return AppUtils.get(context, APP_SERVICE_KEY);
    }

    private Map<String, String> mPhotoUriCache = new ConcurrentHashMap<>();

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

    public String initPhotoUri(String phone) {
        String resultUri = mPhotoUriCache.get(phone);
        if (resultUri == null) {
            resultUri = getContactIdFromNumber(phone);
            mPhotoUriCache.put(phone, resultUri);
        }
        return resultUri;
    }

    private String getContactIdFromNumber(String number) {
        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.PHOTO_URI};
        Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                Uri.encode(number));
        Cursor c = ContextHolder.get().getContentResolver().query(contactUri, projection,
                null, null, null);
        if (c.moveToFirst()) {
            String photoUri = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
            c.close();
            return photoUri == null ? StringUtil.EMPTY : photoUri;
        }
        return StringUtil.EMPTY;
    }
}
