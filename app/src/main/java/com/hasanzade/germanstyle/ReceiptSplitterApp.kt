package com.hasanzade.germanstyle

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hasanzade.germanstyle.components.AssignmentScreen
import com.hasanzade.germanstyle.components.CameraScreen
import com.hasanzade.germanstyle.components.ErrorDialog
import com.hasanzade.germanstyle.components.FriendManagementScreen
import com.hasanzade.germanstyle.components.ResultsScreen
import com.hasanzade.germanstyle.data.Step


@Composable
fun ReceiptSplitterApp(
    viewModel: ReceiptSplitterViewModel,
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
                onNext = { viewModel.goToStep(Step.CAPTURE) },
                modifier = modifier
            )
        }

        Step.CAPTURE -> {
            CameraScreen(
                onImageCaptured = viewModel::processReceiptImage,
                onBack = { viewModel.goToStep(Step.FRIENDS) },
                isProcessing = state.isProcessing,
                modifier = modifier
            )
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
                onShare = { /* TODO: Implement sharing */ },
                modifier = modifier
            )
        }
    }
}

