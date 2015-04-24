package mobi.wrt.android.smartcontacts.view;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.share.internal.LikeButton;
import com.facebook.share.widget.LikeView;
import com.google.android.gms.plus.PlusOneButton;
import com.squareup.picasso.Picasso;

import by.istin.android.xcore.utils.CursorUtils;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.Application;
import mobi.wrt.android.smartcontacts.R;

/**
 * Created by uladzimir_klyshevich on 4/23/15.
 */
public class DrawerInitializer {
    public static final String SELECTION = ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
            + ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
            + ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
            + ContactsContract.Contacts.Data.MIMETYPE + "=?";
    final static Uri PROFILE_URI = Uri.withAppendedPath(
            ContactsContract.Profile.CONTENT_URI,
            ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
    public static final String[] SELECTION_ARGS = new String[]{
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
    };
    final String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
            ContactsContract.Contacts.Data.MIMETYPE
    };

    private static final int PLUS_ONE_REQUEST_CODE = 0;

    private View mHeader;
    private PlusOneButton mPlusOneButton;
    private LikeView mLikeButton;

    public void init(final FragmentActivity activity, final ListView listView) {
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.width = (int)((float)UiUtil.getDisplayWidth() * 0.85f);
        listView.setLayoutParams(layoutParams);
        activity.getSupportLoaderManager().restartLoader(PROFILE_URI.hashCode(), null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(activity,
                        // Retrieve data rows for the device user's 'profile' contact.
                        Uri.withAppendedPath(
                                ContactsContract.Profile.CONTENT_URI,
                                ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                        PROJECTION,
                        SELECTION,
                        SELECTION_ARGS,
                        ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (mHeader == null) {
                    mHeader = View.inflate(activity, R.layout.view_drawer_header, null);
                    listView.addHeaderView(mHeader, null, false);
                    listView.setAdapter(new ArrayAdapter<>(activity,
                            android.R.layout.simple_list_item_single_choice,
                            android.R.id.text1, new String[]{})
                    );
                    mPlusOneButton = (PlusOneButton) mHeader.findViewById(R.id.plus_one_button);
                    mLikeButton = (LikeView) mHeader.findViewById(R.id.facebook_button);
                }
                //String url = BuildConfig.MARKET_URL + BuildConfig.APPLICATION_ID;
                String url = "https://play.google.com";
                mPlusOneButton.initialize(url, PLUS_ONE_REQUEST_CODE);
                mLikeButton.setObjectIdAndType(url, LikeView.ObjectType.PAGE);
                TextView nameView = (TextView) mHeader.findViewById(R.id.profile_name);
                ImageView iconView = (ImageView) mHeader.findViewById(R.id.profile_icon);

                if (!CursorUtils.isEmpty(data)) {
                    String mimeType;
                    String photoUri = StringUtil.EMPTY;
                    String phoneNumber = StringUtil.EMPTY;
                    String givenName = StringUtil.EMPTY;
                    String familyName = StringUtil.EMPTY;
                    String email = StringUtil.EMPTY;
                    data.moveToFirst();
                    do {
                        mimeType = CursorUtils.getString(ContactsContract.Contacts.Data.MIMETYPE, data);
                        switch (mimeType) {
                            case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                                email = CursorUtils.getString(ContactsContract.CommonDataKinds.Email.ADDRESS, data);
                                break;
                            case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                                givenName = CursorUtils.getString(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, data);
                                familyName = CursorUtils.getString(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, data);
                                break;
                            case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                                phoneNumber = CursorUtils.getString(ContactsContract.CommonDataKinds.Phone.NUMBER, data);
                                break;
                            case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
                                photoUri = CursorUtils.getString(ContactsContract.CommonDataKinds.Photo.PHOTO_URI, data);
                                break;
                        }
                    } while (data.moveToNext());
                    String name = StringUtil.join(" ", true, givenName, familyName);
                    if (StringUtil.isEmpty(name) && !StringUtil.isEmpty(email)) {
                        name = email;
                    }
                    if (StringUtil.isEmpty(name)) {
                        if (!StringUtil.isEmpty(phoneNumber)) {
                            nameView.setText(phoneNumber);
                        } else {
                            nameView.setText(StringUtil.EMPTY);
                        }
                    } else {
                        if (!StringUtil.isEmpty(phoneNumber)) {
                            nameView.setText(Html.fromHtml("<b>"+name+"</b><br>" + phoneNumber), TextView.BufferType.SPANNABLE);
                        } else {
                            nameView.setText(name);
                        }
                    }
                    if (!StringUtil.isEmpty(photoUri)) {
                        Picasso.with(activity).
                                load(photoUri).
                                transform(Application.ROUNDED_TRANSFORMATION).
                                into(iconView);
                        ((View)iconView.getParent()).setVisibility(View.VISIBLE);
                    } else {
                        iconView.setImageDrawable(null);
                        ((View)iconView.getParent()).setVisibility(View.INVISIBLE);
                    }
                } else {
                    nameView.setText(StringUtil.EMPTY);
                    iconView.setImageDrawable(null);
                    ((View)iconView.getParent()).setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }
}
