package com.example.cryptodemo.data

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import org.json.JSONArray

data class TickerPrice(
    val symbol: String,
    val price: Double,
    val percentChange: Double
)

class BinanceWebSocketClient {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    // Yeh streams Binance ke "24hr mini ticker" hain — multiple coins ek saath
    private val symbols = listOf("btcusdt", "ethusdt", "bnbusdt", "solusdt", "xrpusdt")

    fun observePrices(): Flow<List<TickerPrice>> = callbackFlow {
        val streams = symbols.joinToString("/") { "$it@ticker" }
        val url = "wss://stream.binance.com:9443/stream?streams=$streams"

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Connection ho gaya, ab data aana shuru hoga
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = org.json.JSONObject(text)
                    val data = json.getJSONObject("data")
                    val symbol = data.getString("s")
                    val price = data.getString("c").toDouble()
                    val percentChange = data.getString("P").toDouble()

                    trySend(listOf(TickerPrice(symbol, price, percentChange)))
                } catch (e: Exception) {
                    // Malformed message, ignore
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        })

        awaitClose {
            webSocket?.close(1000, "Closing")
        }
    }
}
