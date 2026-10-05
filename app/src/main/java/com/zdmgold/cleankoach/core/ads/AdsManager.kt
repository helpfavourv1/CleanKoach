package com.zdmgold.cleankoach.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.zdmgold.cleankoach.core.analytics.ConsentManager
import com.zdmgold.cleankoach.core.diagnostics.CrashReporter
import com.zdmgold.cleankoach.core.data.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Consent first, ads second, Pro always wins.
 * No ad is requested until Google's consent flow says ads may be requested, and nothing
 * is ever loaded or shown while Pro is active or a purchase is pending.
 */
@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    billing: BillingRepository,
    private val consentManager: ConsentManager
) {

    companion object {
        private const val MIN_GAP_MS = 60_000L
        private const val MAX_PER_SESSION = 4
        private const val MAX_PER_DAY = 20
        private const val EVERY_NTH_ACTION = 2
        private const val IDLE_RETURN_MS = 5 * 60_000L

        private const val PREFS = "ad_pacing"
        private const val KEY_DAY = "ads_date"
        private const val KEY_TODAY = "ads_today"
        private const val KEY_LAST_SHOWN = "last_shown"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val canRequest = MutableStateFlow(false)

    /** True when banners and interstitials are allowed right now. */
    val adsEnabled: StateFlow<Boolean> = combine(billing.proEntitled, canRequest) { pro, can ->
        !pro && can
    }.stateIn(scope, SharingStarted.Eagerly, false)

    private var pending = false
    private var proActive = false
    private var sdkStarted = false
    private var interstitial: InterstitialAd? = null
    private var loading = false
    private var showing = false
    private var shownThisSession = 0
    private var completedActions = 0
    private var backgroundedAt = 0L

    init {
        scope.launch { billing.purchasePending.collect { pending = it } }
        scope.launch { billing.proEntitled.collect { proActive = it } }
    }

    /** Call once per activity start, before any ad is requested. */
    fun start(activity: Activity) {
        CrashReporter.note("ads start (activity=${activity.javaClass.simpleName}@${System.identityHashCode(activity)})")
        consentManager.requestConsent(activity) { allowed -> applyConsent(allowed) }
        // Returning users already hold an answer; do not make them wait for the network.
        if (consentManager.canRequestAdsNow()) applyConsent(true)
    }

    fun privacyOptionsRequired(): Boolean = runCatching { consentManager.privacyOptionsRequired() }.getOrDefault(false)

    fun showPrivacyOptions(activity: Activity) = consentManager.showPrivacyOptions(activity)

    private fun applyConsent(allowed: Boolean) {
        CrashReporter.note("consent result: allowed=$allowed")
        if (allowed && !proActive && !sdkStarted) {
            sdkStarted = true
            MobileAds.initialize(context) { loadInterstitial() }
        }
        canRequest.value = allowed
        if (allowed && !proActive) loadInterstitial()
    }

    fun onAppBackgrounded() {
        backgroundedAt = System.currentTimeMillis()
    }

    /** Back after five idle minutes: at most one interstitial. Never on a cold start. */
    fun onAppForegrounded(activity: Activity) {
        val left = backgroundedAt
        backgroundedAt = 0L
        if (left > 0L && System.currentTimeMillis() - left >= IDLE_RETURN_MS) tryShow(activity)
    }

    /** A finished action (clean-up, delete, optimizer batch, Wi-Fi or speed result). Every 2nd one may show an ad. */
    fun onActionCompleted(activity: Activity) {
        completedActions += 1
        if (completedActions % EVERY_NTH_ACTION != 0) return
        tryShow(activity)
    }

    private fun tryShow(activity: Activity) {
        if (!adsEnabled.value || pending || showing || activity.isFinishing) return
        val ad = interstitial
        if (ad == null) {
            loadInterstitial()
            return
        }
        val now = System.currentTimeMillis()
        if (shownThisSession >= MAX_PER_SESSION) return
        if (shownToday() >= MAX_PER_DAY) return
        if (now - prefs.getLong(KEY_LAST_SHOWN, 0L) < MIN_GAP_MS) return

        showing = true
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                shownThisSession += 1
                prefs.edit()
                    .putString(KEY_DAY, today())
                    .putInt(KEY_TODAY, shownToday() + 1)
                    .putLong(KEY_LAST_SHOWN, System.currentTimeMillis())
                    .apply()
            }

            override fun onAdDismissedFullScreenContent() {
                showing = false
                loadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                showing = false
                loadInterstitial()
            }
        }
        ad.show(activity)
    }

    private fun loadInterstitial() {
        if (!canRequest.value || proActive || !sdkStarted) return
        if (interstitial != null || loading) return
        loading = true
        InterstitialAd.load(
            context,
            AdIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    loading = false
                }
            }
        )
    }

    private fun today(): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    private fun shownToday(): Int =
        if (prefs.getString(KEY_DAY, null) == today()) prefs.getInt(KEY_TODAY, 0) else 0
}
