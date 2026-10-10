package com.example.cryptodemo.data

import com.example.cryptodemo.model.Candle
import com.example.cryptodemo.model.MarketTicker
import com.example.cryptodemo.model.OrderBook
import com.example.cryptodemo.model.OrderBookEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BinanceMarketService {
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    fun observeTickerStream(): Flow<List<MarketTicker>> = callbackFlow {
        val wsUrl = "wss://stream.binance.com:9443/ws/!ticker@arr"
        val request = Request.Builder().url(wsUrl).build()

        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                trySend(emptyList())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val arr = JSONArray(text)
                    val result = mutableListOf<MarketTicker>()
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        val symbol = item.optString("s")
                        if (symbol.endsWith("USDT")) {
                            result.add(
                                MarketTicker(
                                    symbol = symbol,
                                    price = item.optString("c").toDoubleOrNull() ?: 0.0,
                                    percentChange = item.optString("P").toDoubleOrNull() ?: 0.0,
                                    volume = item.optString("q").toDoubleOrNull() ?: 0.0,
                                    high = item.optString("h").toDoubleOrNull() ?: 0.0,
                                    low = item.optString("l").toDoubleOrNull() ?: 0.0,
                                    updateTime = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    trySend(result)
                } catch (_: Throwable) {
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        })

        awaitClose { socket.close(1000, "Done") }
    }

    fun observeDepthStream(symbol: String): Flow<OrderBook> = callbackFlow {
        val wsSymbol = symbol.lowercase()
        val url = "wss://stream.binance.com:9443/ws/${wsSymbol}@depth20@100ms"
        val request = Request.Builder().url(url).build()

        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val obj = JSONObject(text)
                    val bids = mutableListOf<OrderBookEntry>()
                    val asks = mutableListOf<OrderBookEntry>()

                    val bidArray = obj.getJSONArray("b")
                    val askArray = obj.getJSONArray("a")
                    for (i in 0 until bidArray.length()) {
                        val row = bidArray.getJSONArray(i)
                        bids.add(OrderBookEntry(row.getDouble(0), row.getDouble(1)))
                    }
                    for (i in 0 until askArray.length()) {
                        val row = askArray.getJSONArray(i)
                        asks.add(OrderBookEntry(row.getDouble(0), row.getDouble(1)))
                    }
                    val bestBid = bids.firstOrNull()?.price ?: 0.0
                    val bestAsk = asks.firstOrNull()?.price ?: 0.0
                    trySend(OrderBook(bids = bids.sortedByDescending { it.price }, asks = asks.sortedBy { it.price }, spread = abs(bestAsk - bestBid)))
                } catch (_: Throwable) {
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        })

        awaitClose { socket.close(1000, "Closing depth stream") }
    }

    suspend fun fetchTicker(symbol: String): MarketTicker? {
        val url = "https://api.binance.com/api/v3/ticker/24hr?symbol=${symbol.uppercase()}"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null
        val body = response.body?.string() ?: return null
        val obj = JSONObject(body)
        return MarketTicker(
            symbol = symbol,
            price = obj.optString("lastPrice").toDoubleOrNull() ?: 0.0,
            percentChange = obj.optString("priceChangePercent").toDoubleOrNull() ?: 0.0,
            volume = obj.optString("quoteVolume").toDoubleOrNull() ?: 0.0,
            high = obj.optString("highPrice").toDoubleOrNull() ?: 0.0,
            low = obj.optString("lowPrice").toDoubleOrNull() ?: 0.0,
            updateTime = obj.optLong("closeTime", System.currentTimeMillis())
        )
    }

    suspend fun fetchCandles(symbol: String, interval: String = "1m", limit: Int = 60): List<Candle> {
        val url = "https://api.binance.com/api/v3/klines?symbol=${symbol.uppercase()}&interval=${interval}&limit=$limit"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()
        val body = response.body?.string() ?: return emptyList()
        val array = JSONArray(body)
        val candles = mutableListOf<Candle>()
        for (i in 0 until array.length()) {
            val item = array.getJSONArray(i)
            candles.add(
                Candle(
                    open = item.getDouble(1),
                    close = item.getDouble(4),
                    high = item.getDouble(2),
                    low = item.getDouble(3),
                    volume = item.getDouble(5),
                    timestamp = item.getLong(0)
                )
            )
        }
        return candles
    }

    suspend fun fetchDepth(symbol: String, limit: Int = 20): OrderBook {
        val url = "https://api.binance.com/api/v3/depth?symbol=${symbol.uppercase()}&limit=$limit"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return OrderBook()
        val body = response.body?.string() ?: return OrderBook()
        val obj = JSONObject(body)
        val bids = mutableListOf<OrderBookEntry>()
        val asks = mutableListOf<OrderBookEntry>()

        val bidArray = obj.getJSONArray("bids")
        val askArray = obj.getJSONArray("asks")
        for (i in 0 until bidArray.length()) {
            val row = bidArray.getJSONArray(i)
            bids.add(OrderBookEntry(row.getDouble(0), row.getDouble(1)))
        }
        for (i in 0 until askArray.length()) {
            val row = askArray.getJSONArray(i)
            asks.add(OrderBookEntry(row.getDouble(0), row.getDouble(1)))
        }
        val bestBid = bids.firstOrNull()?.price ?: 0.0
        val bestAsk = asks.firstOrNull()?.price ?: 0.0
        return OrderBook(bids = bids.sortedByDescending { it.price }, asks = asks.sortedBy { it.price }, spread = abs(bestAsk - bestBid))
    }
}

