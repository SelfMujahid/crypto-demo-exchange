package com.example.cryptodemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptodemo.data.BinanceMarketService
import com.example.cryptodemo.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptoDemoApp()
        }
    }
}

@Composable
fun CryptoDemoApp(viewModel: TradingViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val nav = remember { mutableStateOf(0) }

    AppTheme {
        Scaffold(
            containerColor = Color(0xFF0B1220),
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF121B2D)) {
                    listOf(
                        "Home" to Icons.Filled.Home,
                        "Markets" to Icons.Filled.TrendingUp,
                        "Trade" to Icons.Filled.SwapHoriz,
                        "Assets" to Icons.Filled.AccountBalanceWallet,
                        "Settings" to Icons.Filled.Settings
                    ).forEachIndexed { index, (label, icon) ->
                        NavigationBarItem(
                            selected = nav.value == index,
                            onClick = { nav.value = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFEDC967),
                                selectedTextColor = Color(0xFFEDC967),
                                unselectedIconColor = Color(0xFF8AA0C8),
                                unselectedTextColor = Color(0xFF8AA0C8),
                                indicatorColor = Color(0xFF17253D)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (nav.value) {
                0 -> HomeScreen(uiState, viewModel, Modifier.padding(padding))
                1 -> MarketsScreen(uiState, viewModel, Modifier.padding(padding))
                2 -> TradeScreen(uiState, viewModel, Modifier.padding(padding))
                3 -> AssetsScreen(uiState, viewModel, Modifier.padding(padding))
                else -> SettingsScreen(uiState, viewModel, Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun HomeScreen(uiState: TradingUiState, viewModel: TradingViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Futures", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF9FB2D7))
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = Color(0xFF9FB2D7))
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF111B2D))
                    .border(1.dp, Color(0xFF1B2A42), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(uiState.selectedPair, style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Surface(color = if (uiState.marketChange >= 0) Color(0xFF16A34A) else Color(0xFFDC2626), shape = RoundedCornerShape(20.dp)) {
                        Text("${uiState.marketChange.formatPercent()}%", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("$${uiState.currentPrice.formatPrice()}", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoValue("24H High", uiState.marketHigh.formatPrice())
                    InfoValue("24H Low", uiState.marketLow.formatPrice())
                    InfoValue("Funding", uiState.fundingRate.formatPercent())
                }
            }
        }

        item {
            Text("Watchlist", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        items(uiState.watchlist) { ticker ->
            MarketRow(ticker = ticker, selected = ticker.symbol == uiState.selectedPair) {
                viewModel.selectPair(ticker.symbol)
            }
        }
    }
}

@Composable
fun MarketsScreen(uiState: TradingUiState, viewModel: TradingViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Markets", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        item {
            OutlinedTextField(
                value = uiState.searchText,
                onValueChange = viewModel::setSearchText,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search pair") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF22324F),
                    focusedBorderColor = Color(0xFFEDC967),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedPlaceholderColor = Color(0xFF7D8FB6),
                    focusedPlaceholderColor = Color(0xFF7D8FB6)
                ),
                singleLine = true
            )
        }

        items(uiState.filteredWatchlist) { ticker ->
            MarketRow(ticker, selected = ticker.symbol == uiState.selectedPair) {
                viewModel.selectPair(ticker.symbol)
            }
        }
    }
}

@Composable
fun TradeScreen(uiState: TradingUiState, viewModel: TradingViewModel, modifier: Modifier = Modifier) {
    val orderTab = remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(12.dp)
    ) {
        HeaderBar(uiState, viewModel)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val boxModifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF121B2D))
                .border(1.dp, Color(0xFF1A2940), RoundedCornerShape(14.dp))
                .padding(10.dp)

            Column(boxModifier) {
                Text("Candles", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                PriceChart(uiState.candles)
            }

            Column(
                modifier = boxModifier
                    .widthIn(min = 180.dp)
            ) {
                Text("Order Book", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OrderBookPanel(uiState.orderBook)
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2D)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Order Entry", color = Color.White, fontWeight = FontWeight.Bold)
                    SegmentedControl(orderTab)
                }
                Spacer(Modifier.height(12.dp))

                OrderForm(
                    uiState = uiState,
                    onLong = { viewModel.placeOrder(side = "BUY", amountUsd = uiState.orderAmount, leverage = uiState.leverage, marginMode = uiState.marginMode) },
                    onShort = { viewModel.placeOrder(side = "SELL", amountUsd = uiState.orderAmount, leverage = uiState.leverage, marginMode = uiState.marginMode) },
                    onAmountChange = viewModel::setOrderAmount,
                    onLeverageChange = viewModel::setLeverage,
                    onMarginModeChanged = viewModel::setMarginMode,
                    onOrderTypeChanged = viewModel::setOrderType,
                    onLimitPriceChanged = viewModel::setLimitPrice,
                    onStopLossChanged = viewModel::setStopLoss,
                    onTakeProfitChanged = viewModel::setTakeProfit
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        PositionsPanel(uiState, viewModel)
    }
}

@Composable
fun HeaderBar(uiState: TradingUiState, viewModel: TradingViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101A2C)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(uiState.selectedPair, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${uiState.currentPrice.formatPrice()} • ${uiState.marketChange.formatPercent()}%", color = if (uiState.marketChange >= 0) Color(0xFF16A34A) else Color(0xFFDC2626))
            }
            Button(
                onClick = { viewModel.resetDemoAccount() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDC967), contentColor = Color(0xFF0B1220))
            ) {
                Text("Reset Demo")
            }
        }
    }
}

@Composable
fun SegmentedControl(orderTab: MutableState<Int>) {
    val values = listOf("Limit", "Market")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0D1628))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        values.forEachIndexed { index, label ->
            val selected = orderTab.value == index
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) Color(0xFFEDC967) else Color.Transparent)
                    .clickable { orderTab.value = index }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (selected) Color(0xFF0B1220) else Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun OrderForm(
    uiState: TradingUiState,
    onLong: () -> Unit,
    onShort: () -> Unit,
    onAmountChange: (String) -> Unit,
    onLeverageChange: (String) -> Unit,
    onMarginModeChanged: (String) -> Unit,
    onOrderTypeChanged: (String) -> Unit,
    onLimitPriceChanged: (String) -> Unit,
    onStopLossChanged: (String) -> Unit,
    onTakeProfitChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = onLong,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF16A34A), contentColor = Color.White),
                modifier = Modifier.weight(1f)
            ) { Text("Buy / Long") }
            FilledTonalButton(
                onClick = onShort,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                modifier = Modifier.weight(1f)
            ) { Text("Sell / Short") }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Amount (USDT)")
            BasicTextField(
                value = uiState.orderAmountText,
                onValueChange = onAmountChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B1220))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = uiState.marginMode == "CROSS", onClick = { onMarginModeChanged("CROSS") }, label = { Text("Cross") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFEDC967), selectedLabelColor = Color(0xFF0B1220), labelColor = Color.White))
            FilterChip(selected = uiState.marginMode == "ISOLATED", onClick = { onMarginModeChanged("ISOLATED") }, label = { Text("Isolated") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFEDC967), selectedLabelColor = Color(0xFF0B1220), labelColor = Color.White))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Leverage")
            BasicTextField(
                value = uiState.leverageText,
                onValueChange = onLeverageChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B1220))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Order Type")
            DropdownMenuItem(label = { Text(uiState.orderType) }, onClick = { onOrderTypeChanged("MARKET") })
        }

        val limitPrice = uiState.limitPriceText
        BasicTextField(
            value = limitPrice,
            onValueChange = onLimitPriceChanged,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0B1220))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (limitPrice.isEmpty()) Text("Limit price")
                    innerTextField()
                }
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallInput(label = "SL", value = uiState.stopLossText, onValueChange = onStopLossChanged)
            SmallInput(label = "TP", value = uiState.takeProfitText, onValueChange = onTakeProfitChanged)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoValue("Est Margin", uiState.estimatedMargin.formatPrice())
            InfoValue("Fee", uiState.estimatedFee.formatPrice())
            InfoValue("Notional", uiState.orderAmount.formatPrice())
        }
    }
}

@Composable
fun SmallInput(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.weight(1f)) {
        Text(label, color = Color(0xFF8AA0C8), fontSize = 12.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0B1220))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.White)
        )
    }
}

@Composable
fun AssetsScreen(uiState: TradingUiState, viewModel: TradingViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Assets", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2D)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Demo Wallet", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text("Available USDT: ${uiState.demoBalance.formatPrice()}", color = Color.White)
                    Text("Used Margin: ${uiState.usedMargin.formatPrice()}", color = Color(0xFF8AA0C8))
                    Text("Unrealized PnL: ${uiState.unrealizedPnl.formatPrice()}", color = if (uiState.unrealizedPnl >= 0) Color(0xFF16A34A) else Color(0xFFDC2626))
                    Text("Realized PnL: ${uiState.realizedPnl.formatPrice()}", color = if (uiState.realizedPnl >= 0) Color(0xFF16A34A) else Color(0xFFDC2626))
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.resetDemoAccount() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDC967), contentColor = Color(0xFF0B1220)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Demo Balance")
            }
        }
    }
}

@Composable
fun SettingsScreen(uiState: TradingUiState, viewModel: TradingViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2D)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Connection", color = Color.White, fontWeight = FontWeight.Bold)
                Text(uiState.connectionStatus, color = Color(0xFFEDC967))
                Text("Selected pair: ${uiState.selectedPair}", color = Color(0xFF8AA0C8))
                Text("Last update: ${uiState.lastUpdateDisplay}", color = Color(0xFF8AA0C8))
            }
        }
    }
}

@Composable
fun PositionsPanel(uiState: TradingUiState, viewModel: TradingViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Positions & Orders", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (uiState.positions.isEmpty()) {
                Text("No open positions", color = Color(0xFF8AA0C8))
            } else {
                uiState.positions.forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(p.pair, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${p.side} • ${p.leverage}x • ${p.marginMode}", color = Color(0xFF8AA0C8))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${p.unrealizedPnl.formatPrice()}", color = if (p.unrealizedPnl >= 0) Color(0xFF16A34A) else Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            Text("Qty ${p.quantity}", color = Color(0xFF8AA0C8))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketRow(ticker: MarketTicker, selected: Boolean, onClick: () -> Unit) {
    val green = ticker.percentChange >= 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0xFF1A2940) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(ticker.symbol, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Vol ${ticker.volume.formatPrice()}", color = Color(0xFF8AA0C8), fontSize = 12.sp)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("$${ticker.price.formatPrice()}", color = Color.White)
            Text("${ticker.percentChange.formatPercent()}%", color = if (green) Color(0xFF16A34A) else Color(0xFFDC2626), fontSize = 12.sp)
        }
    }
}

@Composable
fun OrderBookPanel(orderBook: OrderBook) {
    val asks = orderBook.asks.take(5)
    val bids = orderBook.bids.take(5)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        bids.forEach { entry ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${entry.price.formatPrice()}", color = Color(0xFF16A34A), fontSize = 12.sp)
                Text(entry.quantity.formatPrice(), color = Color(0xFF8AA0C8), fontSize = 12.sp)
            }
        }
        Divider(color = Color(0xFF1A2940), modifier = Modifier.padding(vertical = 4.dp))
        asks.forEach { entry ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${entry.price.formatPrice()}", color = Color(0xFFDC2626), fontSize = 12.sp)
                Text(entry.quantity.formatPrice(), color = Color(0xFF8AA0C8), fontSize = 12.sp)
            }
        }
        val spread = orderBook.spread
        Text("Spread: ${spread.formatPrice()}", color = Color(0xFFEDC967), fontSize = 12.sp)
    }
}

@Composable
fun PriceChart(candles: List<Candle>) {
    val visible = candles.takeLast(30)
    if (visible.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading chart", color = Color(0xFF8AA0C8))
        }
        return
    }

    val low = visible.minOf { it.low }
    val high = visible.maxOf { it.high }
    val span = if (high > low) high - low else 1.0
    val mid = visible.last().close

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(Unit) {
                detectTapGestures { }
            }
    ) {
        val width = size.width
        val height = size.height
        val step = width / visible.size

        visible.forEachIndexed { index, candle ->
            val x = index * step + step * 0.5f
            val top = height - ((candle.high - low) / span * (height - 12f)) - 6f
            val bottom = height - ((candle.low - low) / span * (height - 12f)) - 6f
            val openY = height - ((candle.open - low) / span * (height - 12f)) - 6f
            val closeY = height - ((candle.close - low) / span * (height - 12f)) - 6f
            val stickColor = if (candle.close >= candle.open) Color(0xFF16A34A) else Color(0xFFDC2626)
            drawLine(
                color = stickColor,
                start = Offset(x, top),
                end = Offset(x, bottom),
                strokeWidth = 2f
            )
            val rectTop = minOf(openY, closeY)
            val rectBottom = maxOf(openY, closeY)
            drawRect(
                color = stickColor,
                topLeft = Offset(x - 3f, rectTop),
                size = androidx.compose.ui.geometry.Size(6f, maxOf(rectBottom - rectTop, 2f))
            )
        }
    }
}

@Composable
fun InfoValue(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFF8AA0C8), fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFEDC967),
            secondary = Color(0xFF16A34A),
            background = Color(0xFF0B1220),
            surface = Color(0xFF101A2C)
        ),
        typography = Typography(),
        content = content
    )
}

fun Double.formatPrice(): String = if (this == 0.0) "0.00" else BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toPlainString()
fun Double.formatPercent(): String = BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toPlainString()

