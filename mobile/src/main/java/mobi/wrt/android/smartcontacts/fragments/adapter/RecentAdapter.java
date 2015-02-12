package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.provider.CallLog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.model.CursorModel;
import mobi.wrt.android.smartcontacts.Application;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.RecentFragment;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 31.01.2015.
 */
public class RecentAdapter extends FloatHeaderAdapter<RecentAdapter.Holder, RecentFragment.RecentModel> {

    public RecentAdapter(RecentFragment.RecentModel model, int topPadding, IFloatHeader floatHeader) {
        super(model, topPadding, floatHeader);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        private TextView mTextView;

        private TextView mDescriptionTextView;

        private View mClickableView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mTextView = (TextView) itemView.findViewById(R.id.name);
            mDescriptionTextView = (TextView) itemView.findViewById(R.id.description);
            mClickableView = itemView.findViewById(R.id.clickableView);
        }

    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(), R.layout.adapter_recent_call, null);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        super.onBindViewHolder(holder, position);
        initItem(holder, getModelByPosition(position));
    }

    public static void initItem(Holder holder, CursorModel cursorModel) {
        if (cursorModel == null) {
            return;
        }
        String name = cursorModel.getString(CallLog.Calls.CACHED_NAME);
        String number = cursorModel.getString(CallLog.Calls.NUMBER);
        holder.mTextView.setText(name == null ? number : name);
        //ContentValues values  = new ContentValues();
        //DatabaseUtils.cursorRowToContentValues(cursorModel, values);
        //TODO add date
        holder.mDescriptionTextView.setText(number);
        holder.mClickableView.setTag(number);
        ContactHelper contactHelper = ContactHelper.get(ContextHolder.get());
        holder.mImageView.setTag(contactHelper.getContactId(number));
        Picasso.with(holder.mImageView.getContext()).load(contactHelper.getContactPhotoUri(number)).transform(Application.ROUNDED_TRANSFORMATION).into(holder.mImageView);
    }

}
