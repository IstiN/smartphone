package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.model.CursorModel;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 10.02.2015.
 */
public abstract class FloatHeaderAdapter<Holder extends RecyclerView.ViewHolder, Model extends CursorModel> extends RecyclerView.Adapter<Holder> {

    private Model mModel;

    private int mTopPadding;

    private IFloatHeader mFloatHeader;

    private View[] mCurrentFloatViews;

    private Picasso mPicasso;

    public FloatHeaderAdapter(Model model, int topPadding, IFloatHeader floatHeader) {
        this.mModel = model;
        this.mTopPadding = topPadding;
        this.mFloatHeader = floatHeader;
        this.mCurrentFloatViews = new View[getFloatPositionCount()];
        this.mPicasso = Picasso.with(ContextHolder.get());
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        View itemView = holder.itemView;
        if (isFloatPosition(position)) {
            if (mCurrentFloatViews[position] == null) {
                mCurrentFloatViews[position] = itemView;
                itemView.setPadding(0, mTopPadding, 0, 0);
                mFloatHeader.addTopView(itemView);
            } else if (itemView != mCurrentFloatViews[position]) {
                mCurrentFloatViews[position].setPadding(0, 0, 0, 0);
                mFloatHeader.removeTopView(mCurrentFloatViews[position]);
                mCurrentFloatViews[position] = itemView;
                itemView.setPadding(0, mTopPadding, 0, 0);
                mFloatHeader.addTopView(itemView);
            }
        } else {
            for (int i = 0; i < mCurrentFloatViews.length; i++) {
                if (itemView == mCurrentFloatViews[i]) {
                    mCurrentFloatViews[i].setPadding(0, 0, 0, 0);
                    mCurrentFloatViews[i] = null;
                }
            }
        }
    }

    protected boolean isFloatPosition(int position) {
        return position == 0;
    };

    protected int getFloatPositionCount() {
        return 1;
    };

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
