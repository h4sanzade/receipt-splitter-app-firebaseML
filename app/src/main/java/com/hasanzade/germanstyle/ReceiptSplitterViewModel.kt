package com.hasanzade.germanstyle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hasanzade.germanstyle.data.AppState
import com.hasanzade.germanstyle.data.PersonTotal
import com.hasanzade.germanstyle.data.Step
import com.hasanzade.germanstyle.ml.FirebaseMLKitService
import com.hasanzade.germanstyle.parser.ReceiptTextParser

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiptSplitterViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val mlKitService = FirebaseMLKitService(application.applicationContext)
    private val textParser = ReceiptTextParser()

    // Friend Management
    fun addFriend(name: String) {
        if (name.isNotBlank() && !_state.value.friends.contains(name)) {
            _state.value.friends.add(name)
            updateState()
        }
    }

    fun removeFriend(name: String) {
        _state.value.friends.remove(name)
        // Remove friend from all item assignments
        _state.value.receiptItems.forEach { item ->
            item.assignedFriends.remove(name)
        }
        updateState()
    }

    // Receipt Processing
    fun processReceiptImage(imageUri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true, errorMessage = null)

            mlKitService.extractTextFromImage(imageUri)
                .onSuccess { extractedText ->
                    if (extractedText.isBlank()) {
                        _state.value = _state.value.copy(
                            isProcessing = false,
                            errorMessage = "No text found in the image. Please try again with a clearer receipt."
                        )
                        return@onSuccess
                    }

                    val items = textParser.parseReceiptText(extractedText)
                    _state.value.receiptItems.clear()
                    _state.value.receiptItems.addAll(items)

                    _state.value = _state.value.copy(
                        isProcessing = false,
                        currentStep = if (items.isNotEmpty()) Step.ASSIGN else Step.CAPTURE,
                        errorMessage = if (items.isEmpty()) "No receipt items found. Please try again or check if the receipt is clearly visible." else null
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        errorMessage = "Error processing image: ${error.message ?: "Unknown error occurred"}"
                    )
                }
        }
    }

    // Item Assignment
    fun toggleFriendAssignment(itemId: String, friendName: String) {
        val item = _state.value.receiptItems.find { it.id == itemId }
        item?.toggleFriendAssignment(friendName)
        updateState()
    }

    // Calculations
    fun calculateTotals(): List<PersonTotal> {
        val totals = mutableMapOf<String, Double>()

        _state.value.receiptItems.forEach { item ->
            item.assignedFriends.forEach { friend ->
                val currentAmount = totals.getOrDefault(friend, 0.0)
                totals[friend] = currentAmount + item.getAmountPerPerson()
            }
        }

        return totals.map { (name, amount) ->
            PersonTotal(name, amount)
        }.sortedBy { it.name }
    }

    fun getReceiptTotal(): Double {
        return _state.value.receiptItems.sumOf { it.totalPrice }
    }

    fun getAssignedTotal(): Double {
        return _state.value.receiptItems
            .filter { it.assignedFriends.isNotEmpty() }
            .sumOf { it.totalPrice }
    }

    // Navigation
    fun goToStep(step: Step) {
        _state.value = _state.value.copy(currentStep = step)
    }

    fun goToNextStep() {
        val nextStep = when (_state.value.currentStep) {
            Step.FRIENDS -> Step.CAPTURE
            Step.CAPTURE -> Step.ASSIGN
            Step.ASSIGN -> Step.CALCULATE
            Step.CALCULATE -> Step.FRIENDS
        }
        goToStep(nextStep)
    }

    fun goToPreviousStep() {
        val previousStep = when (_state.value.currentStep) {
            Step.FRIENDS -> Step.FRIENDS
            Step.CAPTURE -> Step.FRIENDS
            Step.ASSIGN -> Step.CAPTURE
            Step.CALCULATE -> Step.ASSIGN
        }
        goToStep(previousStep)
    }

    // Reset
    fun reset() {
        _state.value = AppState()
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun updateState() {
        _state.value = _state.value.copy()
    }

    override fun onCleared() {
        super.onCleared()
        mlKitService.closeRecognizer()
    }
}
