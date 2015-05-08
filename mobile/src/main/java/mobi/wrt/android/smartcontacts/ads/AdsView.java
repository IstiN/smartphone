package mobi.wrt.android.smartcontacts.ads;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.Locale;

import by.istin.android.xcore.Core;
import by.istin.android.xcore.callable.ISuccess;
import by.istin.android.xcore.preference.PreferenceHelper;
import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.source.impl.http.HttpAndroidDataSource;
import by.istin.android.xcore.utils.Intents;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.R;

public class AdsView extends FrameLayout {

    public static final String LAST_ADS_SHOWN = "last_ads_shown";

    public AdsView(Context context) {
        super(context);
        initView(context);
    }

    public AdsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AdsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(final Context context){
        long lastAdsShown = PreferenceHelper.getLong(LAST_ADS_SHOWN, 0L);
        if (lastAdsShown == 0L) {
            PreferenceHelper.set(LAST_ADS_SHOWN, System.currentTimeMillis());
            return;
        }
        if (System.currentTimeMillis() - lastAdsShown < 4 * DateUtils.HOUR_IN_MILLIS) {
            return;
        }
        View.inflate(context, R.layout.view_ads, AdsView.this);
        Core.ExecuteOperationBuilder<AdsProcessor.AdsItem[]> responseExecuteOperationBuilder = new Core.ExecuteOperationBuilder<AdsProcessor.AdsItem[]>();
        DataSourceRequest pDataSourceRequest = new DataSourceRequest("http://ads.wrt.mobi/get?ai=" + context.getPackageName() + "&l=" + Locale.getDefault().getLanguage());
        pDataSourceRequest.setForceUpdateData(true);
        responseExecuteOperationBuilder.setDataSourceRequest(pDataSourceRequest);
        responseExecuteOperationBuilder.setProcessorKey(AdsProcessor.APP_SERVICE_KEY);
        responseExecuteOperationBuilder.setDataSourceKey(HttpAndroidDataSource.SYSTEM_SERVICE_KEY);
        responseExecuteOperationBuilder.setSuccess(new ISuccess<AdsProcessor.AdsItem[]>() {
            @Override
            public void success(AdsProcessor.AdsItem[] adsItem) {
                if (adsItem == null || adsItem.length == 0) {
                    return;
                }
                final AdsProcessor.AdsItem ads = adsItem[0];
                if (!StringUtil.isEmpty(ads.getInformation())) {
                    return;
                }
                String adsPackage = ads.getPackage();
                if (!adsPackage.equals(context.getPackageName())) {
                    if (isPackageInstalled(adsPackage, context)) {
                        return;
                    }
                }
                PreferenceHelper.set(LAST_ADS_SHOWN, System.currentTimeMillis());
                final View childAt = getChildAt(0);
                childAt.setVisibility(View.VISIBLE);
                final OnClickListener hideAdsListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlphaAnimation animation = new AlphaAnimation(1, 0);
                        animation.setDuration(500);
                        childAt.startAnimation(animation);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                AdsView.this.removeView(childAt);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                };
                childAt.setOnClickListener(hideAdsListener);

                findViewById(R.id.hideAds).setOnClickListener(hideAdsListener);
                View mainAdsContainer = findViewById(R.id.mainAdsContainer);
                mainAdsContainer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intents.openBrowser(context, ads.getLink());
                        hideAdsListener.onClick(v);
                    }
                });
                String icon = ads.getIcon();
                if (!StringUtil.isEmpty(icon)) {
                    Picasso.with(context).load(icon).into((ImageView) findViewById(R.id.icon));
                }
                TextView label = (TextView) findViewById(R.id.label);
                label.setText(ads.getTitle());
                TextView sublabel = (TextView) findViewById(R.id.sublabel);
                sublabel.setText(ads.getDescription());

                AlphaAnimation animation = new AlphaAnimation(0, 1);
                animation.setDuration(500);
                childAt.startAnimation(animation);

                TranslateAnimation translateAnimation = new TranslateAnimation(UiUtil.getDisplayWidth(), 0f, 0f, 0f);
                translateAnimation.setDuration(500l);
                mainAdsContainer.startAnimation(translateAnimation);
            }
        });
        Core.get(context).execute(responseExecuteOperationBuilder.build());
    }

    public static boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
