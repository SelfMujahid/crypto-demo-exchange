package com.example.cryptodemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cryptodemo.data.TickerPrice

class MainActivity : ComponentActivity() {

    private val viewModel: PriceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PriceListScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceListScreen(viewModel: PriceViewModel) {
    val prices by viewModel.prices.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Crypto Demo Exchange") })

        if (prices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Connecting to live prices...")
            }
        } else {
            LazyColumn {
                items(prices) { ticker ->
                    PriceRow(ticker)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun PriceRow(ticker: TickerPrice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = ticker.symbol, style = MaterialTheme.typography.titleMedium)

        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(text = "$${ticker.price}", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${ticker.percentChange}%",
                color = if (ticker.percentChange >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
            )
        }
    }
}
