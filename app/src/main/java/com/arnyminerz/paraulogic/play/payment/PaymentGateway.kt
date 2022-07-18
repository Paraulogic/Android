package com.arnyminerz.paraulogic.play.payment

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.arnyminerz.paraulogic.utils.doAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PaymentGateway private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: PaymentGateway? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PaymentGateway(context).also {
                    INSTANCE = it
                }
            }

        private val IN_APP_PRODUCTS = listOf(
            "cinema" to BillingClient.ProductType.INAPP,
            "coffee" to BillingClient.ProductType.INAPP,
            "custom" to BillingClient.ProductType.INAPP,
            "dinner" to BillingClient.ProductType.INAPP,
        )

        private val SUBSCRIPTIONS = listOf(
            "monthly_support" to BillingClient.ProductType.SUBS,
        )
    }

    private val purchasesListeners = arrayListOf<(BillingResult, List<Purchase>?) -> Unit>()
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                consumePurchases(purchases)
                ackPurchases(purchases)
            }

            purchasesListeners.forEach { it(billingResult, purchases) }
        }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    private suspend fun connect() = suspendCoroutine { cont ->
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        Timber.i("Billing Client is ready.")
                        return cont.resume(billingResult)
                    } else
                        Timber.i("Billing Client setup finished. Result: $billingResult")
                    cont.resumeWithException(IllegalStateException("Billing client result not ok. Result: $billingResult"))
                }

                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    Timber.i("Billing service has been disconnected.")
                }
            }
        )
    }

    suspend fun getAvailableInAppPurchases(): List<ProductDetails>? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                IN_APP_PRODUCTS.map { (productId, productType) ->
                    QueryProductDetailsParams.Product
                        .newBuilder()
                        .setProductId(productId)
                        .setProductType(productType)
                        .build()
                }
            )
            .build()

        if (!billingClient.isReady)
            connect()

        return withContext(Dispatchers.IO) { billingClient.queryProductDetails(params) }
            .productDetailsList
    }

    suspend fun getAvailableSubscriptions(): List<ProductDetails>? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                SUBSCRIPTIONS.map { (productId, productType) ->
                    QueryProductDetailsParams.Product
                        .newBuilder()
                        .setProductId(productId)
                        .setProductType(productType)
                        .build()
                }
            )
            .build()

        if (!billingClient.isReady)
            connect()

        return withContext(Dispatchers.IO) { billingClient.queryProductDetails(params) }
            .productDetailsList
    }

    private fun ackPurchases(purchases: List<Purchase>) {
        val ackParams = purchases.mapNotNull { purchase ->
            if (!purchase.isAcknowledged)
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            else null
        }
        doAsync {
            ackParams.forEach { ackParam ->
                val billingResult = billingClient.acknowledgePurchase(ackParam)
                if (billingResult.responseCode == BillingResponseCode.OK)
                    Timber.i("Purchase acknowledged!")
                else
                    Timber.e("Could not acknowledge purchase! Error (${billingResult.responseCode}): ${billingResult.debugMessage}")
            }
        }
    }

    private fun consumePurchases(purchases: List<Purchase>) {
        val consumeParams = purchases.map { purchase ->
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        }
        doAsync {
            consumeParams.forEach { params ->
                Timber.d("Consuming purchase: ${params.purchaseToken}")
                val consumeResult = billingClient.consumePurchase(params)
                val billingResult = consumeResult.billingResult
                if (billingResult.responseCode == BillingResponseCode.OK)
                    Timber.i("Purchase consumed! Token=${consumeResult.purchaseToken}")
                else
                    Timber.e("Could not consume purchase! Error (${billingResult.responseCode}): ${billingResult.debugMessage}")
            }
        }
    }

    fun addPurchaseListener(listener: (billingResult: BillingResult, purchases: List<Purchase>?) -> Unit) =
        purchasesListeners.add(listener)

    fun removePurchaseListener(listener: (billingResult: BillingResult, purchases: List<Purchase>?) -> Unit) =
        purchasesListeners.remove(listener)

    fun purchase(activity: Activity, product: ProductDetails, offerToken: String?): BillingResult {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .apply { offerToken?.let { setOfferToken(it) } }
                        .build()
                )
            )
            .build()

        if (!billingClient.isReady)
            throw IllegalStateException("Billing client is not ready.")

        return billingClient.launchBillingFlow(activity, billingFlowParams)
    }
}