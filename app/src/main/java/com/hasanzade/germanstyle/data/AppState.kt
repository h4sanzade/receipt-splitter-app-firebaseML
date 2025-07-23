package com.hasanzade.germanstyle.data

data class AppState(
    val friends: List<String> = emptyList(),
    val receiptItems: List<ReceiptItem> = emptyList(),
    val isProcessing: Boolean = false,
    val currentStep: Step = Step.FRIENDS,
    val errorMessage: String? = null,
    val processingStatus: String = ""
)

enum class Step {
    FRIENDS,
    CAPTURE,
    ASSIGN,
    CALCULATE
}