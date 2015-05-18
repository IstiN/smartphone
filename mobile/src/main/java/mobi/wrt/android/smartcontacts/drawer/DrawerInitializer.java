package mobi.wrt.android.smartcontacts.drawer;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.InflateException;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.share.widget.LikeView;
import com.google.android.gms.plus.PlusOneButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.Core;
import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.callable.ISuccess;
import by.istin.android.xcore.preference.PreferenceHelper;
import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.source.impl.http.HttpAndroidDataSource;
import by.istin.android.xcore.utils.CursorUtils;
import by.istin.android.xcore.utils.Log;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.Application;
import mobi.wrt.android.smartcontacts.BuildConfig;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.app.MainActivity;
import mobi.wrt.android.smartcontacts.config.ConfigProcessor;

/**
 * Created by uladzimir_klyshevich on 4/23/15.
 */
public class DrawerInitializer {


    public static final String SELECTION = ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
            + ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
            + ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
            + ContactsContract.Contacts.Data.MIMETYPE + "=?";
    private final static Uri PROFILE_URI = Uri.withAppendedPath(
            ContactsContract.Profile.CONTENT_URI,
            ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
    public static final String[] SELECTION_ARGS = new String[]{
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
    };
    public static final String LIKED_TIME_KEY = "liked_time_key";
    public static final String PLUS_ONE_TIME_KEY = "plus_one_time_key";
    public static final long DELAY_FOR_HIDE_TIME = 2 * DateUtils.MINUTE_IN_MILLIS;

    private final String[] PROJECTION = {
            ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
            ContactsContract.Contacts.Data.MIMETYPE
    };

    private List<MenuItem> mCurrentMenuItems = new ArrayList<>();
    private View mHeader;
    private PlusOneButton mPlusOneButton;
    private LikeView mLikeButton;

    private ConfigProcessor.Config mConfig;

    private ArrayAdapter<MenuItem> mAdapter;


    public void init(final FragmentActivity activity, final ListView listView) {
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.width = (int)((float)UiUtil.getDisplayWidth() * 0.85f);
        if (UiUtil.hasKitKat() && !UiUtil.hasL()) {
            listView.setPadding(0, UiUtil.getStatusBarHeight(activity), 0, 0);
        }
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
                    try {
                        mHeader = View.inflate(activity, R.layout.view_drawer_header, null);
                    } catch (InflateException e) {
                        mHeader = View.inflate(activity, R.layout.view_drawer_header_without_plus_one, null);
                    }
                    mPlusOneButton = (PlusOneButton) mHeader.findViewById(R.id.plus_one_button);
                    mLikeButton = (LikeView) mHeader.findViewById(R.id.facebook_button);


                    listView.addHeaderView(mHeader, null, false);

                    View footer = View.inflate(activity, R.layout.view_drawer_footer, null);
                    ((TextView) footer.findViewById(android.R.id.text1)).setText(activity.getString(R.string.version) + ": " + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
                    listView.addFooterView(footer, null, false);

                    mCurrentMenuItems.clear();
                    mCurrentMenuItems.add(MenuItem.THEMES);
                    if (mAdapter == null) {
                        mAdapter = new ArrayAdapter<MenuItem>(activity,
                                R.layout.adapter_left_menu,
                                android.R.id.text1, mCurrentMenuItems) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                ((ImageView) view.findViewById(R.id.icon)).setImageResource(mCurrentMenuItems.get(position).iconDrawable);
                                return view;
                            }
                        };
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                view.setTag(mConfig);
                                mCurrentMenuItems.get(position - 1).clickListener.onClick(view);
                            }
                        });
                        listView.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }

                    Core.ExecuteOperationBuilder<ConfigProcessor.Config> operationBuilder = new Core.ExecuteOperationBuilder<>();
                    DataSourceRequest pDataSourceRequest = new DataSourceRequest("http://wrt-phone.appspot.com/config");
                    pDataSourceRequest.setCacheable(true);
                    pDataSourceRequest.setCacheExpiration(DateUtils.DAY_IN_MILLIS);
                    operationBuilder
                            .setActivity(activity)
                            .setDataSourceKey(HttpAndroidDataSource.SYSTEM_SERVICE_KEY)
                            .setProcessorKey(ConfigProcessor.APP_SERVICE_KEY)
                            .setDataSourceRequest(pDataSourceRequest)
                            .setSuccess(new ISuccess<ConfigProcessor.Config>() {
                                @Override
                                public void success(ConfigProcessor.Config config) {
                                    if (mConfig == null) {
                                        mConfig = config;
                                    }
                                    updateAdapter(config);
                                }

                            }).setDataSourceServiceListener(new Core.SimpleDataSourceServiceListener() {
                        @Override
                        public void onDone(Bundle resultData) {

                        }

                        @Override
                        public void onCached(Bundle resultData) {
                            super.onCached(resultData);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final ConfigProcessor.Config config = ConfigProcessor.getFromCache();
                                    mConfig = config;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateAdapter(config);
                                        }
                                    });
                                }
                            }).start();
                        }
                    });
                    Core.get(activity).execute(operationBuilder.build());
                }
                initHeaderView(data, activity);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }

    private void updateAdapter(ConfigProcessor.Config config) {
        if (config == null) {
            return;
        }
        String fbLikeUrl = config.getFbLikeUrl();
        if (!StringUtil.isEmpty(fbLikeUrl) && isNotLiked()) {
            mLikeButton.setVisibility(View.VISIBLE);
            mLikeButton.setObjectIdAndType(fbLikeUrl, LikeView.ObjectType.PAGE);
            ((ViewGroup)mLikeButton.getChildAt(0)).getChildAt(0).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ITracker.Impl.get(ContextHolder.get()).track("like:tap");
                    PreferenceHelper.set(LIKED_TIME_KEY, System.currentTimeMillis());
                    return false;
                }
            });
        } else {
            mLikeButton.setVisibility(View.INVISIBLE);
        }
        String plusOneUrl = config.getPlusOneUrl();
        if (!StringUtil.isEmpty(plusOneUrl) && isNotPlusOne()) {
            if (mPlusOneButton != null) {
                mPlusOneButton.setVisibility(View.VISIBLE);
                mPlusOneButton.initialize(plusOneUrl, MainActivity.REQUEST_CODE_PLUS);
                mPlusOneButton.setOnPlusOneClickListener(new PlusOneButton.OnPlusOneClickListener() {
                    @Override
                    public void onPlusOneClick(Intent intent) {
                        ITracker.Impl.get(ContextHolder.get()).track("plusone:tap");
                        PreferenceHelper.set(PLUS_ONE_TIME_KEY, System.currentTimeMillis());
                    }
                });
            }
        } else {
            if (mPlusOneButton != null) {
                mPlusOneButton.setVisibility(View.INVISIBLE);
            }
        }

        mCurrentMenuItems.clear();
        mCurrentMenuItems.add(MenuItem.THEMES);
        if (!StringUtil.isEmpty(config.getShareUrl())) {
            mCurrentMenuItems.add(MenuItem.SHARE);
        }
        List<ConfigProcessor.Config.Group> groups = config.getGroups();
        if (groups != null && !groups.isEmpty()) {
            mCurrentMenuItems.add(MenuItem.JOIN_GROUP);
        }
        if (!StringUtil.isEmpty(config.getRateAppUrl())) {
            mCurrentMenuItems.add(MenuItem.RATE);
        }
        if (!StringUtil.isEmpty(config.getProUrl())) {
            mCurrentMenuItems.add(MenuItem.PLAY_VERSION);
        }
        if (!StringUtil.isEmpty(config.getGithubUrl())) {
            mCurrentMenuItems.add(MenuItem.OPEN_SOURCE);
        }
        if (!StringUtil.isEmpty(config.getAboutAppUrl())) {
            mCurrentMenuItems.add(MenuItem.ABOUT);
        }
        mAdapter.notifyDataSetChanged();
    }

    private boolean isNotLiked() {
        long savedTime = PreferenceHelper.getLong(LIKED_TIME_KEY, 0l);
        return savedTime == 0l || System.currentTimeMillis() - savedTime < DELAY_FOR_HIDE_TIME;
    }

    private boolean isNotPlusOne() {
        long savedTime = PreferenceHelper.getLong(PLUS_ONE_TIME_KEY, 0l);
        return savedTime == 0l || System.currentTimeMillis() - savedTime < DELAY_FOR_HIDE_TIME;
    }

    private void initHeaderView(Cursor data, FragmentActivity activity) {
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
            String id = null;
            do {
                if (id == null) {
                    id = CursorUtils.getString(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID, data);
                }
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
            if (!StringUtil.isEmpty(id)) {
                /*
                TODO find solution later. Doesn't work now.
                final Intent currentProfileEditIntent = new Intent(Intent.ACTION_EDIT);
                Uri contactUri = ContentUris.withAppendedId(ContactsContract.Profile.CONTENT_URI, Long.valueOf(id));
                currentProfileEditIntent.setDataAndType(contactUri, ContactsContract.RawContacts.CONTENT_ITEM_TYPE);
                mHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.getContext().startActivity(currentProfileEditIntent);
                    }
                });
                */
            }
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
                    nameView.setText(Html.fromHtml("<b>" + name + "</b><br>" + phoneNumber), TextView.BufferType.SPANNABLE);
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

    public void refreshButtons() {
        if (mConfig != null) {
            updateAdapter(mConfig);
        }
    }
}
