package mobi.wrt.android.smartcontacts.drawer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.error.ErrorHandler;
import by.istin.android.xcore.ui.DialogBuilder;
import by.istin.android.xcore.utils.Intents;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.app.MainActivity;
import mobi.wrt.android.smartcontacts.app.ThemesActivity;
import mobi.wrt.android.smartcontacts.config.ConfigProcessor;

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
            ConfigProcessor.Config config = (ConfigProcessor.Config) v.getTag();
            String shareUrl = config.getShareUrl();
            Intents.shareText(v.getContext(), v.getContext().getString(R.string.share_title), v.getContext().getString(R.string.share_message) + "\n" + shareUrl);
        }
    }),

    JOIN_GROUP(R.drawable.ic_people, R.string.join_to_group, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:join_group");
            ConfigProcessor.Config config = (ConfigProcessor.Config) v.getTag();
            final List<ConfigProcessor.Config.Group> groups = config.getGroups();
            final String[] options = new String[groups.size()];
            for (int i = 0; i < groups.size(); i++) {
                options[i] = groups.get(i).getName();
            }
            DialogBuilder.options(v.getContext(), null, options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intents.openBrowser(v.getContext(), groups.get(which).getValue());
                }
            });
        }
    }),

    RATE(R.drawable.ic_thumb_up, R.string.rate_app, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:rate");
            final ConfigProcessor.Config config = (ConfigProcessor.Config) v.getTag();
            final String rateAppUrl = config.getRateAppUrl();
            View starsContainer = View.inflate(v.getContext(), R.layout.view_dialog_stars, null);
            AlertDialog.Builder builder = DialogBuilder.createBuilder(v.getContext());
            final AlertDialog alertDialog = builder.setView(starsContainer)
                    .create();
            View.OnClickListener emailListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    ITracker.Impl.get(v.getContext()).track("leftMenu:rate:"+ v.getTag());
                    Intent sendEmailIntent = ErrorHandler.getSendEmailIntent(config.getSupportEmail(), null, v.getContext().getString(R.string.applicationLabel) + ": Rate " + v.getTag() + ": Support", null, null);
                    v.getContext().startActivity(sendEmailIntent);
                }
            };
            View.OnClickListener marketListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    ITracker.Impl.get(v.getContext()).track("leftMenu:rate:"+ v.getTag());
                    Intents.openBrowser(v.getContext(), rateAppUrl);
                }
            };
            ViewGroup starsContainerGroup = (ViewGroup) starsContainer;
            int childCount = starsContainerGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (i < 3) {
                    starsContainerGroup.getChildAt(i).setOnClickListener(emailListener);
                } else {
                    starsContainerGroup.getChildAt(i).setOnClickListener(marketListener);
                }
            }
            alertDialog.show();
        }
    }),

    PLAY_VERSION(R.drawable.ic_shop, R.string.ads_free_version, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:play_version");
            ConfigProcessor.Config config = (ConfigProcessor.Config) v.getTag();
            String proUrl = config.getProUrl();
            Intents.openBrowser(v.getContext(), proUrl);
        }
    }),

    OPEN_SOURCE(R.drawable.ic_github_icon_black, R.string.open_source, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:open_source");
            ConfigProcessor.Config config = (ConfigProcessor.Config) v.getTag();
            String gitHubUrl = config.getGithubUrl();
            Intents.openBrowser(v.getContext(), gitHubUrl);
        }
    }),

    ABOUT(R.drawable.ic_info, R.string.about, new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            ITracker.Impl.get(v.getContext()).track("leftMenu:about");
            ConfigProcessor.Config config = (ConfigProcessor.Config) v.getTag();
            String aboutUrl = config.getAboutAppUrl();
            Intents.openBrowser(v.getContext(), aboutUrl);
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
