package com.example.winecellar.ai

data class AiPriceBottleInput(
    val bottleId: String,
    val wineName: String,
    val producer: String,
    val vintage: Int?,
    val region: String,
    val country: String,
    val grapeVariety: String,
    val bottleSizeMl: Int? = 750
)

data class AiPriceRequest(
    val task: String = "Estimate current wine prices",
    val currency: String = "USD",
    val instructions: String,
    val bottles: List<AiPriceBottleInput>
)

data class AiPriceBottleResult(
    val bottleId: String,
    val estimatedPrice: Double?,
    val lowPrice: Double?,
    val highPrice: Double?,
    val confidence: String?,
    val explanation: String?
)

data class AiPriceResponse(
    val results: List<AiPriceBottleResult>
)