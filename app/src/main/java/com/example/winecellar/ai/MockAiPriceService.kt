package com.example.winecellar.ai

import kotlin.random.Random

class MockAiPriceService : AiPriceService {

    override fun estimatePrices(request: AiPriceRequest): AiPriceResponse {
        val results = request.bottles.map { bottle ->
            val basePrice = estimateBasePrice(
                wineName = bottle.wineName,
                producer = bottle.producer,
                vintage = bottle.vintage,
                region = bottle.region,
                grapeVariety = bottle.grapeVariety
            )

            val spread = (basePrice * 0.15).coerceAtLeast(5.0)

            AiPriceBottleResult(
                bottleId = bottle.bottleId,
                estimatedPrice = round2(basePrice),
                lowPrice = round2(basePrice - spread),
                highPrice = round2(basePrice + spread),
                confidence = estimateConfidence(bottle),
                explanation = buildExplanation(bottle, basePrice)
            )
        }

        return AiPriceResponse(results = results)
    }

    private fun estimateBasePrice(
        wineName: String,
        producer: String,
        vintage: Int?,
        region: String,
        grapeVariety: String
    ): Double {
        var price = 25.0

        val wine = wineName.lowercase()
        val prod = producer.lowercase()
        val reg = region.lowercase()
        val grape = grapeVariety.lowercase()

        if ("barolo" in wine) price += 45
        if ("bordeaux" in reg) price += 35
        if ("burgundy" in reg) price += 40
        if ("cabernet" in grape) price += 12
        if ("nebbiolo" in grape) price += 18
        if ("pinot noir" in grape) price += 10
        if ("reserve" in wine || "riserva" in wine) price += 15
        if (prod.isNotBlank()) price += 5

        if (vintage != null) {
            val ageBonus = ((2026 - vintage).coerceAtLeast(0)) * 0.8
            price += ageBonus
        }

        price += Random.nextDouble(0.0, 8.0)

        return price.coerceAtLeast(12.0)
    }

    private fun estimateConfidence(bottle: AiPriceBottleInput): String {
        val hasVintage = bottle.vintage != null
        val hasRegion = bottle.region.isNotBlank()
        val hasProducer = bottle.producer.isNotBlank()

        return when {
            hasVintage && hasRegion && hasProducer -> "medium"
            hasVintage && (hasRegion || hasProducer) -> "medium"
            hasRegion || hasProducer -> "low"
            else -> "low"
        }
    }

    private fun buildExplanation(
        bottle: AiPriceBottleInput,
        price: Double
    ): String {
        return "AI-estimated from producer, region, grape, wine style, and vintage metadata. Estimated current value is approximately ${round2(price)} USD."
    }

    private fun round2(value: Double): Double {
        return kotlin.math.round(value * 100.0) / 100.0
    }
}