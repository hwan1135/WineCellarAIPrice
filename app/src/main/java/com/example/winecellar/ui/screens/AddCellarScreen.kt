package com.example.winecellar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddCellarScreen(
    onSave: (String, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    var cellarName by remember { mutableStateOf("") }
    var shelfCountText by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("") }

    val shelfCount = shelfCountText.toIntOrNull() ?: 0
    val capacityPerShelf = capacityText.toIntOrNull() ?: 0
    val totalCapacity = shelfCount * capacityPerShelf

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add Cellar", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = cellarName,
            onValueChange = { cellarName = it },
            label = { Text("Cellar Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = shelfCountText,
            onValueChange = { shelfCountText = it.filter(Char::isDigit) },
            label = { Text("Number of Shelves") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = capacityText,
            onValueChange = { capacityText = it.filter(Char::isDigit) },
            label = { Text("Capacity Per Shelf") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Total Capacity: $totalCapacity bottles")

        Button(
            onClick = { onSave(cellarName.trim(), shelfCount, capacityPerShelf) },
            enabled = cellarName.isNotBlank() && shelfCount > 0 && capacityPerShelf > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Cellar")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}