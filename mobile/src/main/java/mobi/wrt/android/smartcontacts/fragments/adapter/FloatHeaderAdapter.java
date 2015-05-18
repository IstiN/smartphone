package mobi.wrt.android.smartcontacts.fragments.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import by.istin.android.xcore.model.CursorModel;
import mobi.wrt.android.smartcontacts.responders.IFloatHeader;

/**
 * Created by IstiN on 10.02.2015.
 */
public abstract class FloatHeaderAdapter<Holder extends RecyclerView.ViewHolder, Model extends CursorModel> extends CursorModelAdapter<Holder, Model> {

    public FloatHeaderAdapter(Model model) {
        super(model);
    }


}
