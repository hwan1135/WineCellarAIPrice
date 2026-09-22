package com.example.winecellar.data

import com.example.winecellar.model.Bottle
import com.example.winecellar.model.Cellar
import com.example.winecellar.model.Shelf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object InMemoryRepository {

    private val _cellars = MutableStateFlow<List<Cellar>>(emptyList())
    val cellars: StateFlow<List<Cellar>> = _cellars.asStateFlow()

    private val _shelves = MutableStateFlow<List<Shelf>>(emptyList())
    val shelves: StateFlow<List<Shelf>> = _shelves.asStateFlow()

    private val _bottles = MutableStateFlow<List<Bottle>>(emptyList())
    val bottles: StateFlow<List<Bottle>> = _bottles.asStateFlow()

    fun addCellar(cellar: Cellar) {
        _cellars.value = _cellars.value + cellar
    }

    fun addShelves(newShelves: List<Shelf>) {
        _shelves.value = _shelves.value + newShelves
    }

    fun addBottle(bottle: Bottle) {
        _bottles.value = _bottles.value + bottle
    }

    fun updateBottle(updatedBottle: Bottle) {
        _bottles.value = _bottles.value.map { bottle ->
            if (bottle.id == updatedBottle.id) updatedBottle else bottle
        }
    }

    fun getCellar(cellarId: String): Cellar? {
        return _cellars.value.find { it.id == cellarId }
    }

    fun getShelvesForCellar(cellarId: String): List<Shelf> {
        return _shelves.value
            .filter { it.cellarId == cellarId }
            .sortedBy { it.position }
    }

    fun getBottlesForCellar(cellarId: String): List<Bottle> {
        return _bottles.value.filter { it.cellarId == cellarId }
    }

    fun getBottle(bottleId: String): Bottle? {
        return _bottles.value.find { it.id == bottleId }
    }

    fun isSlotOccupied(shelfId: String, slotIndex: Int): Boolean {
        return _bottles.value.any { bottle ->
            bottle.shelfId == shelfId && bottle.slotIndex == slotIndex
        }
    }

    fun getBottleAtSlot(shelfId: String, slotIndex: Int): Bottle? {
        return _bottles.value.find { bottle ->
            bottle.shelfId == shelfId && bottle.slotIndex == slotIndex
        }
    }
}