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

    private val _prices = MutableStateFlow<List<TickerPrice>>(emptyList())
    val prices: StateFlow<List<TickerPrice>> = _prices.asStateFlow()

    init {
        viewModelScope.launch {
            wsClient.observeAllPrices().collect { updates ->
                // Alphabetically sorted rakhte hain taaki list mein position stable rahe
                _prices.value = updates.sortedBy { it.symbol }
            }
        }
    }
}
