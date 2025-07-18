package com.hasanzade.germanstyle.data

data class AppState(
    val friends: MutableList<String> = mutableListOf(),
    val receiptItems: MutableList<ReceiptItem> = mutableListOf(),
    val isProcessing: Boolean = false,
    val currentStep: Step = Step.FRIENDS,
    val errorMessage: String? = null
)
enum class Step {
    FRIENDS,
    CAPTURE,
    ASSIGN,
    CALCULATE
}