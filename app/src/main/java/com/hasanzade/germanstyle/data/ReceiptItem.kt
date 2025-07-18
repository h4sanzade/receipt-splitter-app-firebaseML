package com.hasanzade.germanstyle.data


data class ReceiptItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
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
            totalPrice / assignedFriends.size
        } else {
            0.0
        }
    }
}