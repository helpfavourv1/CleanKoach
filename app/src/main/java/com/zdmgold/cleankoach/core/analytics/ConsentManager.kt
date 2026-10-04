package com.zdmgold.cleankoach.core.analytics

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.zdmgold.cleankoach.core.data.prefs.ConsentDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val consentDataStore: ConsentDataStore
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var consentInfo: ConsentInformation? = null
    private var form: ConsentForm? = null

    /** True when Google already allows ads from an earlier consent answer, so ads need not wait. */
    fun canRequestAdsNow(): Boolean =
        UserMessagingPlatform.getConsentInformation(context).canRequestAds()

    /** True in regions where the app must offer a way to change the ad consent choice. */
    fun privacyOptionsRequired(): Boolean =
        UserMessagingPlatform.getConsentInformation(context).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { }
    }

    fun requestConsent(activity: Activity, onComplete: (Boolean) -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val info = UserMessagingPlatform.getConsentInformation(context)
        consentInfo = info

        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        scope.launch {
                            consentDataStore.setUmpResolved(true)
                            consentDataStore.setUmpCanRequestAds(false)
                        }
                        onComplete(false)
                    } else {
                        val canRequest = info.canRequestAds()
                        scope.launch {
                            consentDataStore.setUmpResolved(true)
                            consentDataStore.setUmpCanRequestAds(canRequest)
                        }
                        onComplete(canRequest)
                    }
                }
            },
            {
                scope.launch {
                    consentDataStore.setUmpResolved(true)
                    consentDataStore.setUmpCanRequestAds(false)
                }
                onComplete(false)
            }
        )
    }
}
