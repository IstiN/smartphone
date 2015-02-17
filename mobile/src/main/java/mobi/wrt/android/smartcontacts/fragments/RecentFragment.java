package mobi.wrt.android.smartcontacts.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import by.istin.android.xcore.fragment.collection.RecyclerViewFragment;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.Log;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.adapter.RecentAdapter;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 31.01.2015.
 */
public class RecentFragment extends RecyclerViewFragment<RecentAdapter.Holder, RecentAdapter, RecentFragment.RecentModel> {

    public static final String ORDER = CallLog.Calls.DATE + " DESC";
    public static final String[] PROJECTION = new String[]{CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.DATE, CallLog.Calls.CACHED_NUMBER_TYPE, CallLog.Calls.TYPE};
    public static final int MAX_COUNT = 15;
    //We will group by number in the Cursor model so we need to get more data as possible to create MAX_COUNT of items.
    public static final int LIMIT = MAX_COUNT * 2;

    public static class RecentModel extends CursorModel {

        public RecentModel(Cursor cursor) {
            super(cursor);
        }

        public RecentModel(Cursor cursor, boolean isMoveToFirst) {
            super(cursor, isMoveToFirst);
        }

        @Override
        public void doInBackground(Context context) {
            super.doInBackground(context);
            if (!isEmpty()) {
                ContactHelper contactHelper = ContactHelper.get(context);
                String lastPhoneNumber = null;
                MatrixCursor matrixCursor = new MatrixCursor(PROJECTION);
                List<Byte> callTypes = new ArrayList<>();
                Object[] objects = null;
                for (int i = 0; i < size(); i++) {
                    CursorModel model = get(i);
                    String phoneNumber = model.getString(CallLog.Calls.NUMBER);
                    byte callType = model.getByte(CallLog.Calls.TYPE);
                    if (lastPhoneNumber == null || !phoneNumber.equals(lastPhoneNumber)) {
                        if (lastPhoneNumber != null) {
                            addRow(matrixCursor, callTypes, objects);
                            objects = null;
                        }
                        if (matrixCursor.getCount() == MAX_COUNT) {
                            break;
                        }
                        lastPhoneNumber = phoneNumber;
                        objects = new Object[PROJECTION.length];
                        //last item will be byte array
                        for (int j = 0; j < PROJECTION.length - 1; j++) {
                            objects[j] = model.getString(PROJECTION[j]);
                        }
                        callTypes.add(callType);
                        contactHelper.initPhotoUri(phoneNumber);
                    } else {
                        callTypes.add(callType);
                    }
                    /*Integer numberType = model.getInt(CallLog.Calls.CACHED_NUMBER_TYPE);
                    if (numberType != null) {
                        switch (numberType) {
                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                        }
                    }*/
                }
                if (objects != null) {
                    addRow(matrixCursor, callTypes, objects);
                }
                setCursor(matrixCursor);
                moveToFirst();
            }
        }

        public void addRow(MatrixCursor matrixCursor, List<Byte> callTypes, Object[] objects) {
            byte[] callsLog  = new byte[callTypes.size()];
            for (int i = 0; i < callTypes.size(); i++) {
                callsLog[i] = callTypes.get(i);
            }
            objects[PROJECTION.length - 1] = callsLog;
            matrixCursor.addRow(objects);
            callTypes.clear();
        }

    }


    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
    }

    @Override
    public CursorModel.CursorModelCreator<RecentModel> getCursorModelCreator() {
        return new CursorModel.CursorModelCreator<RecentModel>() {
            @Override
            public RecentModel create(Cursor cursor) {
                return new RecentModel(cursor);
            }
        };
    }

    @Override
    public int getViewLayout() {
        return R.layout.fragment_recent;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public Loader<RecentModel> onCreateLoader(int id, Bundle args) {
        Log.startAction(getClass().getName());
        return super.onCreateLoader(id, args);
    }

    @Override
    public RecentAdapter createAdapter(FragmentActivity fragmentActivity, RecentModel cursor) {
        Log.endAction(getClass().getName());
        IFloatHeader floatHeader = findFirstResponderFor(IFloatHeader.class);
        return new RecentAdapter(cursor, floatHeader.attach(getCollectionView()), floatHeader);
    }

    @Override
    public void swap(RecentAdapter recentAdapter, RecentModel cursor) {
        getAdapter().swap(cursor);
    }

    @Override
    public String getSelection() {
        //return CallLog.Calls._ID + " in (SELECT " + CallLog.Calls._ID + " FROM calls WHERE type != " + CallLog.Calls.VOICEMAIL_TYPE + " GROUP BY " + CallLog.Calls.NUMBER + " ORDER BY date DESC LIMIT 15)";
        return null;
    }

    @Override
    public String getOrder() {
        return " " + ORDER + " LIMIT " + LIMIT;
    }

    @Override
    public Uri getUri() {
        return CallLog.Calls.CONTENT_URI;
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
