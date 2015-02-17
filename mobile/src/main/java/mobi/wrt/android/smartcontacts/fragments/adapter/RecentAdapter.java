package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.provider.CallLog;
import android.support.v7.widget.RecyclerView;
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

    private static Drawable sIcCallReceived;
    private static Drawable sIcCallReceivedRed;
    private static Drawable sIcCallMade;

    static {
        Resources resources = ContextHolder.get().getResources();
        sIcCallMade = resources.getDrawable(R.drawable.ic_call_made);
        sIcCallReceived = resources.getDrawable(R.drawable.ic_call_received);
        sIcCallReceivedRed = resources.getDrawable(R.drawable.ic_call_received_red);
    }
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
        byte[] phoneLogs = cursorModel.getBlob(CallLog.Calls.TYPE);
        holder.mTextView.setText(name == null ? number : name);
        //ContentValues values  = new ContentValues();
        //DatabaseUtils.cursorRowToContentValues(cursorModel, values);
        //TODO add date
        if (phoneLogs.length > 3) {
            holder.mDescriptionTextView.setText("(" + phoneLogs.length + ") " + number);
        } else {
            holder.mDescriptionTextView.setText(number);
        }
        for (int i = 0; i < 3; i++) {
            ImageView viewWithTag = (ImageView) holder.itemView.findViewWithTag("c_icon_" + i);
            if (i < phoneLogs.length) {
                byte type = phoneLogs[i];
                if (type == CallLog.Calls.INCOMING_TYPE) {
                    viewWithTag.setImageDrawable(sIcCallReceived);
                } else if (type == CallLog.Calls.MISSED_TYPE) {
                    viewWithTag.setImageDrawable(sIcCallReceivedRed);
                } else if (type == CallLog.Calls.OUTGOING_TYPE) {
                    viewWithTag.setImageDrawable(sIcCallMade);
                }
                viewWithTag.setVisibility(View.VISIBLE);
            } else {
                viewWithTag.setVisibility(View.GONE);
            }
        }
        holder.mClickableView.setTag(number);
        ContactHelper contactHelper = ContactHelper.get(ContextHolder.get());
        holder.mImageView.setTag(contactHelper.getContactId(number));
        Picasso.with(holder.mImageView.getContext()).load(contactHelper.getContactPhotoUri(number)).transform(Application.ROUNDED_TRANSFORMATION).into(holder.mImageView);
    }

}
