package com.example.cryptodemo

import androidx.lifecycle.ViewModel
import com.example.cryptodemo.model.MarketTicker
import com.example.cryptodemo.model.TradingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TradingViewModel : ViewModel() {

    private val engine = DemoTradingEngine()

    private val _uiState = MutableStateFlow(
        TradingUiState(
            selectedPair = "BTCUSDT",
            currentPrice = 64800.0,
            marketChange = 1.35,
            marketHigh = 65500.0,
            marketLow = 64100.0,
            watchlist = listOf(
                MarketTicker("BTCUSDT", 64800.0, 1.35, 12000.0, 65500.0, 64100.0),
                MarketTicker("ETHUSDT", 3520.0, 0.82, 9800.0, 3590.0, 3440.0),
                MarketTicker("SOLUSDT", 152.5, -0.42, 7300.0, 160.0, 149.5),
                MarketTicker("BNBUSDT", 607.0, 1.12, 2600.0, 612.5, 596.0),
                MarketTicker("XRPUSDT", 0.61, 0.46, 4200.0, 0.63, 0.59)
            ),
            filteredWatchlist = listOf(
                MarketTicker("BTCUSDT", 64800.0, 1.35, 12000.0, 65500.0, 64100.0),
                MarketTicker("ETHUSDT", 3520.0, 0.82, 9800.0, 3590.0, 3440.0),
                MarketTicker("SOLUSDT", 152.5, -0.42, 7300.0, 160.0, 149.5),
                MarketTicker("BNBUSDT", 607.0, 1.12, 2600.0, 612.5, 596.0),
                MarketTicker("XRPUSDT", 0.61, 0.46, 4200.0, 0.63, 0.59)
            )
        )
    )

    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    fun selectPair(symbol: String) {
        val selected = uiState.value.watchlist.firstOrNull { it.symbol == symbol } ?: return
        _uiState.value = uiState.value.copy(
            selectedPair = symbol,
            currentPrice = selected.price,
            marketChange = selected.percentChange,
            marketHigh = selected.high,
            marketLow = selected.low
        )
    }

    fun setSearchText(value: String) {
        val state = uiState.value
        val filtered = if (value.isBlank()) {
            state.watchlist
        } else {
            state.watchlist.filter { it.symbol.contains(value, ignoreCase = true) }
        }
        _uiState.value = state.copy(searchText = value, filteredWatchlist = filtered)
    }

    fun setOrderAmount(value: String) {
        val state = uiState.value
        val amount = value.toDoubleOrNull() ?: 0.0
        _uiState.value = state.copy(
            orderAmountText = value,
            orderAmount = amount,
            estimatedMargin = engine.calculateMargin(amount, state.leverage),
            estimatedFee = engine.feeForAmount(amount)
        )
    }

    fun setLeverage(value: String) {
        val state = uiState.value
        val leverage = value.toDoubleOrNull() ?: 0.0
        _uiState.value = state.copy(
            leverage = leverage,
            leverageText = value,
            estimatedMargin = engine.calculateMargin(state.orderAmount, leverage),
            estimatedFee = engine.feeForAmount(state.orderAmount)
        )
    }

    fun setMarginMode(mode: String) {
        _uiState.value = uiState.value.copy(marginMode = mode)
    }

    fun setOrderType(type: String) {
        _uiState.value = uiState.value.copy(orderType = type)
    }

    fun setLimitPrice(value: String) {
        _uiState.value = uiState.value.copy(limitPriceText = value)
    }

    fun setStopLoss(value: String) {
        _uiState.value = uiState.value.copy(stopLossText = value)
    }

    fun setTakeProfit(value: String) {
        _uiState.value = uiState.value.copy(takeProfitText = value)
    }

    fun placeOrder(side: String, amountUsd: Double, leverage: Double, marginMode: String) {
        val state = uiState.value
        val margin = engine.calculateMargin(amountUsd, leverage)
        val quantity = if (state.currentPrice > 0.0) amountUsd / state.currentPrice else 0.0
        val position = com.example.cryptodemo.model.PositionModel(
            id = System.currentTimeMillis().toString(),
            pair = state.selectedPair,
            side = side,
            leverage = leverage,
            marginMode = marginMode,
            entryPrice = state.currentPrice,
            quantity = quantity,
            value = amountUsd,
            margin = margin,
            liquidationPrice = if (side == "BUY") state.currentPrice * 0.8 else state.currentPrice * 1.2,
            unrealizedPnl = 0.0,
            realizedPnl = 0.0
        )

        _uiState.value = state.copy(
            positions = state.positions + position,
            demoBalance = state.demoBalance - margin,
            usedMargin = state.usedMargin + margin,
            orderAmount = amountUsd,
            orderAmountText = amountUsd.toString()
        )
    }

    fun resetDemoAccount() {
        _uiState.value = uiState.value.copy(
            demoBalance = 10000.0,
            usedMargin = 0.0,
            unrealizedPnl = 0.0,
            realizedPnl = 0.0,
            positions = emptyList(),
            openOrders = emptyList()
        )
    }
}
