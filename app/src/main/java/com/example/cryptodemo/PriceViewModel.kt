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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                wsClient.observeAllPrices().collect { updates ->
                    _prices.value = updates.sortedBy { it.symbol }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown connection error"
            }
        }
    }
}
