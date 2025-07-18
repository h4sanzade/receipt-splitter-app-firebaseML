package com.hasanzade.germanstyle.utils

import com.hasanzade.germanstyle.data.ReceiptItem

object ValidationUtils {

    fun isValidFriendName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2 && name.length <= 20
    }

    fun isValidReceiptItem(item: ReceiptItem): Boolean {
        return item.name.isNotBlank() &&
                item.quantity > 0 &&
                item.unitPrice >= 0 &&
                item.totalPrice >= 0
    }

    fun getFriendNameError(name: String): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < 2 -> "Name must be at least 2 characters"
            name.length > 20 -> "Name cannot exceed 20 characters"
            else -> null
        }
    }
}