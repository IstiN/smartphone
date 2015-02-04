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

/**
 * Created by IstiN on 31.01.2015.
 */
public class SmartAdapter extends RecyclerView.Adapter<SmartAdapter.Holder> {

    public static class Holder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        private TextView mTextView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mTextView = (TextView) itemView.findViewById(R.id.name);
        }

    }

    private SmartFragment.SmartModel mSmartModel;

    private int mTopPadding;

    public SmartAdapter(SmartFragment.SmartModel smartModel, int topPadding) {
        this.mSmartModel = smartModel;
        this.mTopPadding = topPadding;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(), R.layout.adapter_smart_contact, null);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        View contentView = holder.itemView.findViewById(R.id.content);
        if (position == 0) {
            holder.itemView.setPadding(0, mTopPadding, 0, 0);
        }
        /*TODO if (position == 0) {
            ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
            layoutParams.height = layoutParams.height * 2;
            contentView.setLayoutParams(layoutParams);
        }*/

        CursorModel cursorModel = mSmartModel.get(position);
        String name = cursorModel.getString(ContactsContract.Contacts.DISPLAY_NAME);
        String photoUri = cursorModel.getString(ContactsContract.Contacts.PHOTO_URI);
        holder.mTextView.setText(name);
        Log.xd(this, photoUri);
        Picasso.with(holder.mImageView.getContext()).load(photoUri).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mSmartModel.size();
    }

    public void swap(SmartFragment.SmartModel model) {
        mSmartModel = model;
        notifyDataSetChanged();
    }
}
