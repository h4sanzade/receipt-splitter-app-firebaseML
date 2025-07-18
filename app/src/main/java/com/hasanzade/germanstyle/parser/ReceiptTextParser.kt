package com.hasanzade.germanstyle.parser


import com.hasanzade.germanstyle.data.ReceiptItem

class ReceiptTextParser {

    // Different regex patterns for various receipt formats
    private val receiptPatterns = listOf(
        // Pattern 1: "Fish 2 10.00 20.00" (name, quantity, unit price, total)
        Regex("""^(.+?)\s+(\d+)\s+(\d+\.?\d*)\s+(\d+\.?\d*)$"""),

        // Pattern 2: "Fish x2 $10.00 $20.00" (with currency and x notation)
        Regex("""^(.+?)\s+x?(\d+)\s+\$?(\d+\.?\d*)\s+\$?(\d+\.?\d*)$"""),

        // Pattern 3: "2x Fish $10.00 ea $20.00" (quantity first)
        Regex("""^(\d+)x?\s+(.+?)\s+\$?(\d+\.?\d*)\s+(?:ea\s+)?\$?(\d+\.?\d*)$"""),

        // Pattern 4: "Fish - 2 @ $10.00 = $20.00" (with @ symbol)
        Regex("""^(.+?)\s*-\s*(\d+)\s*@\s*\$?(\d+\.?\d*)\s*=\s*\$?(\d+\.?\d*)$"""),

        // Pattern 5: "Fish (2) $10.00 $20.00" (quantity in parentheses)
        Regex("""^(.+?)\s*\((\d+)\)\s*\$?(\d+\.?\d*)\s+\$?(\d+\.?\d*)$""")
    )

    fun parseReceiptText(text: String): List<ReceiptItem> {
        val items = mutableListOf<ReceiptItem>()
        val lines = text.split('\n')

        lines.forEach { line ->
            val cleanLine = line.trim()
            if (cleanLine.isEmpty() || cleanLine.length < 5) return@forEach

            // Skip lines that look like headers, totals, or non-item lines
            if (isNonItemLine(cleanLine)) return@forEach

            val parsedItem = parseLineToItem(cleanLine)
            if (parsedItem != null) {
                items.add(parsedItem)
            }
        }

        return items
    }

    private fun parseLineToItem(line: String): ReceiptItem? {
        receiptPatterns.forEach { pattern ->
            val match = pattern.find(line)
            if (match != null) {
                return try {
                    val groups = match.groupValues

                    val (itemName, quantity, unitPrice, totalPrice) = when {
                        // Handle quantity-first format: "2x Fish $10.00 ea $20.00"
                        groups.size >= 5 && groups[1].toIntOrNull() != null -> {
                            listOf(groups[2], groups[1], groups[3], groups[4])
                        }
                        // Handle normal format: "Fish 2 10.00 20.00"
                        else -> {
                            listOf(groups[1], groups[2], groups[3], groups[4])
                        }
                    }

                    val parsedQuantity = quantity.toIntOrNull() ?: 1
                    val parsedUnitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                    val parsedTotalPrice = totalPrice.toDoubleOrNull() ?: 0.0

                    // Validate the parsed data
                    if (itemName.isBlank() || parsedQuantity <= 0 || parsedUnitPrice < 0 || parsedTotalPrice < 0) {
                        return@forEach
                    }

                    ReceiptItem(
                        name = itemName.trim(),
                        quantity = parsedQuantity,
                        unitPrice = parsedUnitPrice,
                        totalPrice = parsedTotalPrice
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    private fun isNonItemLine(line: String): Boolean {
        val lowerLine = line.lowercase()
        val skipKeywords = listOf(
            "total", "subtotal", "tax", "tip", "change", "cash", "card",
            "receipt", "thank", "visit", "welcome", "phone", "address",
            "date", "time", "server", "table", "order", "payment"
        )

        return skipKeywords.any { keyword ->
            lowerLine.contains(keyword)
        }
    }
}
