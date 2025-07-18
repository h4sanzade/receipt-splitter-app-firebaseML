package com.hasanzade.germanstyle

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(private val context: Context) {

    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onCameraReady: () -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(previewView.display.rotation)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                Log.d("CameraManager", "Camera initialized successfully")
                onCameraReady()

            } catch (exc: Exception) {
                Log.e("CameraManager", "Camera initialization failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto(
        onImageCaptured: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError("Camera not ready")
            return
        }

        val photoFile = File(
            context.cacheDir,
            "receipt_${System.currentTimeMillis()}.jpg"
        )

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d("CameraManager", "Photo saved successfully: $savedUri")
                    onImageCaptured(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    val errorMsg = "Photo capture failed: ${exception.message}"
                    Log.e("CameraManager", errorMsg, exception)
                    onError(errorMsg)
                }
            }
        )
    }

    fun cleanup() {
        cameraExecutor.shutdown()
    }
}