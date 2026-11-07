package com.zdmgold.cleankoach.core.analytics

/**
 * Decides whether ads may be requested after the consent step.
 * A failed step (no connection, form error, timeout) still allows requests, as Google advises:
 * the SDK then applies the last saved consent status. Otherwise Google's own answer is used.
 */
fun consentOutcome(consentStepFailed: Boolean, googleCanRequestAds: Boolean): Boolean =
    if (consentStepFailed) true else googleCanRequestAds
