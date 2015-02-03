package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.CursorUtils;
import mobi.wrt.android.smartcontacts.Application;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.RecentFragment;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;

/**
 * Created by IstiN on 31.01.2015.
 */
public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.Holder> {

    public static class Holder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        private TextView mTextView;

        private TextView mDescriptionTextView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mTextView = (TextView) itemView.findViewById(R.id.name);
            mDescriptionTextView = (TextView) itemView.findViewById(R.id.description);
        }

    }

    private RecentFragment.RecentModel mRecentModel;

    public RecentAdapter(RecentFragment.RecentModel recentModel) {
        this.mRecentModel = recentModel;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(), R.layout.adapter_recent_call, null);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        CursorModel cursorModel = mRecentModel.get(position);
        initItem(holder, cursorModel);
    }

    public static void initItem(Holder holder, CursorModel cursorModel) {
        String name = cursorModel.getString(CallLog.Calls.CACHED_NAME);
        String number = cursorModel.getString(CallLog.Calls.NUMBER);
        holder.mTextView.setText(name == null ? number : name);
        ContentValues values  = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursorModel, values);
        holder.mDescriptionTextView.setText(values.toString());
        Picasso.with(holder.mImageView.getContext()).load(ContactHelper.get(ContextHolder.get()).getContactPhotoUri(number)).transform(Application.ROUNDED_TRANSFORMATION).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mRecentModel.size();
    }

    public void swap(RecentFragment.RecentModel model) {
        mRecentModel = model;
        notifyDataSetChanged();
    }
}
