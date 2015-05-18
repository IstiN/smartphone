package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.SmartFragment;
import mobi.wrt.android.smartcontacts.utils.ColorUtils;

/**
 * Created by IstiN on 31.01.2015.
 */
public class SmartAdapter extends FloatHeaderAdapter<RecyclerView.ViewHolder, SmartFragment.SmartModel> {

    public static final int REGULAR_VIEW_TYPE = 0;
    public static final int FOOTER_VIEW_TYPE = 1;

    private int smartContactHeight;

    private int parentHeight;

    private Drawable mTextShadow;

    public SmartAdapter(SmartFragment.SmartModel cursors) {
        super(cursors);
        smartContactHeight = ContextHolder.get().getResources().getDimensionPixelSize(R.dimen.smart_contact_height);
        mTextShadow = ContextHolder.get().getResources().getDrawable(R.drawable.bg_smart_text);
    }

    public static class RegularHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        private TextView mTextView;

        private TextView mTextViewCharacter;

        private View mClickableView;

        private View mMoreView;

        public RegularHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mTextView = (TextView) itemView.findViewById(R.id.name);
            mTextViewCharacter = (TextView) itemView.findViewById(R.id.character);
            mClickableView = itemView.findViewById(R.id.clickableView);
            mMoreView = itemView.findViewById(R.id.more);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return FOOTER_VIEW_TYPE;
        }
        return REGULAR_VIEW_TYPE;
    }

    private class FooterHolder extends RecyclerView.ViewHolder {

        public FooterHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == REGULAR_VIEW_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.adapter_smart_contact, null);
            return new RegularHolder(itemView);
        } else {
            View itemView = new View(parent.getContext());
            int itemCount = super.getItemCount()/2;
            parentHeight = parent.getHeight();
            int height = parentHeight - smartContactHeight * itemCount;
            if (height < 0) {
                height = smartContactHeight;
            }
            itemView.setLayoutParams(new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            return new FooterHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == FOOTER_VIEW_TYPE) {
            View itemView = holder.itemView;
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            int itemCount = super.getItemCount() / 2;
            int height = parentHeight - smartContactHeight * itemCount;
            if (height < 0) {
                height = smartContactHeight;
            }
            layoutParams.height = height;
            itemView.setLayoutParams(layoutParams);
            return;
        }
        RegularHolder regularHolder = (RegularHolder) holder;
        SmartFragment.SmartModel cursorModel = getModelByPosition(position);
        String name = cursorModel.getString(ContactsContract.Contacts.DISPLAY_NAME);
        String photoUri = cursorModel.getString(ContactsContract.Contacts.PHOTO_URI);
        TextView textView = regularHolder.mTextView;
        textView.setText(name);
        TextView textViewCharacter = regularHolder.mTextViewCharacter;
        if (photoUri == null) {
            int color = ColorUtils.calculateColor(name);
            textViewCharacter.setBackgroundColor(color);
            if (StringUtil.isEmpty(name)) {
                textViewCharacter.setText(StringUtil.EMPTY);
            } else {
                textViewCharacter.setText(String.valueOf(Character.toUpperCase(name.charAt(0))));
            }
            UiUtil.setBackground(textView, null);
        } else {
            UiUtil.setBackground(textView, mTextShadow);
            UiUtil.setBackground(textViewCharacter, null);
            textViewCharacter.setText(StringUtil.EMPTY);
            getPicasso().load(photoUri).into(regularHolder.mImageView);
        }
        Long id = cursorModel.getLong(ContactsContract.Contacts._ID);
        regularHolder.mClickableView.setTag(id);
        regularHolder.mMoreView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }
}
