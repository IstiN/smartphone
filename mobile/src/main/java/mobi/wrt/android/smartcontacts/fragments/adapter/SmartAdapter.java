package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.provider.ContactsContract;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.Log;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.SmartFragment;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 31.01.2015.
 */
public class SmartAdapter extends FloatHeaderAdapter<SmartAdapter.Holder, SmartFragment.SmartModel> {

    public static final int REGULAR_VIEW_TYPE = 0;
    public static final int FOOTER_VIEW_TYPE = 1;

    private int smartContactHeight;

    private int parentHeight;

    public SmartAdapter(SmartFragment.SmartModel cursors, int topPadding, IFloatHeader floatHeader) {
        super(cursors, topPadding, floatHeader);
        smartContactHeight = ContextHolder.get().getResources().getDimensionPixelSize(R.dimen.smart_contact_height);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        private TextView mTextView;

        private View mClickableView;

        private View mMoreView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mTextView = (TextView) itemView.findViewById(R.id.name);
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

    private class FooterHolder extends Holder {

        public FooterHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == REGULAR_VIEW_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.adapter_smart_contact, null);
            return new Holder(itemView);
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
    public void onBindViewHolder(Holder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (position == getItemCount() - 1) {
            View itemView = holder.itemView;
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            int itemCount = super.getItemCount()/2;
            int height = parentHeight - smartContactHeight * itemCount;
            if (height < 0) {
                height = smartContactHeight;
            }
            layoutParams.height = height;
            itemView.setLayoutParams(layoutParams);
            return;
        }
        SmartFragment.SmartModel cursorModel = getModelByPosition(position);
        String name = cursorModel.getString(ContactsContract.Contacts.DISPLAY_NAME);
        String photoUri = cursorModel.getString(ContactsContract.Contacts.PHOTO_URI);
        holder.mTextView.setText(name);
        getPicasso().load(photoUri).into(holder.mImageView);
        Long id = cursorModel.getLong(ContactsContract.Contacts._ID);
        holder.mClickableView.setTag(id);
        holder.mMoreView.setTag(id);
    }

    @Override
    protected boolean isFloatPosition(int position) {
        return position == 0 || position == 1;
    }

    @Override
    protected int getFloatPositionCount() {
        return 2;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }
}
