package com.minhavida.drawtext;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class AdMobClass extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String APP_OPEN_AD_UNIT_ID = "ca-app-pub-3543569662200404/8808396759";



    private AppOpenAd appOpenAd;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;
    private Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        MobileAds.initialize(this, initializationStatus -> loadAppOpenAd());
    }

    private void loadAppOpenAd() {
        if (isLoadingAd || appOpenAd != null) return;

        isLoadingAd = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        AppOpenAd.load(
                this,
                APP_OPEN_AD_UNIT_ID,
                adRequest,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        appOpenAd = ad;
                        isLoadingAd = false;
                        android.util.Log.d("AdMob", "AppOpen carregou");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        appOpenAd = null;
                        isLoadingAd = false;
                        android.util.Log.e("AdMob", "Falha ao carregar: " 
                                + loadAdError.getCode() + " / "
                                + loadAdError.getMessage() + " / "
                                + loadAdError.getDomain());
                    }
                });
    }

    private void showAppOpenAdIfAvailable() {
        if (isShowingAd || appOpenAd == null || currentActivity == null) {
            loadAppOpenAd();
            return;
        }

        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                loadAppOpenAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                appOpenAd = null;
                isShowingAd = false;
                loadAppOpenAd();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                isShowingAd = true;
            }
        });

        appOpenAd.show(currentActivity);
    }

    // ActivityLifecycleCallbacks
    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
        showAppOpenAdIfAvailable();
    }

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivityStarted(Activity activity) {
        currentActivity = activity;
        showAppOpenAdIfAvailable();
    }

    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}