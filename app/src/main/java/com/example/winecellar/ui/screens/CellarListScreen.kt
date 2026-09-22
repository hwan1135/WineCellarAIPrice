package com.example.winecellar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.winecellar.viewmodel.WineCellarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellarListScreen(
    vm: WineCellarViewModel,
    onAddCellar: () -> Unit,
    onOpenCellar: (String) -> Unit
) {
    val cellars by vm.cellars.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Wine Cellars") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddCellar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Cellar")
            }

            if (cellars.isEmpty()) {
                Text("No cellars yet. Add your first cellar to begin.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cellars) { cellar ->
                        val bottleCount = vm.getBottleCount(cellar.id)
                        val totalCapacity = vm.getCapacity(cellar.id)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCellar(cellar.id) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(cellar.name, style = MaterialTheme.typography.titleMedium)
                                Text("Bottles: $bottleCount")
                                Text("Capacity: $totalCapacity")
                            }
                        }
                    }
                }
            }
        }
    }
}