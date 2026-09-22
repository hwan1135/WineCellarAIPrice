package com.example.winecellar.model

enum class DrinkStatus {
    TOO_YOUNG,
    APPROACHING_PEAK,
    AT_PEAK,
    NEAR_END_OF_WINDOW,
    PAST_PEAK,
    UNKNOWN
}

data class DrinkingWindow(
    val startYear: Int? = null,
    val peakStartYear: Int? = null,
    val peakEndYear: Int? = null,
    val endYear: Int? = null,
    val status: DrinkStatus = DrinkStatus.UNKNOWN,
    val confidence: Float? = null,
    val recommendation: String = "No recommendation available"
)

data class Cellar(
    val id: String,
    val name: String,
    val notes: String = ""
)

data class Shelf(
    val id: String,
    val cellarId: String,
    val name: String,
    val position: Int,
    val capacity: Int
)

data class AiPriceEstimate(
    val estimatedPrice: Double?,
    val lowPrice: Double?,
    val highPrice: Double?,
    val confidence: String?,
    val explanation: String?,
    val lastUpdatedEpochMs: Long?
)

data class Bottle(
    val id: String,
    val cellarId: String,
    val shelfId: String?,
    val slotIndex: Int?,
    val wineName: String,
    val producer: String,
    val vintage: Int?,
    val region: String,
    val country: String,
    val grapeVariety: String,
    val quantity: Int,
    val purchasePrice: Double?,
    val estimatedCurrentPrice: Double?,
    val notes: String,
    val drinkingWindow: DrinkingWindow,
    val aiPriceEstimate: AiPriceEstimate? = null
)