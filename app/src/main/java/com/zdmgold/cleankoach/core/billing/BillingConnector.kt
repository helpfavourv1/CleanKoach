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
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.zdmgold.cleankoach.core.data.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingConnector @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingRepository, PurchasesUpdatedListener {

    companion object {
        const val PRO_PRODUCT_ID = "com.zdmgold.cleankoach.pro"
        private const val PREFS = "billing_prefs"
        private const val KEY_ENTITLED = "pro_entitled"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Seeded from the last verified answer so a paying user never sees ads or an
    // "Upgrade" prompt while the billing client is still connecting (or offline).
    private val _entitled = MutableStateFlow(prefs.getBoolean(KEY_ENTITLED, false))
    override val proEntitled: Flow<Boolean> = _entitled.asStateFlow()

    private val _price = MutableStateFlow<String?>(null)
    override val proPrice: Flow<String?> = _price.asStateFlow()

    private val _pending = MutableStateFlow(false)
    override val purchasePending: Flow<Boolean> = _pending.asStateFlow()

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

    /** Connects if needed; true when the client is ready to use. */
    private suspend fun awaitReady(): Boolean {
        val billing = ensureClient()
        if (billing.isReady) return true
        return suspendCancellableCoroutine { cont ->
            billing.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) {
                        cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(false)
                }
            })
        }
    }

    override suspend fun refresh() {
        if (!awaitReady()) return
        queryPurchases()
        queryProducts()
    }

    override suspend fun launchPurchase(activity: Activity): Boolean {
        if (!awaitReady()) return false
        if (proDetails == null) queryProducts()
        val details = proDetails ?: return false

        val params = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(params))
            .build()

        val result = ensureClient().launchBillingFlow(activity, flowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override suspend fun restore() {
        refresh()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch { purchases.forEach { handlePurchase(it) } }
        }
    }

    private suspend fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val result = ensureClient().queryPurchasesAsync(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) return

        val pro = result.purchasesList.filter { it.products.contains(PRO_PRODUCT_ID) }
        pro.forEach { handlePurchase(it) }
        _pending.value = pro.any { it.purchaseState == Purchase.PurchaseState.PENDING }
        setEntitled(pro.any { it.purchaseState == Purchase.PurchaseState.PURCHASED })
    }

    private suspend fun queryProducts() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRO_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        val result = ensureClient().queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            proDetails = result.productDetailsList?.firstOrNull()
            _price.value = proDetails?.oneTimePurchaseOfferDetails?.formattedPrice
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (!purchase.products.contains(PRO_PRODUCT_ID)) return
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                _pending.value = false
                setEntitled(true)
                if (!purchase.isAcknowledged) {
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    ensureClient().acknowledgePurchase(params) { }
                }
            }
            Purchase.PurchaseState.PENDING -> _pending.value = true
            else -> Unit
        }
    }

    private fun setEntitled(value: Boolean) {
        _entitled.value = value
        prefs.edit().putBoolean(KEY_ENTITLED, value).apply()
    }
}
