package com.zdmgold.cleankoach.core.ads

import android.app.Activity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val ads: AdsManager
) : ViewModel() {

    val adsEnabled: StateFlow<Boolean> = ads.adsEnabled

    val privacyOptionsRequired: Boolean get() = ads.privacyOptionsRequired()

    fun onActionCompleted(activity: Activity) = ads.onActionCompleted(activity)

    fun showPrivacyOptions(activity: Activity) = ads.showPrivacyOptions(activity)
}
