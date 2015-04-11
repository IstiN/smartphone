package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.Log;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.SmartFragment;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 31.01.2015.
 */
public class SmartAdapter extends FloatHeaderAdapter<SmartAdapter.Holder, SmartFragment.SmartModel> {

    public SmartAdapter(SmartFragment.SmartModel cursors, int topPadding, IFloatHeader floatHeader) {
        super(cursors, topPadding, floatHeader);
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
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(), R.layout.adapter_smart_contact, null);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        super.onBindViewHolder(holder, position);
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
}
