package mobi.wrt.android.smartcontacts.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import by.istin.android.xcore.fragment.CursorLoaderFragmentHelper;
import by.istin.android.xcore.fragment.collection.RecyclerViewFragment;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.CursorUtils;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.adapter.RecentAdapter;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;
import mobi.wrt.android.smartcontacts.utils.HumanTimeUtil;

/**
 * Created by IstiN on 31.01.2015.
 */
public class RecentFragment extends RecyclerViewFragment<RecyclerView.ViewHolder, RecentAdapter, RecentFragment.RecentModel> {

    public static final String ORDER = CallLog.Calls.DATE + " DESC";
    private static final int NUMBER = 1;
    private static final int NAME_POSITION = 2;
    private static final int DATE_POSITION = 3;
    public static final String[] PROJECTION = new String[]{CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.DATE, CallLog.Calls.CACHED_NUMBER_TYPE, CallLog.Calls.TYPE};
    public static final int MAX_COUNT = 15;
    //We will group by number in the Cursor model so we need to get more data as possible to create MAX_COUNT of items.
    public static final int LIMIT = MAX_COUNT * 2;

    public static final String EXTRA_IS_LIMIT = "is_limit";

    public boolean isLimit() {
        if (getArguments() == null) {
            return true;
        }
        return getArguments().getBoolean(EXTRA_IS_LIMIT, true);
    }

    public static class RecentModel extends CursorModel {

        public static interface ICallsCallback {

            void onCallAdd(byte[] callsLog, String cachedName, String phoneNumber);
        }

        private boolean isLimit = true;

        public RecentModel(Cursor cursor) {
            super(cursor);
        }

        private ICallsCallback mCallsCallback;

        public RecentModel(Cursor cursor, ICallsCallback callsCallback) {
            super(cursor);
            mCallsCallback = callsCallback;
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
                        if (isLimit && matrixCursor.getCount() == MAX_COUNT) {
                            break;
                        }
                        lastPhoneNumber = phoneNumber;
                        objects = new Object[PROJECTION.length];
                        //last item will be byte array
                        for (int j = 0; j < PROJECTION.length - 1; j++) {
                            if (j == DATE_POSITION) {
                                objects[j] = model.getLong(PROJECTION[j]);
                            } else {
                                objects[j] = model.getString(PROJECTION[j]);
                            }
                        }
                        callTypes.add(callType);
                        contactHelper.initPhotoAndContactIdUri(phoneNumber);
                    } else {
                        callTypes.add(callType);
                    }
                }
                if (objects != null) {
                    addRow(matrixCursor, callTypes, objects);
                }
                setCursor(matrixCursor);
                moveToFirst();
            }
            mLastHeader = null;
        }

        public void addRow(MatrixCursor matrixCursor, List<Byte> callTypes, Object[] objects) {
            byte[] callsLog  = new byte[callTypes.size()];
            for (int i = 0; i < callTypes.size(); i++) {
                callsLog[i] = callTypes.get(i);
            }
            objects[PROJECTION.length - 1] = callsLog;

            Object dateObject = objects[DATE_POSITION];
            Long date = (Long) dateObject;
            objects[DATE_POSITION] = HumanTimeUtil.humanFriendlyDate(date);
            addGroupHeaderIfNeed(matrixCursor, date);
            matrixCursor.addRow(objects);
            matrixCursor.moveToLast();
            if (mCallsCallback != null) {
                mCallsCallback.onCallAdd(callsLog, CursorUtils.getString(CallLog.Calls.CACHED_NAME, matrixCursor),
                        CursorUtils.getString(CallLog.Calls.NUMBER, matrixCursor)
                        );
            }
            callTypes.clear();
        }

        private String mLastHeader = null;

        public void addGroupHeaderIfNeed(MatrixCursor matrixCursor, Long date) {
            if (mCallsCallback != null) {
                return;
            }
            String newHeader = HumanTimeUtil.humanFriendlyDateHeader(date);
            if (mLastHeader == null || !mLastHeader.equals(newHeader)) {
                mLastHeader = newHeader;
                addHeader(matrixCursor);
            }

        }

        public void addHeader(MatrixCursor matrixCursor) {
            Object[] groupHeader = new Object[PROJECTION.length];
            groupHeader[NAME_POSITION] = mLastHeader;
            matrixCursor.addRow(groupHeader);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        RecentAdapter adapter = getAdapter();
        if (adapter != null && adapter.getItemCount() > 0) {
            CursorLoaderFragmentHelper.restartLoader(this);
        }
    }

    @Override
    public CursorModel.CursorModelCreator<RecentModel> getCursorModelCreator() {
        return new CursorModel.CursorModelCreator<RecentModel>() {
            @Override
            public RecentModel create(Cursor cursor) {
                RecentModel recentModel = new RecentModel(cursor);
                recentModel.isLimit = isLimit();
                return recentModel;
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
    public RecentAdapter createAdapter(FragmentActivity fragmentActivity, RecentModel cursor) {
        IFloatHeader floatHeader = findFirstResponderFor(IFloatHeader.class);
        floatHeader.attach(null, getCollectionView());
        return new RecentAdapter(cursor, isLimit());
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
        return " " + ORDER + (isLimit() ? (" LIMIT " + LIMIT) : "");
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
