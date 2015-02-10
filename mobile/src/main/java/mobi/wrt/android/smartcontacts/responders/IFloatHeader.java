package mobi.wrt.android.smartcontacts.responders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by IstiN on 04.02.2015.
 */
public interface IFloatHeader {

    int attach(RecyclerView recyclerView);

    void addTopView(View view);

    void removeTopView(View view);
}
