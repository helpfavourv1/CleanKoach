package com.zdmgold.cleankoach.core.data.repository

import android.app.Activity
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    val proEntitled: Flow<Boolean>

    /** Localized price of the Pro product, or null while it cannot be loaded. */
    val proPrice: Flow<String?>

    /** True while Google Play reports the Pro payment as pending. */
    val purchasePending: Flow<Boolean>

    suspend fun refresh()

    /** Returns false when the purchase sheet could not be opened (product unavailable, no connection). */
    suspend fun launchPurchase(activity: Activity): Boolean

    suspend fun restore()
}
