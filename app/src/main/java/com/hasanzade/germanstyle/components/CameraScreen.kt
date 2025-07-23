package com.hasanzade.germanstyle.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import com.hasanzade.germanstyle.CameraManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onBack: () -> Unit,
    isProcessing: Boolean,
    processingStatus: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { CameraManager(context) }
    var isCameraReady by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    cameraManager.startCamera(
                        previewView = previewView,
                        lifecycleOwner = lifecycleOwner,
                        onCameraReady = {
                            isCameraReady = true
                            errorMessage = null
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            if (isProcessing && processingStatus.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = processingStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Instructions Card
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 16.dp, end = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gemini AI Receipt Scanner",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Position the receipt in the frame and tap the camera button",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Camera Ready Status
        if (!isCameraReady && !isProcessing) {
            Card(
                modifier = Modifier.align(Alignment.Center),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Initializing camera...")
                }
            }
        }

        // Enhanced Capture Button
        FloatingActionButton(
            onClick = {
                if (isCameraReady && !isProcessing) {
                    cameraManager.takePhoto(
                        onImageCaptured = onImageCaptured,
                        onError = { error ->
                            errorMessage = error
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .size(72.dp),
            containerColor = if (isCameraReady && !isProcessing)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Capture Receipt",
                    modifier = Modifier.size(32.dp),
                    tint = if (isCameraReady)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.cleanup()
        }
    }
}