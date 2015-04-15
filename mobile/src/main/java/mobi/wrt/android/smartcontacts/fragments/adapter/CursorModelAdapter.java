package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.model.CursorModel;
import by.istin.android.xcore.utils.CursorUtils;

/**
 * Created by uladzimir_klyshevich on 4/15/15.
 */
public abstract class CursorModelAdapter<Holder extends RecyclerView.ViewHolder, Model extends CursorModel> extends RecyclerView.Adapter<Holder> {

    private Model mModel;

    private Picasso mPicasso;

    public CursorModelAdapter(Model model) {
        this.mModel = model;
        this.mPicasso = Picasso.with(ContextHolder.get());
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public Model getModelByPosition(int position) {
        mModel.moveToPosition(position);
        return mModel;
    }

    @Override
    public int getItemCount() {
        if (CursorUtils.isEmpty(mModel)) {
            return 0;
        }
        return mModel.size();
    }

    public void swap(Model model) {
        mModel = model;
        notifyDataSetChanged();
    }
}
