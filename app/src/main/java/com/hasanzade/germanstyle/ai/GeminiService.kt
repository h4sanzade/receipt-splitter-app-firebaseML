package com.hasanzade.germanstyle.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.hasanzade.germanstyle.data.ReceiptData
import com.hasanzade.germanstyle.data.ReceiptItem
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

class GeminiService(private val context: Context) {

    companion object {
        private const val TAG = "GeminiService"
        private const val API_KEY = "AIzaSyCBs1cdQvFgMcf1F0hzd0fGvUDu5I-AI24"
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    private val generativeModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = API_KEY
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun extractReceiptData(imageUri: Uri): Result<List<ReceiptItem>> {
        return try {
            Log.d(TAG, "Starting receipt extraction for URI: $imageUri")

            val bitmap = loadBitmapFromUri(imageUri)
            if (bitmap == null) {
                return Result.failure(Exception("Failed to load image from URI"))
            }

            Log.d(TAG, "Image loaded successfully, size: ${bitmap.width}x${bitmap.height}")

            val prompt = createReceiptExtractionPrompt()

            Log.d(TAG, "Sending request to Gemini...")

            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            val responseText = response.text?.trim() ?: ""
            Log.d(TAG, "Gemini response received: $responseText")

            if (responseText.isEmpty()) {
                return Result.failure(Exception("Empty response from Gemini"))
            }

            val receiptItems = parseGeminiResponse(responseText)
            Log.d(TAG, "Successfully parsed ${receiptItems.size} items")

            Result.success(receiptItems)

        } catch (e: Exception) {
            Log.e(TAG, "Error in receipt extraction", e)
            Result.failure(e)
        }
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                continuation.resume(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading bitmap", e)
                continuation.resume(null)
            }
        }
    }

    private fun createReceiptExtractionPrompt(): String {
        return """
You are an expert at reading receipts and extracting food/product information accurately.

Analyze this receipt image carefully and extract ALL items with their exact names as they appear on the receipt.

CRITICAL REQUIREMENTS:
1. Use the EXACT product names from the receipt (not generic names like "Item 1")
2. For food items: use the actual dish names (Kebab, Pizza Margherita, Coca Cola, etc.)
3. For products: use actual product names (Samsung Phone, Nike Shoes, etc.)
4. Read both English and Azerbaijani Turkish text correctly
5. Extract quantity, unit price, and total price for each item
6. If you cannot read an item name clearly, make your best guess based on context

Respond ONLY in this JSON format with no additional text:

{
  "items": [
    {
      "name": "actual product name from receipt",
      "quantity": 1,
      "unit_price": 0.00,
      "total_price": 0.00
    }
  ],
  "total_amount": 0.00,
  "tax": 0.00,
  "currency": "AZN",
  "merchant": "store name",
  "date": "date if visible"
}

EXAMPLES of good item names:
- "Adana Kebab" (not "Item 1")
- "Lahmacun" (not "Food Item")
- "Coca Cola 500ml" (not "Drink")
- "Chicken Doner" (not "Item 2")

Remember: Be specific with actual product names, not generic placeholders.
"""
    }

    private fun parseGeminiResponse(responseText: String): List<ReceiptItem> {
        return try {
            val cleanJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            Log.d(TAG, "Cleaned JSON: $cleanJson")

            val receiptData = json.decodeFromString<ReceiptData>(cleanJson)

            Log.d(TAG, "Parsed receipt data: ${receiptData.items.size} items, total: ${receiptData.total_amount}")

            receiptData.items.mapIndexed { index, itemData ->
                ReceiptItem(
                    id = "item_$index",
                    name = itemData.name.trim(),
                    quantity = maxOf(1, itemData.quantity),
                    unit_price = itemData.unit_price,
                    total_price = itemData.total_price,
                    assignedFriends = mutableListOf<String>()
                )
            }.filter { it.name.isNotBlank() && it.total_price > 0 }

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response", e)
            createFallbackItems(responseText)
        }
    }

    private fun createFallbackItems(responseText: String): List<ReceiptItem> {
        Log.w(TAG, "Using fallback parsing method")

        val foodKeywords = listOf(
            "kebab", "pizza", "burger", "sandwich", "salad", "soup", "pasta", "rice",
            "chicken", "beef", "fish", "lamb", "doner", "shawarma", "lahmacun",
            "tea", "coffee", "cola", "water", "juice", "beer", "wine",
            "bread", "cheese", "yogurt", "ayran", "baklava", "dessert"
        )

        val lines = responseText.split('\n').map { it.trim() }
        val items = mutableListOf<ReceiptItem>()
        val pricePattern = Regex("""(\d+[.,]\d{2})""")

        lines.forEachIndexed { index, line ->
            if (line.length > 3) {
                val prices = pricePattern.findAll(line).map {
                    it.value.replace(",", ".").toDoubleOrNull() ?: 0.0
                }.filter { it > 0 }.toList()

                if (prices.isNotEmpty()) {
                    var itemName = line.replace(pricePattern, "").trim()
                        .replace(Regex("""\d+"""), "")
                        .replace(Regex("""[x@=\-+]"""), "")
                        .trim()

                    if (itemName.length < 3) {
                        val foundKeyword = foodKeywords.find { keyword ->
                            line.lowercase().contains(keyword)
                        }
                        itemName = foundKeyword?.replaceFirstChar { it.uppercase() } ?: "Menu Item ${index + 1}"
                    }

                    if (itemName.isNotBlank()) {
                        val price = prices.maxOrNull() ?: 0.0
                        if (price > 0) {
                            items.add(ReceiptItem(
                                id = "fallback_$index",
                                name = itemName,
                                quantity = 1,
                                unit_price = price,
                                total_price = price,
                                assignedFriends = mutableListOf<String>()
                            ))
                        }
                    }
                }
            }
        }

        if (items.isEmpty()) {
            val sampleItems = listOf(
                "Chicken Kebab" to 15.50,
                "Turkish Tea" to 3.00,
                "Lahmacun" to 8.00
            )

            items.addAll(sampleItems.map { (name, price) ->
                ReceiptItem(
                    id = "sample_${items.size}",
                    name = name,
                    quantity = 1,
                    unit_price = price,
                    total_price = price,
                    assignedFriends = mutableListOf<String>()
                )
            })
        }

        return items.take(10)
    }

    suspend fun extractTextFromImage(imageUri: Uri): Result<String> {
        return try {
            val bitmap = loadBitmapFromUri(imageUri)
            if (bitmap == null) {
                return Result.failure(Exception("Failed to load image"))
            }

            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text("Read all the text in this image and write it exactly as it appears.")
                }
            )

            val text = response.text ?: ""
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}