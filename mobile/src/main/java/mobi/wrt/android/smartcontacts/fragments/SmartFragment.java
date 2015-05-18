package mobi.wrt.android.smartcontacts.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import by.istin.android.xcore.fragment.CursorLoaderFragmentHelper;
import by.istin.android.xcore.fragment.collection.RecyclerViewFragment;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.CursorUtils;
import by.istin.android.xcore.utils.StringUtil;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.adapter.RecentAdapter;
import mobi.wrt.android.smartcontacts.fragments.adapter.SmartAdapter;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 31.01.2015.
 */
public class SmartFragment extends RecyclerViewFragment<RecyclerView.ViewHolder, SmartAdapter, SmartFragment.SmartModel>  {

    public static class SmartModel extends CursorModel{

        private CursorModel mLastCall;

        public SmartModel(Cursor cursor) {
            super(cursor);
        }

        public SmartModel(Cursor cursor, boolean isMoveToFirst) {
            super(cursor, isMoveToFirst);
        }

        @Override
        public void doInBackground(final Context context) {
            super.doInBackground(context);
            Cursor lastCall = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, RecentFragment.PROJECTION, null, null, RecentFragment.ORDER + " LIMIT " + RecentFragment.LIMIT);
            if (lastCall != null && !CursorUtils.isEmpty(lastCall) && lastCall.moveToFirst()) {
                final List<ContentValues> sortedStarredList = new ArrayList<>();
                final List<ContentValues> sortedOtherList = new ArrayList<>();
                final List<ContentValues> starredList = new ArrayList<>();
                CursorUtils.convertToContentValues(this, starredList, CursorUtils.Converter.get());
                CursorModel lastCallModel = new RecentFragment.RecentModel(lastCall, new RecentFragment.RecentModel.ICallsCallback() {
                    @Override
                    public void onCallAdd(byte[] callsLog, String cachedName, String phone) {
                        if (callsLog == null) {
                            return;
                        }
                        Long contactId = ContactHelper.get(context).getContactId(phone);
                        if (contactId == null) {
                            return;
                        }
                        ListIterator<ContentValues> contentValuesListIterator = starredList.listIterator();
                        boolean isStarredContact = false;
                        while (contentValuesListIterator.hasNext()) {
                            ContentValues contentValues = contentValuesListIterator.next();
                            Long id = contentValues.getAsLong(ContactsContract.Contacts._ID);
                            if (id != null && id.equals(contactId)) {
                                Integer oldCount = contentValues.getAsInteger(BaseColumns._COUNT);
                                if (oldCount == null) {
                                    oldCount = 0;
                                }
                                contentValues.put(BaseColumns._COUNT, oldCount + callsLog.length);
                                addToSortedList(contentValues, sortedStarredList);
                                isStarredContact = true;
                                break;
                            }
                        }
                        if (!isStarredContact) {
                            ListIterator<ContentValues> sortedOtherListIterator = sortedOtherList.listIterator();
                            boolean isFound = false;
                            while (sortedOtherListIterator.hasNext()) {
                                ContentValues contentValues = sortedOtherListIterator.next();
                                if (contactId.equals(contentValues.getAsLong(ContactsContract.Contacts._ID))) {
                                    contentValues.put(BaseColumns._COUNT, contentValues.getAsInteger(BaseColumns._COUNT) + callsLog.length);
                                    sortedOtherList.remove(contentValues);
                                    addToSortedList(contentValues, sortedOtherList);
                                    isFound = true;
                                    break;
                                }
                            }
                            if (!isFound) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(ContactsContract.Contacts._ID, contactId);
                                contentValues.put(ContactsContract.Contacts.DISPLAY_NAME, cachedName);
                                String value = ContactHelper.get(context).initPhotoAndContactIdUri(phone);
                                contentValues.put(ContactsContract.Contacts.PHOTO_URI, value.equals(StringUtil.EMPTY) ? null : value);
                                contentValues.put(BaseColumns._COUNT, callsLog.length);
                                addToSortedList(contentValues, sortedOtherList);
                            }
                        }
                    }

                    private void addToSortedList(ContentValues contentValues, List<ContentValues> targetList) {
                        targetList.remove(contentValues);
                        if (targetList.isEmpty()) {
                            targetList.add(contentValues);
                        } else {
                            int size = targetList.size();
                            boolean isFound = false;
                            for (int j = 0; j < size; j++) {
                                ContentValues existingContentValues = targetList.get(j);
                                if (existingContentValues.getAsInteger(BaseColumns._COUNT) < contentValues.getAsInteger(BaseColumns._COUNT)) {
                                    targetList.add(j, contentValues);
                                    isFound = true;
                                    break;
                                }
                            }
                            if (!isFound) {
                                targetList.add(contentValues);
                            }
                        }
                    }
                });
                lastCallModel.doInBackground(context);
                lastCallModel.moveToFirst();
                for (ContentValues contentValues : starredList) {
                    if (!sortedStarredList.contains(contentValues)) {
                        sortedStarredList.add(contentValues);
                    }
                }
                sortedStarredList.addAll(sortedOtherList);
                setCursor(CursorUtils.listContentValuesToCursor(sortedStarredList, PROJECTION));
                mLastCall = lastCallModel;
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
        firstResponderFor.attach(null, getCollectionView());
        return new SmartAdapter(cursor);
    }

    private int count;

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        getCollectionView().addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.left = 1;
                outRect.right = 1;
                outRect.bottom = 1;

                // Add top margin only for the first item to avoid double space between items
                int childAdapterPosition = parent.getChildAdapterPosition(view);
                if (childAdapterPosition == 0 || childAdapterPosition == 1) {
                    outRect.top = 1;
                }
            }

        });
    }

    @Override
    public void onLoadFinished(Loader<SmartModel> loader, SmartModel cursor) {
        if (cursor == null) {
            this.count = 0;
        } else {
            this.count = cursor.getCount();
        }
        final View recentCallView = getActivity().findViewById(R.id.recent_call);
        if (cursor != null && !CursorUtils.isEmpty(cursor.mLastCall)) {
            cursor.mLastCall.moveToFirst();
            RecentAdapter.initItem(new RecentAdapter.Holder(recentCallView), cursor.mLastCall, Picasso.with(recentCallView.getContext()), new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    ViewGroup itemView = (ViewGroup) v.getParent();
                    final Long id = (Long) itemView.getTag();
                    Context context = itemView.getContext();
                    ContactHelper.get(context).removeCallLog(context, id, new Runnable() {
                        @Override
                        public void run() {
                            CursorLoaderFragmentHelper.restartLoader(SmartFragment.this);
                        }
                    });
                    return true;
                }
            });
            recentCallView.setVisibility(View.VISIBLE);
        } else {
            recentCallView.setVisibility(View.GONE);
        }
        super.onLoadFinished(loader, cursor);
        View view = getView();
        if (view != null) {
            if (count == 0) {
                view.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(android.R.id.empty).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SmartAdapter adapter = getAdapter();
        if (adapter != null && adapter.getItemCount() > 0) {
            CursorLoaderFragmentHelper.restartLoader(this);
        }
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
                return 1;
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
