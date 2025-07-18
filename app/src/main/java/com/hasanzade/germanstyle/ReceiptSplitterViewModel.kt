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

    // Friend Management - FIXED
    fun addFriend(name: String) {
        if (name.isNotBlank() && !_state.value.friends.contains(name)) {
            val currentState = _state.value
            val newFriends = currentState.friends.toMutableList()
            newFriends.add(name)

            _state.value = currentState.copy(friends = newFriends)
            println("Friend added: $name, Total friends: ${_state.value.friends.size}")
        }
    }

    fun removeFriend(name: String) {
        val currentState = _state.value
        val newFriends = currentState.friends.toMutableList()
        newFriends.remove(name)

        // Remove friend from all item assignments
        val updatedItems = currentState.receiptItems.map { item ->
            item.copy(assignedFriends = item.assignedFriends.filter { it != name }.toMutableList())
        }.toMutableList()

        _state.value = currentState.copy(
            friends = newFriends,
            receiptItems = updatedItems
        )
        println("Friend removed: $name, Total friends: ${_state.value.friends.size}")
    }

    // Receipt Processing - ENHANCED
    fun processReceiptImage(imageUri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true, errorMessage = null)
            println("Processing image: $imageUri")

            mlKitService.extractTextFromImage(imageUri)
                .onSuccess { extractedText ->
                    println("Extracted text: $extractedText")

                    if (extractedText.isBlank()) {
                        _state.value = _state.value.copy(
                            isProcessing = false,
                            errorMessage = "No text found in the image. Please try again with a clearer receipt."
                        )
                        return@onSuccess
                    }

                    val items = textParser.parseReceiptText(extractedText)
                    println("Parsed items count: ${items.size}")
                    items.forEach { item ->
                        println("Item: ${item.name}, Price: ${item.totalPrice}")
                    }

                    val currentState = _state.value
                    _state.value = currentState.copy(
                        receiptItems = items.toMutableList(),
                        isProcessing = false,
                        currentStep = if (items.isNotEmpty()) Step.ASSIGN else Step.CAPTURE,
                        errorMessage = if (items.isEmpty()) {
                            "No receipt items found. Try these tips:\n" +
                                    "• Ensure good lighting\n" +
                                    "• Hold camera steady\n" +
                                    "• Make sure text is clear and readable\n" +
                                    "• Try a closer shot of the itemized section"
                        } else null
                    )
                }
                .onFailure { error ->
                    println("ML Kit error: ${error.message}")
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        errorMessage = "Error processing image: ${error.message ?: "Unknown error occurred"}"
                    )
                }
        }
    }

    // Item Assignment
    fun toggleFriendAssignment(itemId: String, friendName: String) {
        val currentState = _state.value
        val updatedItems = currentState.receiptItems.map { item ->
            if (item.id == itemId) {
                item.copy().also { it.toggleFriendAssignment(friendName) }
            } else item
        }.toMutableList()

        _state.value = currentState.copy(receiptItems = updatedItems)
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
        println("Navigating to step: $step")
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

    override fun onCleared() {
        super.onCleared()
        mlKitService.closeRecognizer()
    }
}