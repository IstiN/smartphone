package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.graphics.drawable.Drawable;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.Application;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.SearchFragment;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.utils.ColorUtils;

/**
 * Created by IstiN on 31.01.2015.
 */
public class SearchAdapter extends CursorModelAdapter<RecyclerView.ViewHolder, SearchFragment.SearchCursorModel> {

    public static final int VIEW_TYPE_CONTACT = 1;
    public static final int VIEW_TYPE_NUMBER = 0;

    private Drawable mStarDrawable;

    private Drawable mStarOutlineDrawable;

    public SearchAdapter(SearchFragment.SearchCursorModel model) {
        super(model);
        mStarDrawable = ContextHolder.get().getResources().getDrawable(R.drawable.ic_star);
        mStarOutlineDrawable = ContextHolder.get().getResources().getDrawable(R.drawable.ic_star_outline);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        private TextView mTextView;

        private TextView mPhoneTextView;

        private View mClickableView;

        private TextView mCharacterView;

        private ImageView mImageViewStar;

        private TextView mTypeView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mTextView = (TextView) itemView.findViewById(R.id.name);
            mPhoneTextView = (TextView) itemView.findViewById(R.id.phone);
            mClickableView = itemView.findViewById(R.id.clickableView);
            mCharacterView = (TextView) itemView.findViewById(R.id.character);
            mImageViewStar = (ImageView) itemView.findViewById(R.id.star);
            mTypeView = (TextView) itemView.findViewById(R.id.phone_type);
        }

    }

    public static class NumberHolder extends RecyclerView.ViewHolder {

        private TextView mPhoneView;

        private TextView mTypeView;

        private View mClickableView;

        public NumberHolder(View itemView) {
            super(itemView);
            mPhoneView = (TextView) itemView.findViewById(R.id.phone);
            mTypeView = (TextView) itemView.findViewById(R.id.phone_type);
            mClickableView = itemView.findViewById(R.id.clickableView);
        }

    }

    @Override
    public int getItemViewType(int position) {
        SearchFragment.SearchCursorModel modelByPosition = getModelByPosition(position);
        return getViewType(modelByPosition, position);
    }

    public int getViewType(SearchFragment.SearchCursorModel modelByPosition, int position) {
        if (position == 0) {
            return VIEW_TYPE_CONTACT;
        }
        Long id = modelByPosition.getLong(BaseColumns._ID);
        Long prevId = getModelByPosition(position - 1).getLong(BaseColumns._ID);
        if (id.equals(prevId)) {
            return VIEW_TYPE_NUMBER;
        }
        return VIEW_TYPE_CONTACT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CONTACT) {
            View itemView = View.inflate(parent.getContext(), R.layout.adapter_search_contact, null);
            return new Holder(itemView);
        } else if (viewType == VIEW_TYPE_NUMBER) {
            View itemView = View.inflate(parent.getContext(), R.layout.adapter_search_contact_phone, null);
            return new NumberHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SearchFragment.SearchCursorModel modelByPosition = getModelByPosition(position);
        int viewType = getViewType(modelByPosition, position);
        modelByPosition = getModelByPosition(position);
        if (viewType == VIEW_TYPE_CONTACT) {
            initItem((Holder) holder, modelByPosition, getPicasso());
        } else if (viewType == VIEW_TYPE_NUMBER) {
            NumberHolder numberHolder = (NumberHolder) holder;
            String number = modelByPosition.getString(ContactsContract.CommonDataKinds.Phone.NUMBER);
            numberHolder.mClickableView.setTag(StringUtil.isEmpty(number) ? modelByPosition.getLong(ContactsContract.CommonDataKinds.Phone._ID) : number);
            numberHolder.mPhoneView.setText(number);
            setType(modelByPosition, number, numberHolder.mTypeView);
        }
    }

    private void setType(SearchFragment.SearchCursorModel modelByPosition, String number, TextView typeTextView) {
        if (StringUtil.isEmpty(number)) {
            typeTextView.setVisibility(View.INVISIBLE);
            return;
        }
        typeTextView.setVisibility(View.VISIBLE);
        Integer type = modelByPosition.getInt(ContactsContract.CommonDataKinds.Phone.TYPE);
        CharSequence phoneTypeLabel = ContactHelper.get(ContextHolder.get()).getPhoneTypeLabel(type);
        typeTextView.setText(phoneTypeLabel);
    }

    public void initItem(Holder holder, SearchFragment.SearchCursorModel cursorModel, Picasso picasso) {
        if (cursorModel == null) {
            return;
        }

        Long id = cursorModel.getLong(ContactsContract.Contacts._ID);
        String name = cursorModel.getString(ContactsContract.Contacts.DISPLAY_NAME);
        String photoUri = cursorModel.getString(ContactsContract.Contacts.PHOTO_URI);
        String number = cursorModel.getString(ContactsContract.CommonDataKinds.Phone.NUMBER);
        holder.mTextView.setText(name);
        holder.mPhoneTextView.setText(number);
        holder.mClickableView.setTag(StringUtil.isEmpty(number) ? cursorModel.getLong(ContactsContract.CommonDataKinds.Phone._ID) : number);
        holder.mImageView.setTag(id);
        Integer isStarred = cursorModel.getInt(ContactsContract.Contacts.STARRED);
        ImageView mImageViewStar = holder.mImageViewStar;
        mImageViewStar.setImageDrawable(isStarred > 0 ? mStarDrawable : mStarOutlineDrawable);
        mImageViewStar.setTag(isStarred + "==" + id);

        setType(cursorModel, number, holder.mTypeView);
        if (photoUri == null) {
            holder.mCharacterView.setText(name == null ? "?" : String.valueOf(Character.toUpperCase(name.charAt(0))));
            UiUtil.setBackground(holder.mImageView, ColorUtils.getColorCircle(holder.mImageView.getHeight(), number == null ? name : number));
        } else {
            holder.mCharacterView.setText(StringUtil.EMPTY);
            UiUtil.setBackground(holder.mImageView, null);
        }
        picasso.load(photoUri).transform(Application.ROUNDED_TRANSFORMATION).into(holder.mImageView);
    }


}
