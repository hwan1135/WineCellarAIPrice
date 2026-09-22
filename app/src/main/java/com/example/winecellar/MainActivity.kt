package com.example.winecellar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.winecellar.ui.theme.WineCellarTheme
import com.example.winecellar.viewmodel.WineCellarViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WineCellarTheme {
                val viewModel: WineCellarViewModel = viewModel()
                WineCellarApp(viewModel)
            }
        }
    }
}