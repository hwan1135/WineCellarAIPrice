package com.example.winecellar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.winecellar.viewmodel.WineCellarViewModel

@Composable
fun AddBottleScreen(
    vm: WineCellarViewModel,
    cellarId: String,
    onBack: () -> Unit
) {
    var wineName by remember { mutableStateOf("") }
    var producer by remember { mutableStateOf("") }
    var vintageText by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var grapeVariety by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var purchasePriceText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Add Bottle", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = wineName,
            onValueChange = { wineName = it },
            label = { Text("Wine Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = producer,
            onValueChange = { producer = it },
            label = { Text("Producer") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vintageText,
            onValueChange = { vintageText = it.filter(Char::isDigit) },
            label = { Text("Vintage") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("Region") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = grapeVariety,
            onValueChange = { grapeVariety = it },
            label = { Text("Grape Variety") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = quantityText,
            onValueChange = { quantityText = it.filter(Char::isDigit) },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = purchasePriceText,
            onValueChange = { purchasePriceText = it },
            label = { Text("Purchase Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val quantity = quantityText.toIntOrNull() ?: 0

                if (wineName.isBlank() || producer.isBlank()) {
                    errorMessage = "Wine name and producer are required."
                    return@Button
                }

                if (quantity <= 0) {
                    errorMessage = "Quantity must be greater than 0."
                    return@Button
                }

                val success = vm.addBottle(
                    cellarId = cellarId,
                    wineName = wineName,
                    producer = producer,
                    vintage = vintageText.toIntOrNull(),
                    region = region,
                    country = country,
                    grapeVariety = grapeVariety,
                    quantity = quantity,
                    purchasePrice = purchasePriceText.toDoubleOrNull(),
                    notes = notes
                )

                if (success) {
                    onBack()
                } else {
                    errorMessage = "No open slot is available in this cellar."
                }
            },
            enabled = wineName.isNotBlank() && producer.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Bottle")
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}