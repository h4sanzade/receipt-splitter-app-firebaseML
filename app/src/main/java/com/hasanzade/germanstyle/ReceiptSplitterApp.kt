package com.hasanzade.germanstyle

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hasanzade.germanstyle.components.*
import com.hasanzade.germanstyle.data.Step

@Composable
fun ReceiptSplitterApp(
    viewModel: ReceiptSplitterViewModel,
    hasCameraPermission: Boolean,
    onRequestCameraPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ErrorDialog(
        errorMessage = state.errorMessage,
        onDismiss = viewModel::clearError
    )

    when (state.currentStep) {
        Step.FRIENDS -> {
            FriendManagementScreen(
                friends = state.friends,
                onAddFriend = viewModel::addFriend,
                onRemoveFriend = viewModel::removeFriend,
                onNext = {
                    Log.d("ReceiptSplitterApp", "AI Scanner button clicked - Going to CAPTURE step")
                    viewModel.goToStep(Step.CAPTURE)
                },
                modifier = modifier
            )
        }

        Step.CAPTURE -> {
            if (!hasCameraPermission) {
                PermissionDeniedScreen(
                    onRequestPermission = onRequestCameraPermission,
                    onBack = { viewModel.goToStep(Step.FRIENDS) },
                    modifier = modifier
                )
            } else {
                CameraScreen(
                    onImageCaptured = { uri ->
                        Log.d("ReceiptSplitterApp", "Image captured for Gemini AI: $uri")
                        viewModel.processReceiptImage(uri)
                    },
                    onBack = { viewModel.goToStep(Step.FRIENDS) },
                    isProcessing = state.isProcessing,
                    processingStatus = state.processingStatus,
                    modifier = modifier
                )
            }
        }

        Step.ASSIGN -> {
            AssignmentScreen(
                items = state.receiptItems,
                friends = state.friends,
                onToggleAssignment = viewModel::toggleFriendAssignment,
                onCalculate = { viewModel.goToStep(Step.CALCULATE) },
                onBack = { viewModel.goToStep(Step.CAPTURE) },
            )
        }

        Step.CALCULATE -> {
            ResultsScreen(
                personTotals = viewModel.calculateTotals(),
                onBack = { viewModel.goToStep(Step.ASSIGN) },
                onReset = viewModel::reset,
                onShare = {
                },
                modifier = modifier
            )
        }
    }
}