package com.example.winecellar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.winecellar.viewmodel.WineCellarViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BottleDetailScreen(
    vm: WineCellarViewModel,
    bottleId: String,
    onBack: () -> Unit
) {
    val bottle = vm.getBottle(bottleId)
    val aiUpdateMessage by vm.aiUpdateMessage.collectAsState()
    val isUpdatingAiPrices by vm.isUpdatingAiPrices.collectAsState()

    if (bottle == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Bottle not found")
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        return
    }

    val lastUpdatedText = bottle.aiPriceEstimate?.lastUpdatedEpochMs?.let {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it))
    } ?: "Never"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${bottle.wineName} (${bottle.vintage ?: "NV"})",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Producer: ${bottle.producer}")
        Text("Region: ${bottle.region.ifBlank { "Unknown" }}")
        Text("Country: ${bottle.country.ifBlank { "Unknown" }}")
        Text("Grape: ${bottle.grapeVariety.ifBlank { "Unknown" }}")
        Text("Quantity: ${bottle.quantity}")
        Text("Purchase Price: ${bottle.purchasePrice ?: "Unknown"}")
        Text("Drink Window: ${bottle.drinkingWindow.startYear ?: "?"} - ${bottle.drinkingWindow.endYear ?: "?"}")
        Text("Peak: ${bottle.drinkingWindow.peakStartYear ?: "?"} - ${bottle.drinkingWindow.peakEndYear ?: "?"}")
        Text("Status: ${bottle.drinkingWindow.status}")
        Text("Recommendation: ${bottle.drinkingWindow.recommendation}")
        Text("Shelf: ${vm.getShelfDisplayName(bottle.shelfId)}")
        Text("Slot: ${if (bottle.slotIndex != null) bottle.slotIndex + 1 else "Unassigned"}")
        Text("Notes: ${bottle.notes.ifBlank { "None" }}")

        Text("AI Estimated Price: ${bottle.aiPriceEstimate?.estimatedPrice ?: "Unknown"}")
        Text("AI Range: ${bottle.aiPriceEstimate?.lowPrice ?: "?"} - ${bottle.aiPriceEstimate?.highPrice ?: "?"}")
        Text("AI Confidence: ${bottle.aiPriceEstimate?.confidence ?: "Unknown"}")
        Text("AI Last Updated: $lastUpdatedText")
        Text("AI Explanation: ${bottle.aiPriceEstimate?.explanation ?: "No AI estimate yet"}")

        aiUpdateMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Button(
            onClick = { vm.updateAiPriceForBottle(bottle.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isUpdatingAiPrices) "Updating AI Price..." else "Refresh AI Price")
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}