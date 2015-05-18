package mobi.wrt.android.smartcontacts.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import by.istin.android.xcore.fragment.collection.RecyclerViewFragment;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.CursorUtils;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.adapter.ContactsAdapter;
import mobi.wrt.android.smartcontacts.fragments.adapter.RecentAdapter;
import mobi.wrt.android.smartcontacts.fragments.adapter.SmartAdapter;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 31.01.2015.
 */
public class ContactsFragment extends RecyclerViewFragment<ContactsAdapter.Holder, ContactsAdapter, ContactsFragment.ContactsModel> {

    public static class ContactsModel extends CursorModel {

        public ContactsModel(Cursor cursor) {
            super(cursor);
        }

        public ContactsModel(Cursor cursor, boolean isMoveToFirst) {
            super(cursor, isMoveToFirst);
        }

        @Override
        public void doInBackground(Context context) {
            super.doInBackground(context);
        }

        @Override
        public void close() {
            super.close();
        }
    }

    public static final String[] PROJECTION = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_URI, ContactsContract.Contacts.STARRED};
    public static final String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1";
    public static final String ORDER = ContactsContract.Contacts.STARRED + " desc, " + ContactsContract.Contacts.DISPLAY_NAME + " asc";


    @Override
    public CursorModel.CursorModelCreator<ContactsModel> getCursorModelCreator() {
        return new CursorModel.CursorModelCreator<ContactsModel>() {
            @Override
            public ContactsModel create(Cursor cursor) {
                return new ContactsModel(cursor);
            }
        };
    }

    @Override
    public int getViewLayout() {
        return R.layout.fragment_contacts;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public ContactsAdapter createAdapter(FragmentActivity fragmentActivity, ContactsModel cursor) {
        IFloatHeader firstResponderFor = findFirstResponderFor(IFloatHeader.class);
        final View scrollCharacterCard = getView().findViewById(R.id.scrollCharacterCard);
        final TextView scrollCharacter = (TextView) getView().findViewById(R.id.scrollCharacter);
        firstResponderFor.attach(new RecyclerView.OnScrollListener() {

            private Runnable hideRunnable = new Runnable() {

                @Override
                public void run() {
                    scrollCharacterCard.setVisibility(View.GONE);
                }

            };

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    ContactsAdapter adapter = getAdapter();
                    int pos = firstVisibleItemPosition;
                    if (adapter.getItemCount() > firstVisibleItemPosition + 1) {
                        pos = firstVisibleItemPosition + 1;
                    }
                    ContactsModel modelByPosition = adapter.getModelByPosition(pos);
                    if (!CursorUtils.isEmpty(modelByPosition) && modelByPosition.getInt(ContactsContract.Contacts.STARRED) == 0) {
                        scrollCharacterCard.removeCallbacks(hideRunnable);
                        scrollCharacterCard.setVisibility(View.VISIBLE);
                        scrollCharacterCard.postDelayed(hideRunnable, 1000l);
                        scrollCharacter.setText(String.valueOf(Character.toUpperCase(modelByPosition.getString(ContactsContract.Contacts.DISPLAY_NAME).charAt(0))));
                    }
                }
            }
        }, getCollectionView());
        return new ContactsAdapter(cursor);
    }

    @Override
    public void swap(ContactsAdapter smartAdapter, ContactsModel cursor) {
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
    public String getUrl() {
        return null;
    }

    @Override
    public String getProcessorKey() {
        return null;
    }

}
