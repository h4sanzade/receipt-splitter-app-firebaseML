package com.hasanzade.germanstyle.utils

object FormatUtils {

    fun formatCurrency(amount: Double): String {
        return String.format("₼%.2f", amount)
    }

    fun formatQuantity(quantity: Int): String {
        return if (quantity == 1) "1 item" else "$quantity items"
    }

    fun formatItemDetails(quantity: Int, unitPrice: Double, totalPrice: Double): String {
        return "Qty: $quantity × ${formatCurrency(unitPrice)} = ${formatCurrency(totalPrice)}"
    }

    fun formatSplitInfo(friendCount: Int, totalPrice: Double): String {
        return if (friendCount > 0) {
            "Split $friendCount ways: ${formatCurrency(totalPrice / friendCount)} each"
        } else {
            "Not assigned"
        }
    }
}