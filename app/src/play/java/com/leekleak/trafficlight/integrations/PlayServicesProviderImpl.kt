package com.leekleak.trafficlight.integrations

import android.app.Activity
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.leekleak.play_integration.AppReviewManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayServicesProviderImpl(
    private val appReviewManager: AppReviewManager
): PlayServicesProvider {
    override suspend fun onAppLaunch(activity: Activity) {
        withContext(Dispatchers.IO) {
            // Test App ID for AdMob
            MobileAds.initialize(activity, InitializationConfig.Builder("ca-app-pub-3940256099942544~3347511713").build())
        }
        appReviewManager.onAppLaunch(activity)
    }
}
