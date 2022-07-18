package com.arnyminerz.paraulogic.activity

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.play.payment.PaymentGateway
import com.arnyminerz.paraulogic.ui.elements.CardWithIcon
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.arnyminerz.paraulogic.ui.toast
import com.arnyminerz.paraulogic.utils.doAsync
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class DonationsActivity : AppCompatActivity() {
    private lateinit var paymentGateway: PaymentGateway

    private val availableProducts = mutableStateOf<List<ProductDetails>?>(null)
    private val availableSubscriptions = mutableStateOf<List<ProductDetails>?>(null)
    private val paymentExplanation = mutableStateOf<Pair<String, String>?>(null)

    private val purchaseMade = mutableStateOf(false)

    private val onPurchaseMade: (BillingResult, List<Purchase>?) -> Unit =
        { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                Timber.i("Purchase completed! Count: ${purchases.size}")
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == PURCHASED)
                        Firebase.analytics.logEvent(
                            FirebaseAnalytics.Event.PURCHASE,
                            Bundle(),
                        )
                }
                purchaseMade.value = true
            } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED)
            // Handle an error caused by a user cancelling the purchase flow.
                toast(R.string.toast_payment_cancelled)
            else {
                // Handle any other error codes.
                Timber.e("Could not process purchase. Error (${billingResult.responseCode}): ${billingResult.debugMessage}")
                toast(R.string.toast_payment_error)
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("Creating payment gateway...")
        paymentGateway = PaymentGateway.getInstance(this)

        Timber.d("Adding listener to PaymentGateway...")
        paymentGateway.addPurchaseListener(onPurchaseMade)

        doAsync {
            Timber.d("Getting available products...")
            val availableProducts = paymentGateway.getAvailableInAppPurchases()
            this@DonationsActivity.availableProducts.value = availableProducts
            Timber.d("There are ${availableProducts?.size} products available.")

            Timber.d("Getting available subscriptions...")
            val availableSubscriptions = paymentGateway.getAvailableSubscriptions()
            this@DonationsActivity.availableSubscriptions.value = availableSubscriptions
            Timber.d("There are ${availableSubscriptions?.size} subscriptions available.")
        }

        setContent {
            BackHandler(onBack = ::finish)

            AppTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(stringResource(R.string.donations_title))
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = ::finish
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.action_close),
                                    )
                                }
                            }
                        )
                    },
                ) { paddingValues ->

                    val isPurchaseMade by purchaseMade

                    AnimatedVisibility(
                        visible = !isPurchaseMade,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                        ) { PurchaseList() }
                    }
                    AnimatedVisibility(
                        visible = isPurchaseMade,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                        ) { Appreciation() }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        paymentGateway.removePurchaseListener(onPurchaseMade)
    }

    @ExperimentalMaterial3Api
    @Composable
    fun ColumnScope.PurchaseList() {
        val availableProducts by availableProducts
        val availableSubscriptions by availableSubscriptions
        var paymentExplanation by paymentExplanation

        if (paymentExplanation != null)
            AlertDialog(
                onDismissRequest = { paymentExplanation = null },
                confirmButton = {
                    OutlinedButton(onClick = { paymentExplanation = null }) {
                        Text(stringResource(R.string.action_close))
                    }
                },
                title = {
                    Text(
                        paymentExplanation?.first ?: "",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                text = { Text(paymentExplanation?.second ?: "") },
            )

        if (availableProducts != null && availableSubscriptions != null)
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxSize(),
            ) {
                // Information card
                item {
                    CardWithIcon(
                        icon = Icons.Outlined.Info,
                        title = getString(R.string.donations_explanation_title),
                        message = getString(R.string.donations_explanation_message),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }

                // Options cards
                items(availableProducts ?: emptyList()) { PurchaseCard(it) }
                items(availableSubscriptions ?: emptyList()) { PurchaseCard(it) }
            }
        else
            CircularProgressIndicator(
                Modifier.align(Alignment.CenterHorizontally)
            )
    }

    @Composable
    fun ColumnScope.Appreciation() {
        Image(
            painter = painterResource(R.drawable.appreciation),
            contentDescription = stringResource(R.string.image_desc_donation_appreciation),
            contentScale = ContentScale.Inside,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(.7f)
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp),
        )
        Text(
            stringResource(R.string.donations_appreciation_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp),
        )
        Text(
            stringResource(R.string.donations_appreciation_message),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        )
        Text(
            "🍪",
            fontSize = 96.sp,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }

    @Composable
    @ExperimentalMaterial3Api
    fun PurchaseCard(product: ProductDetails) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
        ) {
            Column(
                Modifier.padding(8.dp)
            ) {
                val title = product.title.let { s ->
                    s.substring(
                        0,
                        s.lastIndexOf(" (").takeIf { it >= 0 } ?: s.length,
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = product.description,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = product.oneTimePurchaseOfferDetails?.formattedPrice
                        ?: product.subscriptionOfferDetails?.joinToString("\n") { sod ->
                            sod.pricingPhases.pricingPhaseList.joinToString { it.formattedPrice }
                        }
                        ?: "0.00€",
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(end = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 22.sp,
                    textAlign = TextAlign.End,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = {
                            paymentExplanation.value = title to product.description
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = getString(R.string.image_desc_product_information),
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(stringResource(R.string.donations_action_more_info))
                    }
                    OutlinedButton(
                        onClick = {
                            if (product.oneTimePurchaseOfferDetails != null)
                                paymentGateway.purchase(
                                    this@DonationsActivity,
                                    product
                                )
                            toast("Subscriptions are not supported yet.")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingCart,
                            contentDescription = getString(R.string.image_desc_purchase),
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(stringResource(R.string.donations_action_purchase))
                    }
                }
            }
        }
    }
}