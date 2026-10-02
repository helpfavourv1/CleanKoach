package com.zdmgold.cleankoach.core.data.repository

import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    val proEntitled: Flow<Boolean>
    suspend fun refresh()
    suspend fun launchPurchase()
    suspend fun restore()
}
