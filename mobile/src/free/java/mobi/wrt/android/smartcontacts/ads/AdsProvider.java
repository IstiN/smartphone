package mobi.wrt.android.smartcontacts.ads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.aebuwtdvfd.tcyvussuxg23220.AdConfig;
import com.aebuwtdvfd.tcyvussuxg23220.AdListener;
import com.aebuwtdvfd.tcyvussuxg23220.EulaListener;
import com.aebuwtdvfd.tcyvussuxg23220.Main;

import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.utils.Log;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.BuildConfig;
import mobi.wrt.android.smartcontacts.R;

/**
 * Created by uladzimir_klyshevich on 4/21/15.
 */
public class AdsProvider implements IAdsProvider {

    private Main main;

    private ITracker mTracker;

    private boolean isAppWallCached = false;

    private Activity activity;

    private AdListener mAdListener = new AdListener() {

        @Override
        public void onAdCached(AdConfig.AdType adType) {
            if (adType == AdConfig.AdType.smartwall) {
                isAppWallCached = true;
                mTracker.track("smart:onAdCached");
            }
        }

        @Override
        public void onIntegrationError(String s) {
            mTracker.track("onErrorListener");
            Log.xd(activity, "onErrorListener " + s);
        }

        @Override
        public void onAdError(String s) {
            mTracker.track("smart:onAdError " + s);
        }

        @Override
        public void noAdListener() {
            mTracker.track("noAdAvailableListener");
            Log.xd(activity, "noAdAvailableListener");
        }

        @Override
        public void onAdShowing() {

        }

        @Override
        public void onAdClosed() {

        }

        @Override
        public void onAdLoadingListener() {

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
        public void onCloseListener() {

        }

        @Override
        public void onAdExpandedListner() {

        }

        @Override
        public void onAdClickedListener() {
            Log.xd(activity, "onAdClickListener");
            mTracker.track("onAdClickListener");
            View fab = activity.findViewById(R.id.fab);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fab.getLayoutParams();
            layoutParams.bottomMargin = UiUtil.getDp(activity, 16);
            fab.setLayoutParams(layoutParams);
        }

    };

    @Override
    public void onCreate(final Activity activity) {
        this.activity = activity;
        mTracker = ITracker.Impl.get(activity);
        AdConfig.setAppId(272015);
        AdConfig.setApiKey("1346148707232208873");
        AdConfig.setEulaListener(new EulaListener() {
            @Override
            public void optinResult(boolean b) {

            }

            @Override
            public void showingEula() {

            }
        });
        AdConfig.setAdListener(mAdListener);
        AdConfig.setCachingEnabled(true);
        AdConfig.setPlacementId(0);

        //initialize Airpush
        main = new Main(activity); //Here this is reference of current Activity.
        //for calling banner 360
        main.start360BannerAd(activity);  //pass the current Activity's reference.
        main.startPushAd();
    }

    @Override
    public void onBackPressed(Activity activity) {
        if (isAppWallCached) {
            try {
                main.showCachedAd(AdConfig.AdType.smartwall);
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
