package com.example.winecellar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.winecellar.model.Bottle
import com.example.winecellar.model.DrinkStatus
import com.example.winecellar.model.Shelf
import com.example.winecellar.viewmodel.WineCellarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellarDetailScreen(
    vm: WineCellarViewModel,
    cellarId: String,
    onBack: () -> Unit,
    onAddBottle: () -> Unit,
    onBottleDetail: (String) -> Unit,
    onRecommendations: () -> Unit
) {
    val cellar = vm.getCellar(cellarId)
    val shelfState by vm.shelves.collectAsState()
    val bottleState by vm.bottles.collectAsState()
    val aiUpdateMessage by vm.aiUpdateMessage.collectAsState()
    val isUpdatingAiPrices by vm.isUpdatingAiPrices.collectAsState()

    val cellarShelves = remember(shelfState, cellarId) {
        vm.getShelvesForCellar(cellarId)
    }
    val cellarBottles = remember(bottleState, cellarId) {
        vm.getBottlesForCellar(cellarId)
    }

    var selectedBottleToMove by remember { mutableStateOf<Bottle?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cellar?.name ?: "Cellar") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Bottles: ${cellarBottles.size}")
            Text("Capacity: ${cellarShelves.sumOf { it.capacity }}")

            Button(
                onClick = onAddBottle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Bottle")
            }

            Button(
                onClick = { vm.updateAiPricesForCellar(cellarId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isUpdatingAiPrices) "Updating AI Prices..." else "Update AI Prices for Cellar")
            }

            Button(
                onClick = onRecommendations,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Drink Recommendations")
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Cellars")
            }

            aiUpdateMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            selectedBottleToMove?.let { bottle ->
                Text(
                    text = "Move mode active: ${bottle.wineName} (${bottle.vintage ?: "NV"})",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cellarShelves) { shelf ->
                    ShelfRow(
                        shelf = shelf,
                        vm = vm,
                        selectedBottleToMove = selectedBottleToMove,
                        onSelectBottle = { bottle ->
                            selectedBottleToMove = bottle
                        },
                        onCancelMove = {
                            selectedBottleToMove = null
                        },
                        onMoveCompleted = {
                            selectedBottleToMove = null
                        },
                        onBottleDetail = onBottleDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun ShelfRow(
    shelf: Shelf,
    vm: WineCellarViewModel,
    selectedBottleToMove: Bottle?,
    onSelectBottle: (Bottle) -> Unit,
    onCancelMove: () -> Unit,
    onMoveCompleted: () -> Unit,
    onBottleDetail: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${shelf.name} (${shelf.capacity} slots)",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (slotIndex in 0 until shelf.capacity) {
                    val bottle = vm.getBottleAtSlot(shelf.id, slotIndex)

                    BottleSlot(
                        bottle = bottle,
                        isTargetMode = selectedBottleToMove != null && bottle == null,
                        onClick = {
                            when {
                                selectedBottleToMove != null && bottle == null -> {
                                    val moved = vm.moveBottle(
                                        bottleId = selectedBottleToMove.id,
                                        targetShelfId = shelf.id,
                                        targetSlotIndex = slotIndex
                                    )
                                    if (moved) onMoveCompleted()
                                }

                                bottle != null && selectedBottleToMove?.id == bottle.id -> {
                                    onCancelMove()
                                }

                                bottle != null -> {
                                    onSelectBottle(bottle)
                                }
                            }
                        },
                        onViewClick = {
                            if (bottle != null) {
                                onBottleDetail(bottle.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottleSlot(
    bottle: Bottle?,
    isTargetMode: Boolean,
    onClick: () -> Unit,
    onViewClick: () -> Unit
) {
    val slotColor = when {
        bottle != null -> getBottleColor(bottle.drinkingWindow.status)
        isTargetMode -> Color(0xFFDFF2D8)
        else -> Color(0xFFEAEAEA)
    }

    Box(
        modifier = Modifier
            .width(76.dp)
            .height(118.dp)
            .background(slotColor)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bottle == null) {
            Text(
                text = if (isTargetMode) "Move Here" else "Empty",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = bottle.wineName.take(9),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = bottle.vintage?.toString() ?: "NV",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = bottle.aiPriceEstimate?.estimatedPrice?.let { "$$it" } ?: "No AI Price",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onViewClick) {
                    Text("View", color = Color.White)
                }
            }
        }
    }
}

private fun getBottleColor(status: DrinkStatus): Color {
    return when (status) {
        DrinkStatus.PAST_PEAK -> Color(0xFFB3261E)
        DrinkStatus.NEAR_END_OF_WINDOW -> Color(0xFF9C6644)
        DrinkStatus.AT_PEAK -> Color(0xFF7B1E3A)
        DrinkStatus.APPROACHING_PEAK -> Color(0xFF5F6CAF)
        DrinkStatus.TOO_YOUNG -> Color(0xFF2E7D32)
        DrinkStatus.UNKNOWN -> Color(0xFF616161)
    }
}