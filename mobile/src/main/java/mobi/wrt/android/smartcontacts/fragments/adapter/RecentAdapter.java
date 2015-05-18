package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.provider.CallLog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.ui.DialogBuilder;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.Application;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.RecentFragment;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;
import mobi.wrt.android.smartcontacts.utils.ColorUtils;

/**
 * Created by IstiN on 31.01.2015.
 */
public class RecentAdapter extends FloatHeaderAdapter<RecyclerView.ViewHolder, RecentFragment.RecentModel> implements View.OnLongClickListener {

    public static final int VIEW_TYPE_GROUP = 1;
    public static final int VIEW_TYPE_NUMBER = 0;
    public static final int VIEW_TYPE_MORE = 2;
    private static Drawable sIcCallReceived;
    private static Drawable sIcCallReceivedRed;
    private static Drawable sIcCallMade;

    static {
        Resources resources = ContextHolder.get().getResources();
        sIcCallMade = resources.getDrawable(R.drawable.ic_call_made);
        sIcCallReceived = resources.getDrawable(R.drawable.ic_call_received);
        sIcCallReceivedRed = resources.getDrawable(R.drawable.ic_call_received_red);
    }

    private boolean isLimit = true;

    public RecentAdapter(RecentFragment.RecentModel model, boolean isLimit) {
        super(model);
        this.isLimit = isLimit;
    }

    @Override
    public boolean onLongClick(View v) {
        ViewGroup itemView = (ViewGroup) v.getParent();
        final Long id = (Long) itemView.getTag();
        Context context = v.getContext();
        ContactHelper.get(context).removeCallLog(context, id, null);
        return true;
    }

    public static class Holder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        private TextView mTextView;

        private TextView mDescriptionTextView;

        private View mClickableView;

        private TextView mCharacterView;

        public Holder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mTextView = (TextView) itemView.findViewById(R.id.name);
            mDescriptionTextView = (TextView) itemView.findViewById(R.id.description);
            mClickableView = itemView.findViewById(R.id.clickableView);
            mCharacterView = (TextView) itemView.findViewById(R.id.character);
        }

    }

    public static class GroupHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;

        public GroupHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.title);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (isLimit && position == getItemCount() - 1) {
            return VIEW_TYPE_MORE;
        }
        RecentFragment.RecentModel modelByPosition = getModelByPosition(position);
        return getViewType(modelByPosition);
    }

    public int getViewType(RecentFragment.RecentModel modelByPosition) {
        String number = modelByPosition.getString(CallLog.Calls.NUMBER);
        if (number == null) {
            return VIEW_TYPE_GROUP;
        }
        return VIEW_TYPE_NUMBER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_NUMBER) {
            View itemView = View.inflate(parent.getContext(), R.layout.adapter_recent_call, null);
            return new Holder(itemView);
        } else if (viewType == VIEW_TYPE_GROUP) {
            View itemView = View.inflate(parent.getContext(), R.layout.adapter_group_header, null);
            return new GroupHolder(itemView);
        } else if (viewType == VIEW_TYPE_MORE) {
            View itemView = View.inflate(parent.getContext(), R.layout.adapter_more, null);
            return new RecyclerView.ViewHolder(itemView) {

            };
        }
        return null;
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        if (isLimit) {
            if (itemCount == 0) {
                return 0;
            }
            return itemCount + 1;
        } else {
            return itemCount;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == getItemCount() - 1) {
            return;
        }
        RecentFragment.RecentModel modelByPosition = getModelByPosition(position);
        int viewType = getViewType(modelByPosition);
        if (viewType == VIEW_TYPE_NUMBER) {
            initItem((Holder) holder, modelByPosition, getPicasso(), this);
        } else if (viewType == VIEW_TYPE_GROUP) {
            GroupHolder groupHolder = (GroupHolder) holder;
            groupHolder.mTextView.setText(modelByPosition.getString(CallLog.Calls.CACHED_NAME));
        }
    }

    public static void initItem(Holder holder, CursorModel cursorModel, Picasso picasso, View.OnLongClickListener onLongClickListener) {
        if (cursorModel == null) {
            return;
        }
        String name = cursorModel.getString(CallLog.Calls.CACHED_NAME);
        String number = cursorModel.getString(CallLog.Calls.NUMBER);
        String date = cursorModel.getString(CallLog.Calls.DATE);
        byte[] phoneLogs = cursorModel.getBlob(CallLog.Calls.TYPE);
        String title = StringUtil.isEmpty(name) ? number : name;
        holder.mTextView.setText(title);
        if (phoneLogs.length > 3) {
            holder.mDescriptionTextView.setText("(" + phoneLogs.length + ") " + number + ", " + date);
        } else {
            holder.mDescriptionTextView.setText(number + ", " + date);
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
        ViewGroup itemView = (ViewGroup) holder.mClickableView.getParent();
        itemView.setTag(cursorModel.getLong(CallLog.Calls._ID));
        holder.mClickableView.setOnLongClickListener(onLongClickListener);

        ContactHelper contactHelper = ContactHelper.get(ContextHolder.get());
        Long contactId = contactHelper.getContactId(number);
        holder.mImageView.setTag(contactId);
        String contactPhotoUri = contactHelper.getContactPhotoUri(number);
        if (contactPhotoUri == null) {
            if (contactId == null) {
                holder.mCharacterView.setText("+");
                holder.mImageView.setTag(number);
            } else {
                holder.mCharacterView.setText(name == null ? "?" : String.valueOf(Character.toUpperCase(title.charAt(0))));
            }
            UiUtil.setBackground(holder.mImageView, ColorUtils.getColorCircle(holder.mImageView.getHeight(), title));
        } else {
            holder.mCharacterView.setText(StringUtil.EMPTY);
            UiUtil.setBackground(holder.mImageView, null);
        }
        picasso.load(contactPhotoUri).transform(Application.ROUNDED_TRANSFORMATION).into(holder.mImageView);
    }


}
