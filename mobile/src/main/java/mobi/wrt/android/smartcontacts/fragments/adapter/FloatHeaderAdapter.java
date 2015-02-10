package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.Log;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.fragments.SmartFragment;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 10.02.2015.
 */
public abstract class FloatHeaderAdapter<Holder extends RecyclerView.ViewHolder, Model extends CursorModel> extends RecyclerView.Adapter<Holder> {

    private Model mModel;

    private int mTopPadding;

    private IFloatHeader mFloatHeader;

    private View mCurrentFloatView;

    public FloatHeaderAdapter(Model model, int topPadding, IFloatHeader floatHeader) {
        this.mModel = model;
        this.mTopPadding = topPadding;
        this.mFloatHeader = floatHeader;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        View itemView = holder.itemView;
        if (position == 0) {
            if (mCurrentFloatView == null) {
                mCurrentFloatView = itemView;
                itemView.setPadding(0, mTopPadding, 0, 0);
                mFloatHeader.addTopView(mCurrentFloatView);
            } else if (mCurrentFloatView != itemView) {
                mCurrentFloatView.setPadding(0, 0, 0, 0);
                mFloatHeader.removeTopView(mCurrentFloatView);
                mCurrentFloatView = itemView;
                itemView.setPadding(0, mTopPadding, 0, 0);
                mFloatHeader.addTopView(mCurrentFloatView);
            }

        }
    }

    public Model getModelByPosition(int position) {
        mModel.moveToPosition(position);
        return mModel;
    }

    @Override
    public int getItemCount() {
        return mModel.size();
    }

    public void swap(Model model) {
        mModel = model;
        notifyDataSetChanged();
    }
}
