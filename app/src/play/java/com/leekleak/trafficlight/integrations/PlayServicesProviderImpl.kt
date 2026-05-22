package com.leekleak.trafficlight.integrations

import android.app.Activity
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.leekleak.play_integration.AppReviewManager
import com.leekleak.trafficlight.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayServicesProviderImpl(
    private val appReviewManager: AppReviewManager
): PlayServicesProvider {
    override suspend fun onAppLaunch(activity: Activity) {
        withContext(Dispatchers.IO) {
            MobileAds.initialize(activity, InitializationConfig.Builder(BuildConfig.ADMOB_APP_ID).build())
        }
        appReviewManager.onAppLaunch(activity)
    }
}
