package com.example.winecellar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.winecellar.viewmodel.WineCellarViewModel

@Composable
fun RecommendationsScreen(
    vm: WineCellarViewModel,
    cellarId: String,
    onBack: () -> Unit
) {
    val priorityBottles = vm.getPriorityBottles(cellarId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Drink First Recommendations",
            style = MaterialTheme.typography.headlineSmall
        )

        if (priorityBottles.isEmpty()) {
            Text("No bottles found in this cellar.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(priorityBottles) { bottle ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${bottle.wineName} (${bottle.vintage ?: "NV"})")
                            Text("Producer: ${bottle.producer}")
                            Text("Status: ${bottle.drinkingWindow.status}")
                            Text("Drink Window: ${bottle.drinkingWindow.startYear ?: "?"} - ${bottle.drinkingWindow.endYear ?: "?"}")
                            Text("Peak: ${bottle.drinkingWindow.peakStartYear ?: "?"} - ${bottle.drinkingWindow.peakEndYear ?: "?"}")
                            Text("AI Price: ${bottle.aiPriceEstimate?.estimatedPrice ?: "Unknown"}")
                            Text("Recommendation: ${bottle.drinkingWindow.recommendation}")
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}