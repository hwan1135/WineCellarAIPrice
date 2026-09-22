package com.example.winecellar.repository

import com.example.winecellar.ai.AiPriceBottleInput
import com.example.winecellar.ai.AiPricePromptBuilder
import com.example.winecellar.ai.AiPriceRequest
import com.example.winecellar.ai.AiPriceService
import com.example.winecellar.model.AiPriceEstimate
import com.example.winecellar.model.Bottle

class AiPriceRepository(
    private val aiPriceService: AiPriceService
) {

    fun estimatePricesForBottles(bottles: List<Bottle>): Map<String, AiPriceEstimate> {
        if (bottles.isEmpty()) return emptyMap()

        val request = AiPriceRequest(
            instructions = AiPricePromptBuilder.buildInstructions(),
            bottles = bottles.map { bottle ->
                AiPriceBottleInput(
                    bottleId = bottle.id,
                    wineName = bottle.wineName,
                    producer = bottle.producer,
                    vintage = bottle.vintage,
                    region = bottle.region,
                    country = bottle.country,
                    grapeVariety = bottle.grapeVariety,
                    bottleSizeMl = 750
                )
            }
        )

        val response = aiPriceService.estimatePrices(request)
        val now = System.currentTimeMillis()

        return response.results.associate { result ->
            result.bottleId to AiPriceEstimate(
                estimatedPrice = result.estimatedPrice,
                lowPrice = result.lowPrice,
                highPrice = result.highPrice,
                confidence = result.confidence,
                explanation = result.explanation,
                lastUpdatedEpochMs = now
            )
        }
    }
}