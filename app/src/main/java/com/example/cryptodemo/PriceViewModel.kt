package com.example.cryptodemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptodemo.data.BinanceWebSocketClient
import com.example.cryptodemo.data.TickerPrice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PriceViewModel : ViewModel() {

    private val wsClient = BinanceWebSocketClient()

    private val _prices = MutableStateFlow<Map<String, TickerPrice>>(emptyMap())
    val prices: StateFlow<Map<String, TickerPrice>> = _prices.asStateFlow()

    init {
        viewModelScope.launch {
            wsClient.observePrices().collect { updates ->
                val current = _prices.value.toMutableMap()
                updates.forEach { ticker ->
                    current[ticker.symbol] = ticker
                }
                _prices.value = current
            }
        }
    }
}
