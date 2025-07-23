package com.hasanzade.germanstyle.data

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val quantity: Int = 1,
    val unit_price: Double,
    val total_price: Double,
    val assignedFriends: MutableList<String> = mutableListOf()
) {
    fun isAssignedTo(friendName: String): Boolean {
        return assignedFriends.contains(friendName)
    }

    fun toggleFriendAssignment(friendName: String) {
        if (isAssignedTo(friendName)) {
            assignedFriends.remove(friendName)
        } else {
            assignedFriends.add(friendName)
        }
    }

    fun getAmountPerPerson(): Double {
        return if (assignedFriends.isNotEmpty()) {
            total_price / assignedFriends.size
        } else {
            0.0
        }
    }
}