package mobi.wrt.android.smartcontacts.drawer;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.analytics.ITracker;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.app.MainActivity;
import mobi.wrt.android.smartcontacts.app.ThemesActivity;

/**
 * Created by uladzimir_klyshevich on 4/29/15.
 */
public enum MenuItem {

    THEMES(R.drawable.ic_color_lens, R.string.theme, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            Intent intent = new Intent(v.getContext(), ThemesActivity.class);
            ((Activity)v.getContext()).startActivityForResult(intent, MainActivity.REQUEST_CODE_THEME);
            ITracker.Impl.get(v.getContext()).track("leftMenu:themes");
        }
    }),

    SHARE(R.drawable.ic_share, R.string.share, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:share");
        }
    }),

    JOIN_GROUP(R.drawable.ic_people, R.string.join_to_group, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:join_group");
        }
    }),

    RATE(R.drawable.ic_thumb_up, R.string.rate_app, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:rate");
        }
    }),

    PLAY_VERSION(R.drawable.ic_shop, R.string.ads_free_version, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:play_version");
        }
    }),

    ABOUT(R.drawable.ic_info, R.string.about, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:about");
        }
    }),
    ;

    int iconDrawable;

    int stringResource;

    View.OnClickListener clickListener;

    MenuItem(int iconDrawable, int stringResource, View.OnClickListener clickListener) {
        this.iconDrawable = iconDrawable;
        this.stringResource = stringResource;
        this.clickListener = clickListener;
    }


    @Override
    public String toString() {
        return ContextHolder.get().getString(stringResource);
    }
}
