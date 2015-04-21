package mobi.wrt.android.smartcontacts.ads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.aebuwtdvfd.tcyvussuxg23220.AdListener;
import com.aebuwtdvfd.tcyvussuxg23220.Bun;

import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.utils.Log;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.BuildConfig;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.app.MainActivity;

/**
 * Created by uladzimir_klyshevich on 4/21/15.
 */
public class AdsProvider implements IAdsProvider {

    private Bun mBun;

    private ITracker mTracker;

    private boolean isAppWallCached = false;

    private AdListener mAdListener = new AdListener() {
        @Override
        public void onSmartWallAdShowing() {

        }

        @Override
        public void onSmartWallAdClosed() {

        }

        @Override
        public void onAdError(String s) {
            mTracker.track("smart:onAdError");
        }

        @Override
        public void onSDKIntegrationError(String s) {

        }

        @Override
        public void onAdCached(AdType adType) {
            if (adType == AdType.smartwall) {
                isAppWallCached = true;
                mTracker.track("smart:onAdCached");
            }
        }

        @Override
        public void noAdAvailableListener() {
            mTracker.track("smart:noAdAvailableListener");
        }
    };

    @Override
    public void onCreate(final Activity activity) {
        mTracker = ITracker.Impl.get(activity);
        mBun = new Bun(activity.getApplicationContext(), mAdListener, true);
        mBun.startNotificationAd(BuildConfig.DEBUG);
        mBun.callSmartWallAd();
        mBun.call360Ad(activity, 1, BuildConfig.DEBUG, new AdListener.MraidAdListener() {
            @Override
            public void onAdLoadingListener() {
                Log.xd(activity, "onAdLoadingListener");
            }

            @Override
            public void onAdLoadedListener() {
                Log.xd(activity, "onAdLoadedListener");
                View fab = activity.findViewById(R.id.fab);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fab.getLayoutParams();
                layoutParams.bottomMargin = UiUtil.getDp(activity, 48);
                fab.setLayoutParams(layoutParams);
                mTracker.track("onAdLoadedListener");
            }

            @Override
            public void onErrorListener(String s) {
                mTracker.track("onErrorListener");
                Log.xd(activity, "onErrorListener " + s);
            }

            @Override
            public void onCloseListener() {
                Log.xd(activity, "onCloseListener");
            }

            @Override
            public void onAdExpandedListner() {
                Log.xd(activity, "onAdExpandedListner");
            }

            @Override
            public void onAdClickListener() {
                Log.xd(activity, "onAdClickListener");
                mTracker.track("onAdClickListener");
                View fab = activity.findViewById(R.id.fab);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fab.getLayoutParams();
                layoutParams.bottomMargin = UiUtil.getDp(activity, 16);
                fab.setLayoutParams(layoutParams);
            }

            @Override
            public void noAdAvailableListener() {
                mTracker.track("noAdAvailableListener");
                Log.xd(activity, "noAdAvailableListener");
            }
        });
    }

    @Override
    public void onBackPressed(Activity activity) {
        if (isAppWallCached) {
            try {
                mBun.showCachedAd(activity, AdListener.AdType.smartwall);
                mTracker.track("smart:showCachedAd");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFabClick(Activity activity) {
        ViewGroup mainContent = findMainView(activity);
        if (mainContent != null && mainContent.getChildCount() > 1) {
            mainContent.getChildAt(1).setVisibility(View.GONE);
        }
    }

    @Override
    public void onPhoneClosed(Activity activity) {
        ViewGroup mainContent = findMainView(activity);
        if (mainContent != null && mainContent.getChildCount() > 1) {
            mainContent.getChildAt(1).setVisibility(View.VISIBLE);
        }
    }

    private ViewGroup findMainView(Activity activity) {
        return (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

}
