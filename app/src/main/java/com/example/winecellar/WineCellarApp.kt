package com.example.winecellar

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.winecellar.ui.screens.AddBottleScreen
import com.example.winecellar.ui.screens.AddCellarScreen
import com.example.winecellar.ui.screens.BottleDetailScreen
import com.example.winecellar.ui.screens.CellarDetailScreen
import com.example.winecellar.ui.screens.CellarListScreen
import com.example.winecellar.ui.screens.RecommendationsScreen
import com.example.winecellar.viewmodel.WineCellarViewModel

object Routes {
    const val CELLARS = "cellars"
    const val ADD_CELLAR = "addCellar"
    const val CELLAR_DETAIL = "cellar/{cellarId}"
    const val ADD_BOTTLE = "addBottle/{cellarId}"
    const val BOTTLE_DETAIL = "bottle/{bottleId}"
    const val RECOMMENDATIONS = "recommendations/{cellarId}"

    fun cellarDetail(cellarId: String) = "cellar/$cellarId"
    fun addBottle(cellarId: String) = "addBottle/$cellarId"
    fun bottleDetail(bottleId: String) = "bottle/$bottleId"
    fun recommendations(cellarId: String) = "recommendations/$cellarId"
}

@Composable
fun WineCellarApp(viewModel: WineCellarViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CELLARS
    ) {
        composable(Routes.CELLARS) {
            CellarListScreen(
                vm = viewModel,
                onAddCellar = { navController.navigate(Routes.ADD_CELLAR) },
                onOpenCellar = { cellarId ->
                    navController.navigate(Routes.cellarDetail(cellarId))
                }
            )
        }

        composable(Routes.ADD_CELLAR) {
            AddCellarScreen(
                onSave = { name, shelfCount, capacityPerShelf ->
                    val cellarId = viewModel.addCellar(name, shelfCount, capacityPerShelf)
                    navController.navigate(Routes.cellarDetail(cellarId)) {
                        popUpTo(Routes.CELLARS)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CELLAR_DETAIL,
            arguments = listOf(navArgument("cellarId") { type = NavType.StringType })
        ) { entry ->
            val cellarId = entry.arguments?.getString("cellarId").orEmpty()
            CellarDetailScreen(
                vm = viewModel,
                cellarId = cellarId,
                onBack = { navController.popBackStack() },
                onAddBottle = { navController.navigate(Routes.addBottle(cellarId)) },
                onBottleDetail = { bottleId ->
                    navController.navigate(Routes.bottleDetail(bottleId))
                },
                onRecommendations = {
                    navController.navigate(Routes.recommendations(cellarId))
                }
            )
        }

        composable(
            route = Routes.ADD_BOTTLE,
            arguments = listOf(navArgument("cellarId") { type = NavType.StringType })
        ) { entry ->
            val cellarId = entry.arguments?.getString("cellarId").orEmpty()
            AddBottleScreen(
                vm = viewModel,
                cellarId = cellarId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.BOTTLE_DETAIL,
            arguments = listOf(navArgument("bottleId") { type = NavType.StringType })
        ) { entry ->
            val bottleId = entry.arguments?.getString("bottleId").orEmpty()
            BottleDetailScreen(
                vm = viewModel,
                bottleId = bottleId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RECOMMENDATIONS,
            arguments = listOf(navArgument("cellarId") { type = NavType.StringType })
        ) { entry ->
            val cellarId = entry.arguments?.getString("cellarId").orEmpty()
            RecommendationsScreen(
                vm = viewModel,
                cellarId = cellarId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}