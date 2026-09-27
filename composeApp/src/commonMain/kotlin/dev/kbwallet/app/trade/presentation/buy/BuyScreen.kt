package dev.kbwallet.app.trade.presentation.buy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.core.util.formatCoinUnit
import dev.kbwallet.app.notifications.presentation.component.PurchaseSuccessOverlay
import dev.kbwallet.app.trade.presentation.common.TradeScreen
import androidx.compose.runtime.LaunchedEffect
import dev.kbwallet.app.trade.presentation.common.TradeType
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.core.parameter.parametersOf
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun BuyScreen(
    coinId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = koinViewModel<BuyViewModel>(
        parameters = {
            parametersOf(coinId)
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    // A successful buy no longer closes the sheet outright — it swaps in the
    // confirmation animation, which calls onSuccess() once it has played.
    var completedPurchase by remember { mutableStateOf<BuyEvents.BuySuccess?>(null) }

    LaunchedEffect(viewModel.events) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is BuyEvents.BuySuccess -> {
                        completedPurchase = event
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        TradeScreen(
            state = state,
            tradeType = TradeType.BUY,
            onAmountChange = viewModel::onAmountChanged,
            onPercentageClicked = viewModel::onPercentageClicked,
            onSubmitClicked = viewModel::onBuyClicked,
            onToggleMode = viewModel::onToggleMode,
            onBack = onBack,
        )

        val purchase = completedPurchase
        if (purchase != null) {
            PurchaseSuccessOverlay(
                title = strings.notifPurchaseTitle,
                subtitle = strings.tradeSuccessSubtitle(
                    formatCoinUnit(purchase.amountInUnit, purchase.coinSymbol)
                ),
                onFinished = onSuccess,
                // Covers exactly the sheet content without stretching it.
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
