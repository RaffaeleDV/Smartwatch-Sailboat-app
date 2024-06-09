package com.example.sailboatapp.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sailboatapp.presentation.ui.screen.LocalViewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.sailboatapp.presentation.ui.screen.Homepage
import com.example.sailboatapp.presentation.ui.screen.Polars
import com.example.sailboatapp.presentation.ui.screen.Map

const val DEGREE_SYMBOL = "\u00B0"
const val KNOT_SYMBOL = "Kn"

@Composable
fun SailboatApp() {


    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        modifier = Modifier,
        //.fillMaxHeight()
        //.fillMaxWidth(),
        navController = navController,
        userSwipeEnabled = false,
        startDestination = "homepage"
    ) {
        composable("homepage") {
            Homepage(navController)
        }
        composable("polars") {
            Polars(navController)
        }
        composable("map") {
            Map(navController)
        }
    }
}