package com.example.cryptodemo.model

import kotlinx.serialization.Serializable

@Serializable
data class MarketTicker(
    val symbol: String,
    val price: Double,
    val percentChange: Double,
    val volume: Double,
    val high: Double,
    val low: Double,
    val updateTime: Long = System.currentTimeMillis()
)

@Serializable
data class OrderBookEntry(
    val price: Double,
    val quantity: Double
)

@Serializable
data class OrderBook(
    val bids: List<OrderBookEntry> = emptyList(),
    val asks: List<OrderBookEntry> = emptyList(),
    val spread: Double = 0.0
)

@Serializable
data class Candle(
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double,
    val timestamp: Long
)

@Serializable
data class PositionModel(
    val id: String,
    val pair: String,
    val side: String,
    val leverage: Double,
    val marginMode: String,
    val entryPrice: Double,
    val quantity: Double,
    val value: Double,
    val margin: Double,
    val liquidationPrice: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val unrealizedPnl: Double = 0.0,
    val realizedPnl: Double = 0.0
)

@Serializable
data class OrderModel(
    val id: String,
    val pair: String,
    val side: String,
    val type: String,
    val price: Double,
    val amount: Double,
    val status: String = "OPEN"
)

@Serializable
data class TradingUiState(
    val selectedPair: String = "BTCUSDT",
    val currentPrice: Double = 0.0,
    val marketChange: Double = 0.0,
    val marketHigh: Double = 0.0,
    val marketLow: Double = 0.0,
    val fundingRate: Double = 0.0,
    val connectionStatus: String = "Connected",
    val lastUpdateDisplay: String = "Just now",
    val watchlist: List<MarketTicker> = emptyList(),
    val filteredWatchlist: List<MarketTicker> = emptyList(),
    val searchText: String = "",
    val candles: List<Candle> = emptyList(),
    val orderBook: OrderBook = OrderBook(),
    val marginMode: String = "CROSS",
    val leverage: Double = 10.0,
    val leverageText: String = "10",
    val orderType: String = "MARKET",
    val limitPriceText: String = "",
    val stopLossText: String = "",
    val takeProfitText: String = "",
    val orderAmount: Double = 100.0,
    val orderAmountText: String = "100",
    val estimatedMargin: Double = 0.0,
    val estimatedFee: Double = 0.0,
    val demoBalance: Double = 10000.0,
    val usedMargin: Double = 0.0,
    val unrealizedPnl: Double = 0.0,
    val realizedPnl: Double = 0.0,
    val positions: List<PositionModel> = emptyList(),
    val openOrders: List<OrderModel> = emptyList()
)

