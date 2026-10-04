package com.zdmgold.cleankoach.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.zdmgold.cleankoach.core.ads.AdIds
import com.zdmgold.cleankoach.core.ads.AdsViewModel

/**
 * 320 x 50 dp banner. Takes no space at all until an ad has loaded, and never exists for Pro
 * users or before ad consent is settled.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val viewModel: AdsViewModel = hiltViewModel()
    val enabled by viewModel.adsEnabled.collectAsStateWithLifecycle()
    if (!enabled) return

    val context = LocalContext.current
    var loaded by remember { mutableStateOf(false) }
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AdIds.BANNER
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    loaded = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loaded = false
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(adView) { onDispose { adView.destroy() } }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (loaded) 50.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { adView },
            modifier = Modifier
                .width(320.dp)
                .height(50.dp)
        )
    }
}

/** Bottom of a screen: optional bar above, banner below, system navigation inset around both. */
@Composable
fun BottomAdBar(
    modifier: Modifier = Modifier,
    above: @Composable ColumnScope.() -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth().navigationBarsPadding()) {
        above()
        BannerAd()
    }
}
