package com.leekleak.trafficlight.integrations

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.common.AdChoicesPlacement
import com.google.android.libraries.ads.mobile.sdk.common.AdChoicesView
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.leekleak.trafficlight.BuildConfig
import com.leekleak.trafficlight.ui.theme.card
import timber.log.Timber

val LocalNativeAdView = compositionLocalOf<NativeAdView?> { null }

@Composable
fun Ad(adLocation: AdLocation) {
    val adUnitId = when(adLocation) {
        AdLocation.Overview -> BuildConfig.ADMOB_UNIT_ID_OVERVIEW
    }
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    var adStatus by remember { mutableStateOf("loading") }

    LaunchedEffect(adUnitId) {
        Timber.d("Loading ad: $adUnitId")

        val adRequest = NativeAdRequest.Builder(adUnitId, listOf(NativeAd.NativeAdType.NATIVE))
            .setAdChoicesPlacement(AdChoicesPlacement.TOP_RIGHT)
            .build()

        NativeAdLoader.load(adRequest, object : NativeAdLoaderCallback {
            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                Timber.d("Ad loaded successfully")
                nativeAdState = nativeAd
                adStatus = "loaded"
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Timber.e("Ad failed to load: ${adError.message} (code ${adError.code})")
                adStatus = "failed"
            }
        })
    }

    DisposableEffect(nativeAdState) {
        onDispose {
            nativeAdState?.destroy()
        }
    }

    when (adStatus) {
        "loaded" -> nativeAdState?.let { CallNativeAd(it) }
        "loading" -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .card()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading Ad...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        "failed" -> { }
    }
}

@Composable
fun CallNativeAd(nativeAd: NativeAd) {
    val attributionColor = MaterialTheme.colorScheme.onTertiaryContainer.toArgb()
    val attributionBg = MaterialTheme.colorScheme.tertiaryContainer.toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .card(),
        factory = { ctx ->
            val density = ctx.resources.displayMetrics.density
            val adView = NativeAdView(ctx).apply {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            }

            val attributionLabel = TextView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    val margin = (16 * density).toInt()
                    setMargins(margin, margin, 0, 0)
                }
                text = "Ad"
                setTextColor(attributionColor)
                textSize = 12f
                background = GradientDrawable().apply {
                    setColor(attributionBg)
                    cornerRadius = 4 * density
                }
                setPadding(
                    (8 * density).toInt(),
                    (2 * density).toInt(),
                    (8 * density).toInt(),
                    (2 * density).toInt()
                )
            }

            val composeView = ComposeView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setContent {
                    CompositionLocalProvider(LocalNativeAdView provides adView) {
                        NativeAdLayout(nativeAd)
                    }
                }
            }
            
            adView.addView(composeView)
            adView.addView(attributionLabel)
            
            adView.headlineView = attributionLabel

            val adChoicesView = AdChoicesView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.TOP or android.view.Gravity.END
                }
            }
            adView.addView(adChoicesView)
            adView.adChoicesView = adChoicesView

            adView
        }
    )
}

@Composable
fun NativeAdLayout(nativeAd: NativeAd) {
    val adView = LocalNativeAdView.current
    
    Column(modifier = Modifier.padding(16.dp)) {
        Box(modifier = Modifier.height(24.dp))

        Text(
            text = nativeAd.headline ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = nativeAd.body ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        AndroidView(
            factory = { context ->
                MediaView(context).apply {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    adView?.registerNativeAd(nativeAd, this)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            nativeAd.icon?.let { icon ->
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            setImageDrawable(icon.drawable)
                        }
                    },
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = { },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = nativeAd.callToAction ?: "",
                    fontSize = 11.sp
                )
            }
        }
    }
}
