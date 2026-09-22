package com.example.winecellar.ai

object AiPricePromptBuilder {

    fun buildInstructions(): String {
        return """
Return only structured JSON with this schema:
{
  "results": [
    {
      "bottleId": "string",
      "estimatedPrice": number or null,
      "lowPrice": number or null,
      "highPrice": number or null,
      "confidence": "low" | "medium" | "high" | null,
      "explanation": "short explanation"
    }
  ]
}

Estimate current wine prices in USD using the provided bottle metadata.
If uncertain, return a conservative range and lower confidence.
Do not include markdown.
Do not include extra commentary outside the JSON.
""".trimIndent()
    }
}