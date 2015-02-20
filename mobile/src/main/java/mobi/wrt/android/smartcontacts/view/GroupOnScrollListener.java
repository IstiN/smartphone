package mobi.wrt.android.smartcontacts.view;

import android.support.v7.widget.RecyclerView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IstiN on 20.02.2015.
 */
public class GroupOnScrollListener extends RecyclerView.OnScrollListener {

    private Set<RecyclerView.OnScrollListener> mScrollListener = new HashSet<>();

    public GroupOnScrollListener(RecyclerView.OnScrollListener ... listeners) {
        Collections.addAll(mScrollListener, listeners);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        for (RecyclerView.OnScrollListener scrollListener : mScrollListener) {
            scrollListener.onScrollStateChanged(recyclerView, newState);
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        for (RecyclerView.OnScrollListener scrollListener : mScrollListener) {
            scrollListener.onScrolled(recyclerView, dx, dy);
        }
    }
}
