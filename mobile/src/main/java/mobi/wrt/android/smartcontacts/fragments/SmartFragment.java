package mobi.wrt.android.smartcontacts.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;


import com.squareup.picasso.Picasso;

import by.istin.android.xcore.fragment.collection.RecyclerViewFragment;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.CursorUtils;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.adapter.RecentAdapter;
import mobi.wrt.android.smartcontacts.fragments.adapter.SmartAdapter;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 31.01.2015.
 */
public class SmartFragment extends RecyclerViewFragment<SmartAdapter.Holder, SmartAdapter, SmartFragment.SmartModel>  {

    public static class SmartModel extends CursorModel{

        private CursorModel mLastCall;

        public SmartModel(Cursor cursor) {
            super(cursor);
        }

        public SmartModel(Cursor cursor, boolean isMoveToFirst) {
            super(cursor, isMoveToFirst);
        }

        @Override
        public void doInBackground(Context context) {
            super.doInBackground(context);
            Cursor lastCall = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, RecentFragment.PROJECTION, null, null, RecentFragment.ORDER + " LIMIT 1");
            if (lastCall != null && lastCall.moveToFirst()) {
                mLastCall = new RecentFragment.RecentModel(lastCall);
                mLastCall.doInBackground(context);
                mLastCall.moveToLast();
            }
        }

        @Override
        public void close() {
            super.close();
            CursorUtils.close(mLastCall);
        }
    }

    public static final String[] PROJECTION = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_URI};
    public static final String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1 AND " + ContactsContract.Contacts.STARRED + "=1";
    public static final String ORDER = null;

    @Override
    public CursorModel.CursorModelCreator<SmartModel> getCursorModelCreator() {
        return new CursorModel.CursorModelCreator<SmartModel>() {
            @Override
            public SmartModel create(Cursor cursor) {
                return new SmartModel(cursor);
            }
        };
    }

    @Override
    public int getViewLayout() {
        return R.layout.fragment_smart;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public SmartAdapter createAdapter(FragmentActivity fragmentActivity, SmartModel cursor) {
        IFloatHeader firstResponderFor = findFirstResponderFor(IFloatHeader.class);
        return new SmartAdapter(cursor, firstResponderFor.attach(null, getCollectionView()), firstResponderFor);
    }

    private int count;

    @Override
    public void onLoadFinished(Loader<SmartModel> loader, SmartModel cursor) {
        this.count = cursor.getCount();
        final View recentCallView = getActivity().findViewById(R.id.recent_call);
        RecentAdapter.initItem(new RecentAdapter.Holder(recentCallView), cursor.mLastCall, Picasso.with(recentCallView.getContext()));
        super.onLoadFinished(loader, cursor);
    }

    @Override
    public void swap(SmartAdapter smartAdapter, SmartModel cursor) {
        getAdapter().swap(cursor);
    }

    @Override
    public String getSelection() {
        return SELECTION;
    }

    @Override
    public String getOrder() {
        return ORDER;
    }

    @Override
    public Uri getUri() {
        return ContactsContract.Contacts.CONTENT_URI;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position < count) {
                    return 1;
                }
                return 2;
            }
        });
        return gridLayoutManager;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getProcessorKey() {
        return null;
    }

}
