package com.zdmgold.cleankoach.core.analytics

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ConsentOutcomeTest {
    @Test fun failureAlwaysAllowsRequests() {
        assertThat(consentOutcome(consentStepFailed = true, googleCanRequestAds = false)).isTrue()
        assertThat(consentOutcome(consentStepFailed = true, googleCanRequestAds = true)).isTrue()
    }

    @Test fun otherwiseGoogleDecides() {
        assertThat(consentOutcome(consentStepFailed = false, googleCanRequestAds = true)).isTrue()
        assertThat(consentOutcome(consentStepFailed = false, googleCanRequestAds = false)).isFalse()
    }
}
