package com.example.cryptodemo

import com.example.cryptodemo.model.PositionModel
import org.junit.Assert.assertEquals
import org.junit.Test

class DemoTradingEngineTest {
    private val engine = DemoTradingEngine()

    @Test
    fun `margin should be amount divided by leverage`() {
        val margin = engine.calculateMargin(1000.0, 10.0)
        assertEquals(100.0, margin, 0.0001)
    }

    @Test
    fun `fee should be positive and reasonable`() {
        val fee = engine.feeForAmount(500.0)
        assertEquals(0.375, fee, 0.0001)
    }

    @Test
    fun `buy position pnl calculation should increase with price rise`() {
        val position = PositionModel(
            id = "1",
            pair = "BTCUSDT",
            side = "BUY",
            leverage = 10.0,
            marginMode = "CROSS",
            entryPrice = 10000.0,
            quantity = 1.0,
            value = 10000.0,
            margin = 1000.0,
            liquidationPrice = 8000.0,
            unrealizedPnl = 0.0,
            realizedPnl = 0.0
        )
        val updated = engine.updatePositionPnl(position, 10500.0)
        assertEquals(500.0, updated.unrealizedPnl, 0.0001)
    }

    @Test
    fun `closing position should return realized pnl`() {
        val position = PositionModel(
            id = "1",
            pair = "ETHUSDT",
            side = "SELL",
            leverage = 5.0,
            marginMode = "ISOLATED",
            entryPrice = 3000.0,
            quantity = 2.0,
            value = 6000.0,
            margin = 1200.0,
            liquidationPrice = 4500.0,
            unrealizedPnl = 500.0,
            realizedPnl = 0.0
        )
        val closed = engine.closePosition(position, 1.0)
        assertEquals(500.0, closed.realizedPnl, 0.0001)
    }
}
