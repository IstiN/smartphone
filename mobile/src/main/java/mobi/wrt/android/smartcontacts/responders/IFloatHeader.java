package mobi.wrt.android.smartcontacts.responders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import mobi.wrt.android.smartcontacts.view.GroupOnScrollListener;

/**
 * Created by IstiN on 04.02.2015.
 */
public interface IFloatHeader {

    void attach(RecyclerView.OnScrollListener scrollListener, RecyclerView recyclerView);

}
