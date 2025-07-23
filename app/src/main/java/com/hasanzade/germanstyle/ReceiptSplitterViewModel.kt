package com.hasanzade.germanstyle

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hasanzade.germanstyle.ai.GeminiService
import com.hasanzade.germanstyle.data.AppState
import com.hasanzade.germanstyle.data.PersonTotal
import com.hasanzade.germanstyle.data.Step
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiptSplitterViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ReceiptSplitterViewModel"
    }

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val geminiService = GeminiService(application.applicationContext)

    fun addFriend(name: String) {
        if (name.isNotBlank() && !_state.value.friends.contains(name)) {
            val currentState = _state.value
            val newFriends = currentState.friends.toMutableList()
            newFriends.add(name)

            _state.value = currentState.copy(friends = newFriends)
            Log.d(TAG, "Friend added: $name, Total friends: ${_state.value.friends.size}")
        }
    }

    fun removeFriend(name: String) {
        val currentState = _state.value
        val newFriends = currentState.friends.toMutableList()
        newFriends.remove(name)

        val updatedItems = currentState.receiptItems.map { item ->
            item.copy(assignedFriends = item.assignedFriends.filter { it != name }.toMutableList())
        }.toMutableList()

        _state.value = currentState.copy(
            friends = newFriends,
            receiptItems = updatedItems
        )
        Log.d(TAG, "Friend removed: $name, Total friends: ${_state.value.friends.size}")
    }

    fun processReceiptImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isProcessing = true,
                    errorMessage = null,
                    processingStatus = "Analyzing receipt with Gemini AI..."
                )

                Log.d(TAG, "Processing image with Gemini: $imageUri")

                _state.value = _state.value.copy(
                    processingStatus = "Processing image..."
                )

                geminiService.extractReceiptData(imageUri)
                    .onSuccess { items ->
                        Log.d(TAG, "Gemini successfully extracted ${items.size} items")

                        _state.value = _state.value.copy(
                            processingStatus = "Analysis completed!"
                        )

                        if (items.isNotEmpty()) {
                            items.forEach { item ->
                                Log.d(TAG, "Item: ${item.name}, Qty: ${item.quantity}, Unit: ${item.unit_price} AZN, Total: ${item.total_price} AZN")
                            }

                            _state.value = _state.value.copy(
                                receiptItems = items.toMutableList(),
                                isProcessing = false,
                                currentStep = Step.ASSIGN,
                                processingStatus = ""
                            )
                        } else {
                            _state.value = _state.value.copy(
                                isProcessing = false,
                                processingStatus = "",
                                errorMessage = "Gemini AI couldn't find any items in the receipt. Please try:\n" +
                                        "• Better lighting\n" +
                                        "• Keep camera steady\n" +
                                        "• Ensure entire receipt is in frame\n" +
                                        "• Take closer shot of items section"
                            )
                        }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Gemini processing failed", error)
                        _state.value = _state.value.copy(
                            isProcessing = false,
                            processingStatus = "",
                            errorMessage = "Error processing with Gemini AI: ${error.message ?: "Unknown error"}\n\n" +
                                    "Please check:\n" +
                                    "• Internet connection\n" +
                                    "• Image quality\n" +
                                    "• API key configuration"
                        )
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in processReceiptImage", e)
                _state.value = _state.value.copy(
                    isProcessing = false,
                    processingStatus = "",
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    fun toggleFriendAssignment(itemId: String, friendName: String) {
        val currentState = _state.value
        val updatedItems = currentState.receiptItems.map { item ->
            if (item.id == itemId) {
                item.copy().also { it.toggleFriendAssignment(friendName) }
            } else item
        }.toMutableList()

        _state.value = currentState.copy(receiptItems = updatedItems)
        Log.d(TAG, "Toggled assignment: $friendName to item $itemId")
    }

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
        return _state.value.receiptItems.sumOf { it.total_price }
    }

    fun getAssignedTotal(): Double {
        return _state.value.receiptItems
            .filter { it.assignedFriends.isNotEmpty() }
            .sumOf { it.total_price }
    }

    fun goToStep(step: Step) {
        _state.value = _state.value.copy(currentStep = step)
        Log.d(TAG, "Navigating to step: $step")
    }

    fun reset() {
        _state.value = AppState()
        Log.d(TAG, "App state reset")
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}