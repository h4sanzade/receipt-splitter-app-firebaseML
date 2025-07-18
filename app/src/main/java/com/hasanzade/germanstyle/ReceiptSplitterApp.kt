package com.hasanzade.germanstyle

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

    // Error Dialog
    ErrorDialog(
        errorMessage = state.errorMessage,
        onDismiss = viewModel::clearError
    )

    // Main Content
    when (state.currentStep) {
        Step.FRIENDS -> {
            FriendManagementScreen(
                friends = state.friends,
                onAddFriend = viewModel::addFriend,
                onRemoveFriend = viewModel::removeFriend,
                onNext = {
                    println("Camera button clicked - Going to CAPTURE step")
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
                        println("Image captured: $uri")
                        viewModel.processReceiptImage(uri)
                    },
                    onBack = { viewModel.goToStep(Step.FRIENDS) },
                    isProcessing = state.isProcessing,
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
                modifier = modifier
            )
        }

        Step.CALCULATE -> {
            ResultsScreen(
                personTotals = viewModel.calculateTotals(),
                onBack = { viewModel.goToStep(Step.ASSIGN) },
                onReset = viewModel::reset,
                onShare = {
                    // TODO: Implement sharing functionality
                },
                modifier = modifier
            )
        }
    }
}