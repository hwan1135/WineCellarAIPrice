package com.example.winecellar.viewmodel

import androidx.lifecycle.ViewModel
import com.example.winecellar.ai.MockAiPriceService
import com.example.winecellar.data.InMemoryRepository
import com.example.winecellar.model.AiPriceEstimate
import com.example.winecellar.model.Bottle
import com.example.winecellar.model.Cellar
import com.example.winecellar.model.DrinkStatus
import com.example.winecellar.model.DrinkingWindow
import com.example.winecellar.model.Shelf
import com.example.winecellar.repository.AiPriceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.UUID

class WineCellarViewModel : ViewModel() {

    val cellars: StateFlow<List<Cellar>> = InMemoryRepository.cellars
    val shelves: StateFlow<List<Shelf>> = InMemoryRepository.shelves
    val bottles: StateFlow<List<Bottle>> = InMemoryRepository.bottles

    private val aiPriceRepository = AiPriceRepository(MockAiPriceService())

    private val _isUpdatingAiPrices = MutableStateFlow(false)
    val isUpdatingAiPrices: StateFlow<Boolean> = _isUpdatingAiPrices.asStateFlow()

    private val _aiUpdateMessage = MutableStateFlow<String?>(null)
    val aiUpdateMessage: StateFlow<String?> = _aiUpdateMessage.asStateFlow()

    fun clearAiUpdateMessage() {
        _aiUpdateMessage.value = null
    }

    fun addCellar(
        name: String,
        shelfCount: Int,
        capacityPerShelf: Int
    ): String {
        val cellarId = UUID.randomUUID().toString()

        val cellar = Cellar(
            id = cellarId,
            name = name.trim()
        )
        InMemoryRepository.addCellar(cellar)

        val shelfList = (1..shelfCount).map { position ->
            Shelf(
                id = UUID.randomUUID().toString(),
                cellarId = cellarId,
                name = "Shelf $position",
                position = position,
                capacity = capacityPerShelf
            )
        }
        InMemoryRepository.addShelves(shelfList)

        return cellarId
    }

    fun addBottle(
        cellarId: String,
        wineName: String,
        producer: String,
        vintage: Int?,
        region: String,
        country: String,
        grapeVariety: String,
        quantity: Int,
        purchasePrice: Double?,
        notes: String
    ): Boolean {
        val firstOpenSlot = findFirstOpenSlot(cellarId) ?: return false

        val bottle = Bottle(
            id = UUID.randomUUID().toString(),
            cellarId = cellarId,
            shelfId = firstOpenSlot.first,
            slotIndex = firstOpenSlot.second,
            wineName = wineName.trim(),
            producer = producer.trim(),
            vintage = vintage,
            region = region.trim(),
            country = country.trim(),
            grapeVariety = grapeVariety.trim(),
            quantity = quantity,
            purchasePrice = purchasePrice,
            estimatedCurrentPrice = null,
            notes = notes.trim(),
            drinkingWindow = estimateDrinkingWindow(
                wineName = wineName,
                vintage = vintage,
                region = region,
                grapeVariety = grapeVariety
            ),
            aiPriceEstimate = null
        )

        InMemoryRepository.addBottle(bottle)
        return true
    }

    fun moveBottle(
        bottleId: String,
        targetShelfId: String,
        targetSlotIndex: Int
    ): Boolean {
        val bottle = InMemoryRepository.getBottle(bottleId) ?: return false

        if (InMemoryRepository.isSlotOccupied(targetShelfId, targetSlotIndex)) {
            return false
        }

        InMemoryRepository.updateBottle(
            bottle.copy(
                shelfId = targetShelfId,
                slotIndex = targetSlotIndex
            )
        )
        return true
    }

    fun updateAiPriceForBottle(bottleId: String) {
        val bottle = InMemoryRepository.getBottle(bottleId)
        if (bottle == null) {
            _aiUpdateMessage.value = "Bottle not found."
            return
        }

        _isUpdatingAiPrices.value = true

        val results = aiPriceRepository.estimatePricesForBottles(listOf(bottle))
        val estimate = results[bottle.id]

        if (estimate != null) {
            applyAiEstimate(bottle, estimate)
            _aiUpdateMessage.value = "AI price updated for ${bottle.wineName}."
        } else {
            _aiUpdateMessage.value = "No AI estimate returned."
        }

        _isUpdatingAiPrices.value = false
    }

    fun updateAiPricesForCellar(cellarId: String) {
        val cellarBottles = getBottlesForCellar(cellarId)
        if (cellarBottles.isEmpty()) {
            _aiUpdateMessage.value = "No bottles found in this cellar."
            return
        }

        _isUpdatingAiPrices.value = true

        val results = aiPriceRepository.estimatePricesForBottles(cellarBottles)
        var updatedCount = 0

        cellarBottles.forEach { bottle ->
            val estimate = results[bottle.id]
            if (estimate != null) {
                applyAiEstimate(bottle, estimate)
                updatedCount++
            }
        }

        _aiUpdateMessage.value = "AI prices updated for $updatedCount bottles."
        _isUpdatingAiPrices.value = false
    }

    private fun applyAiEstimate(
        bottle: Bottle,
        estimate: AiPriceEstimate
    ) {
        InMemoryRepository.updateBottle(
            bottle.copy(
                estimatedCurrentPrice = estimate.estimatedPrice,
                aiPriceEstimate = estimate
            )
        )
    }

    fun getCellar(cellarId: String): Cellar? = InMemoryRepository.getCellar(cellarId)

    fun getShelvesForCellar(cellarId: String): List<Shelf> =
        InMemoryRepository.getShelvesForCellar(cellarId)

    fun getBottlesForCellar(cellarId: String): List<Bottle> =
        InMemoryRepository.getBottlesForCellar(cellarId)

    fun getBottle(bottleId: String): Bottle? =
        InMemoryRepository.getBottle(bottleId)

    fun getBottleAtSlot(shelfId: String, slotIndex: Int): Bottle? =
        InMemoryRepository.getBottleAtSlot(shelfId, slotIndex)

    fun getBottleCount(cellarId: String): Int =
        getBottlesForCellar(cellarId).size

    fun getCapacity(cellarId: String): Int =
        getShelvesForCellar(cellarId).sumOf { it.capacity }

    fun getPriorityBottles(cellarId: String): List<Bottle> {
        return getBottlesForCellar(cellarId)
            .sortedByDescending { getPriorityScore(it) }
    }

    fun getShelfDisplayName(shelfId: String?): String {
        if (shelfId == null) return "Unassigned"
        return shelves.value.find { it.id == shelfId }?.name ?: "Unknown Shelf"
    }

    private fun findFirstOpenSlot(cellarId: String): Pair<String, Int>? {
        val cellarShelves = getShelvesForCellar(cellarId)

        for (shelf in cellarShelves) {
            for (slotIndex in 0 until shelf.capacity) {
                if (!InMemoryRepository.isSlotOccupied(shelf.id, slotIndex)) {
                    return shelf.id to slotIndex
                }
            }
        }

        return null
    }

    private fun getPriorityScore(bottle: Bottle): Int {
        val baseScore = when (bottle.drinkingWindow.status) {
            DrinkStatus.PAST_PEAK -> 100
            DrinkStatus.NEAR_END_OF_WINDOW -> 90
            DrinkStatus.AT_PEAK -> 80
            DrinkStatus.APPROACHING_PEAK -> 60
            DrinkStatus.TOO_YOUNG -> 20
            DrinkStatus.UNKNOWN -> 10
        }

        return if (bottle.quantity > 1) baseScore + 2 else baseScore
    }

    private fun estimateDrinkingWindow(
        wineName: String,
        vintage: Int?,
        region: String,
        grapeVariety: String
    ): DrinkingWindow {
        val currentYear = LocalDate.now().year

        if (vintage == null) {
            return DrinkingWindow(
                status = DrinkStatus.UNKNOWN,
                confidence = 0.2f,
                recommendation = "Vintage missing; unable to estimate drinking window."
            )
        }

        val lowerWineName = wineName.lowercase()
        val lowerRegion = region.lowercase()
        val lowerGrape = grapeVariety.lowercase()

        val agingYears = when {
            "barolo" in lowerWineName -> 20
            "nebbiolo" in lowerGrape -> 18
            "bordeaux" in lowerRegion -> 18
            "cabernet" in lowerGrape -> 15
            "syrah" in lowerGrape -> 12
            "riesling" in lowerGrape -> 12
            "pinot noir" in lowerGrape -> 10
            "chardonnay" in lowerGrape -> 8
            else -> 8
        }

        val startYear = vintage + 3
        val peakStartYear = vintage + (agingYears / 2)
        val peakEndYear = vintage + agingYears
        val endYear = vintage + agingYears + 3

        val status = when {
            currentYear < startYear -> DrinkStatus.TOO_YOUNG
            currentYear in startYear until peakStartYear -> DrinkStatus.APPROACHING_PEAK
            currentYear in peakStartYear..peakEndYear -> DrinkStatus.AT_PEAK
            currentYear in (peakEndYear + 1)..endYear -> DrinkStatus.NEAR_END_OF_WINDOW
            currentYear > endYear -> DrinkStatus.PAST_PEAK
            else -> DrinkStatus.UNKNOWN
        }

        val recommendation = when (status) {
            DrinkStatus.TOO_YOUNG -> "Hold for additional aging."
            DrinkStatus.APPROACHING_PEAK -> "Can drink now, but likely improves with more time."
            DrinkStatus.AT_PEAK -> "Ideal time to drink."
            DrinkStatus.NEAR_END_OF_WINDOW -> "Prioritize soon before quality declines."
            DrinkStatus.PAST_PEAK -> "Drink first; may be past optimal maturity."
            DrinkStatus.UNKNOWN -> "Insufficient data for recommendation."
        }

        return DrinkingWindow(
            startYear = startYear,
            peakStartYear = peakStartYear,
            peakEndYear = peakEndYear,
            endYear = endYear,
            status = status,
            confidence = 0.6f,
            recommendation = recommendation
        )
    }
}