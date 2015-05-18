package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.Application;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.ContactsFragment;
import mobi.wrt.android.smartcontacts.utils.ColorUtils;

/**
 * Created by IstiN on 31.01.2015.
 */
public class ContactsAdapter extends FloatHeaderAdapter<ContactsAdapter.Holder, ContactsFragment.ContactsModel> {

    private Drawable mStarDrawable;

    private Drawable mStarOutlineDrawable;

    public ContactsAdapter(ContactsFragment.ContactsModel cursors) {
        super(cursors);
        mStarDrawable = ContextHolder.get().getResources().getDrawable(R.drawable.ic_star);
        mStarOutlineDrawable = ContextHolder.get().getResources().getDrawable(R.drawable.ic_star_outline);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        private ImageView mImageViewStar;

        private TextView mTextView;

        private View mClickableView;

        private TextView mCharacterView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mImageViewStar = (ImageView) itemView.findViewById(R.id.star);
            mTextView = (TextView) itemView.findViewById(R.id.name);
            mClickableView = itemView.findViewById(R.id.clickableView);
            mCharacterView = (TextView) itemView.findViewById(R.id.character);
        }

    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(), R.layout.adapter_contact, null);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        ContactsFragment.ContactsModel cursorModel = getModelByPosition(position);
        String name = cursorModel.getString(ContactsContract.Contacts.DISPLAY_NAME);
        String photoUri = cursorModel.getString(ContactsContract.Contacts.PHOTO_URI);
        Integer isStarred = cursorModel.getInt(ContactsContract.Contacts.STARRED);

        ImageView mImageViewStar = holder.mImageViewStar;
        mImageViewStar.setImageDrawable(isStarred > 0 ? mStarDrawable : mStarOutlineDrawable);

        holder.mTextView.setText(name);

        Long id = cursorModel.getLong(ContactsContract.Contacts._ID);
        mImageViewStar.setTag(isStarred+"=="+id);
        holder.mClickableView.setTag(id);

        holder.mImageView.setTag(id);

        getPicasso().load(photoUri).into(holder.mImageView);

        if (StringUtil.isEmpty(photoUri)) {
            if (name == null) {
                holder.mCharacterView.setText(StringUtil.EMPTY);
                UiUtil.setBackground(holder.mImageView, ColorUtils.getColorCircle(holder.mImageView.getHeight(), StringUtil.EMPTY));
            } else {
                holder.mCharacterView.setText(String.valueOf(Character.toUpperCase(name.charAt(0))));
                UiUtil.setBackground(holder.mImageView, ColorUtils.getColorCircle(holder.mImageView.getHeight(), name));
            }
        } else {
            holder.mCharacterView.setText(StringUtil.EMPTY);
            UiUtil.setBackground(holder.mImageView, null);
            getPicasso().load(photoUri).transform(Application.ROUNDED_TRANSFORMATION).into(holder.mImageView);
        }
    }

}
