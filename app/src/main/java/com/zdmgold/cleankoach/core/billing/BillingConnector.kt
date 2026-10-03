package com.zdmgold.cleankoach.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.zdmgold.cleankoach.core.data.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingConnector @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingRepository, PurchasesUpdatedListener {

    companion object {
        const val PRO_PRODUCT_ID = "com.zdmgold.cleankoach.pro"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _entitled = MutableStateFlow(false)
    override val proEntitled: Flow<Boolean> = _entitled.asStateFlow()

    private var client: BillingClient? = null
    private var proDetails: ProductDetails? = null

    private fun ensureClient(): BillingClient {
        client?.let { return it }
        val newClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        client = newClient
        return newClient
    }

    override suspend fun refresh() {
        val billing = ensureClient()
        if (!billing.isReady) {
            billing.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch { queryPurchases(); queryProducts() }
                    }
                }
                override fun onBillingServiceDisconnected() = Unit
            })
        } else {
            queryPurchases()
            queryProducts()
        }
    }

    override suspend fun launchPurchase() {
        val billing = ensureClient()
        val activity = context as? Activity ?: return
        val details = proDetails ?: run {
            queryProducts()
            proDetails
        } ?: return

        val params = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(params))
            .build()

        billing.launchBillingFlow(activity, flowParams)
    }

    override suspend fun restore() {
        refresh()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch {
                purchases.forEach { handlePurchase(it) }
            }
        }
    }

    private fun queryPurchases() {
        val billing = ensureClient()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billing.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                scope.launch {
                    purchases.forEach { handlePurchase(it) }
                    _entitled.value = purchases.any { it.products.contains(PRO_PRODUCT_ID) }
                }
            }
        }
    }

    private fun queryProducts() {
        val billing = ensureClient()
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRO_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billing.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                proDetails = productDetailsList?.firstOrNull()
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            _entitled.value = true
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                ensureClient().acknowledgePurchase(params) { }
            }
        }
    }
}
