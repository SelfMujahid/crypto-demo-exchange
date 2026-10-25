package com.example.cryptodemo

class DemoTradingEngine {
    fun calculateMargin(amount: Double, leverage: Double): Double {
        return if (leverage <= 0.0) amount else amount / leverage
    }

    fun feeForAmount(amount: Double): Double {
        return amount * 0.00075
    }

    fun updatePositionPnl(position: com.example.cryptodemo.model.PositionModel, currentPrice: Double): com.example.cryptodemo.model.PositionModel {
        val pnl = if (position.side == "BUY") {
            (currentPrice - position.entryPrice) * position.quantity
        } else {
            (position.entryPrice - currentPrice) * position.quantity
        }
        return position.copy(unrealizedPnl = pnl)
    }

    fun closePosition(position: com.example.cryptodemo.model.PositionModel, closingPrice: Double): com.example.cryptodemo.model.PositionModel {
        val realized = if (position.unrealizedPnl != 0.0) {
            position.unrealizedPnl
        } else if (position.side == "BUY") {
            (closingPrice - position.entryPrice) * position.quantity
        } else {
            (position.entryPrice - closingPrice) * position.quantity
        }
        return position.copy(realizedPnl = realized, unrealizedPnl = 0.0)
    }
}
