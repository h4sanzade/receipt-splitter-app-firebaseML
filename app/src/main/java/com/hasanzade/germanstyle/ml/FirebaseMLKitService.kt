package com.hasanzade.germanstyle.ml

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class FirebaseMLKitService(private val context: Context) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromImage(imageUri: Uri): Result<String> {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val text = processImageWithMLKit(image)
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extractTextFromBitmap(bitmap: Bitmap): Result<String> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val text = processImageWithMLKit(image)
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extractTextFromFile(file: File): Result<String> {
        return try {
            val image = InputImage.fromFilePath(context, Uri.fromFile(file))
            val text = processImageWithMLKit(image)
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extractTextFromFilePath(filePath: String): Result<String> {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return Result.failure(Exception("File does not exist: $filePath"))
            }

            val image = InputImage.fromFilePath(context, Uri.fromFile(file))
            val text = processImageWithMLKit(image)
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun processImageWithMLKit(image: InputImage): String =
        suspendCancellableCoroutine { continuation ->
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resume("")
                    }
                }
                .addOnCanceledListener {
                    if (continuation.isActive) {
                        continuation.resume("")
                    }
                }
        }

    fun closeRecognizer() {
        textRecognizer.close()
    }
}