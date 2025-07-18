package com.hasanzade.germanstyle.calculation

import com.hasanzade.germanstyle.data.PersonTotal
import com.hasanzade.germanstyle.data.ReceiptItem

class CalculationManager {

    fun calculatePersonTotals(items: List<ReceiptItem>): List<PersonTotal> {
        val totals = mutableMapOf<String, Double>()

        items.forEach { item ->
            if (item.assignedFriends.isNotEmpty()) {
                val amountPerPerson = item.totalPrice / item.assignedFriends.size
                item.assignedFriends.forEach { friend ->
                    totals[friend] = totals.getOrDefault(friend, 0.0) + amountPerPerson
                }
            }
        }

        return totals.map { (name, amount) ->
            PersonTotal(name, amount)
        }.sortedBy { it.name }
    }

    fun getReceiptSummary(items: List<ReceiptItem>): ReceiptSummary {
        val totalAmount = items.sumOf { it.totalPrice }
        val assignedAmount = items.filter { it.assignedFriends.isNotEmpty() }.sumOf { it.totalPrice }
        val unassignedAmount = totalAmount - assignedAmount
        val totalItems = items.size
        val assignedItems = items.count { it.assignedFriends.isNotEmpty() }

        return ReceiptSummary(
            totalAmount = totalAmount,
            assignedAmount = assignedAmount,
            unassignedAmount = unassignedAmount,
            totalItems = totalItems,
            assignedItems = assignedItems
        )
    }
}

data class ReceiptSummary(
    val totalAmount: Double,
    val assignedAmount: Double,
    val unassignedAmount: Double,
    val totalItems: Int,
    val assignedItems: Int
)