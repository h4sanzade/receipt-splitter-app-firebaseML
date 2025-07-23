package com.hasanzade.germanstyle.data

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptData(
    val items: List<ReceiptItemData> = emptyList(),
    val total_amount: Double = 0.0,
    val tax: Double = 0.0,
    val currency: String = "AZN",
    val merchant: String = "",
    val date: String = ""
)

@Serializable
data class ReceiptItemData(
    val name: String,
    val quantity: Int = 1,
    val unit_price: Double,
    val total_price: Double
)