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
import java.util.concurrent.TimeUnit

data class TickerPrice(
    val symbol: String,
    val price: Double,
    val percentChange: Double
)

class BinanceWebSocketClient {

    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    // "!ticker@arr" = Binance ke saare trading pairs ka live 24hr ticker, ek hi stream mein
    fun observeAllPrices(): Flow<List<TickerPrice>> = callbackFlow {
        val url = "wss://stream.binance.com:9443/ws/!ticker@arr"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Connected — data ab continuously aayega
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val jsonArray = JSONArray(text)
                    val list = mutableListOf<TickerPrice>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val symbol = item.getString("s")
                        // Sirf USDT pairs rakhte hain — cleaner list ke liye
                        if (symbol.endsWith("USDT")) {
                            val price = item.getString("c").toDoubleOrNull() ?: continue
                            val percentChange = item.getString("P").toDoubleOrNull() ?: 0.0
                            list.add(TickerPrice(symbol, price, percentChange))
                        }
                    }
                    trySend(list)
                } catch (e: Exception) {
                    // Malformed message, ignore aur agla message wait karo
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

    private fun String.toDoubleOrNull(): Double? = try {
        this.toDouble()
    } catch (e: NumberFormatException) {
        null
    }
}
