package mobi.wrt.android.smartcontacts.fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.fragment.CursorLoaderFragmentHelper;
import by.istin.android.xcore.fragment.collection.RecyclerViewFragment;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.ContentUtils;
import by.istin.android.xcore.utils.CursorUtils;
import by.istin.android.xcore.utils.Log;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.anim.SimpleAnimationListener;
import mobi.wrt.android.smartcontacts.fragments.adapter.SearchAdapter;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.view.DrawerArrowDrawable;

public class SearchFragment extends RecyclerViewFragment<RecyclerView.ViewHolder, SearchAdapter, SearchFragment.SearchCursorModel> {

    public static final CursorModel.CursorModelCreator<SearchCursorModel> CURSOR_MODEL_CREATOR = new CursorModel.CursorModelCreator<SearchCursorModel>() {
        @Override
        public SearchCursorModel create(Cursor cursor) {
            return new SearchCursorModel(cursor);
        }
    };

    @Override
    public int getViewLayout() {
        return R.layout.fragment_search;
    }

    private View mShadowView;

    private EditText mEditText;

    private DrawerArrowDrawable mArrowDrawable;

    private String mSearchQuery;

    private TextWatcher mWatcher = new TextWatcher() {

        private Handler mHandler = new Handler();

        private Runnable mSearchRunnable = new Runnable() {
            @Override
            public void run() {
                CursorLoaderFragmentHelper.restartLoader(SearchFragment.this);
            }
        };

        private View searchClear;

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void afterTextChanged(Editable s) {
            View view = getView();
            if (view == null) {
                return;
            }
            String value = s.toString();
            if (searchClear == null) {
                searchClear = view.findViewById(R.id.clear);
                searchClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEditText.setText(StringUtil.EMPTY);
                    }
                });
            }
            if (StringUtil.isEmpty(value)) {
                if (searchClear != null) {
                    searchClear.setVisibility(View.GONE);
                }
                SearchAdapter adapter = getAdapter();
                if (adapter != null) {
                    swap(adapter, null);
                }
            } else {
                if (searchClear != null) {
                    searchClear.setVisibility(View.VISIBLE);
                }
            }

            mSearchQuery = value;
            mHandler.removeCallbacks(mSearchRunnable);
            mHandler.postDelayed(mSearchRunnable, 500l);
        }

    };

    public static class SearchCursorModel extends CursorModel {

        public SearchCursorModel(Cursor cursor) {
            super(cursor);
        }

        @Override
        public void doInBackground(Context context) {
            super.doInBackground(context);
            ContactHelper contactHelper = ContactHelper.get(context);
            if (!CursorUtils.isEmpty(this)) {
                List<ContentValues> result = new ArrayList<>();
                for (int i = 0; i < size(); i++) {
                    moveToPosition(i);
                    ContentValues contentValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(this, contentValues);
                    Long id = getLong(BaseColumns._ID);
                    final List<ContentValues> entities = contactHelper.getPhonesById(id);
                    //TODO make facebook and skype chat visibility
                    final List<ContentValues> emails = contactHelper.getEmailsById(id);
                    if (emails != null) {
                        for (ContentValues email : emails) {
                            Log.xd(this, email);
                        }
                    }

                    if (entities != null && !entities.isEmpty()) {
                        int sizeOfPhones = entities.size();
                        for (int j = 0; j < sizeOfPhones; j++) {
                            ContentValues phone = entities.get(j);
                            Log.xd(this, phone.toString());
                            String number = phone.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            if (!StringUtil.isEmpty(number)) {
                                Log.xd(this, "number: " + number);
                                contactHelper.initPhotoUri(number);
                            }
                            contentValues.putAll(phone);
                            result.add(contentValues);
                            if (j != sizeOfPhones - 1) {
                                contentValues = new ContentValues();
                                DatabaseUtils.cursorRowToContentValues(this, contentValues);
                            }
                        }
                    } else {
                        result.add(contentValues);
                    }
                }

                setCursor(CursorUtils.listContentValuesToCursor(result,
                        ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_URI,
                        ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY, ContactsContract.CommonDataKinds.Phone.TYPE));
            }
        }
    }


    @Override
    public void onViewCreated(final View view) {
        super.onViewCreated(view);
        mEditText = (EditText) view.findViewById(R.id.search_edit);
        mShadowView = view.findViewById(R.id.shadow);
        mEditText.setVisibility(View.VISIBLE);
        mEditText.addTextChangedListener(mWatcher);
        mArrowDrawable = new DrawerArrowDrawable(getActivity(), getActivity());
        ImageView topButton = (ImageView) view.findViewById(R.id.arrow);
        topButton.setImageDrawable(mArrowDrawable);
        topButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearch();
            }
        });

        animate(0, 1, new SimpleAnimationListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                view.findViewById(R.id.search_input).setVisibility(View.INVISIBLE);
                mShadowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeSearch();
                    }
                });
            }

        });
        mEditText.requestFocus();
        mEditText.post(new Runnable() {
            @Override
            public void run() {
                UiUtil.showKeyboard(mEditText);
            }
        });
        getCollectionView().setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 1) {
                    UiUtil.hideKeyboard(mEditText);
                }
            }
        });

    }

    @Override
    public CursorModel.CursorModelCreator<SearchCursorModel> getCursorModelCreator() {
        return CURSOR_MODEL_CREATOR;
    }

    @Override
    public SearchAdapter createAdapter(FragmentActivity fragmentActivity, SearchCursorModel cursor) {
        return new SearchAdapter(cursor);
    }

    @Override
    public void swap(SearchAdapter searchAdapter, SearchCursorModel cursor) {
        searchAdapter.swap(cursor);
    }

    private void animate(int startValue, int endValue, Animator.AnimatorListener listener) {
        ValueAnimator anim = ValueAnimator.ofFloat(startValue, endValue);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                mArrowDrawable.setProgress(slideOffset);
                if (slideOffset < 0.57f) {
                    mShadowView.setAlpha(slideOffset);
                } else {
                    mShadowView.setAlpha(0.57f);
                }
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(300);
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    private boolean isCloseRunning = false;

    public void closeSearch() {
        if (isCloseRunning) {
            return;
        }
        isCloseRunning = true;
        animate(1, 0, new SimpleAnimationListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                FragmentActivity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.getSupportFragmentManager().popBackStackImmediate();
            }

        });
        UiUtil.hideKeyboard(mEditText);
    }

    @Override
    public Uri getUri() {
        if (StringUtil.isEmpty(mSearchQuery)) {
            return null;
        }
        return Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, StringUtil.encode(mSearchQuery));
    }

    public static final String[] PROJECTION = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_URI};

    @Override
    public void onLoadFinished(Loader<SearchCursorModel> loader, SearchCursorModel cursor) {
        if (!StringUtil.isEmpty(mSearchQuery)) {
            super.onLoadFinished(loader, cursor);
        }
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getOrder() {
        return ContactsContract.Contacts._ID + " ASC LIMIT 0, 10";
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
