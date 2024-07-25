package com.example.sailboatapp.presentation.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.sailboatapp.presentation.MainActivity
import com.example.sailboatapp.presentation.ui.screen.Homepage
import com.example.sailboatapp.presentation.ui.screen.Map
import com.example.sailboatapp.presentation.ui.screen.Polars
import com.example.sailboatapp.presentation.ui.screen.Test


const val DEGREE_SYMBOL = "\u00B0"
const val KNOT_SYMBOL = "Kn"


@Composable
fun SailboatApp(mainActivity: MainActivity) {

    var isSwippeEnabled by remember { mutableStateOf(true) }


    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        navController = navController,
        userSwipeEnabled = isSwippeEnabled,
        startDestination = "homepage"
    ) {
        composable("homepage") {
            Homepage(navController, isSwippeEnabled, mainActivity) { newValues ->
                isSwippeEnabled = newValues as Boolean
            }
        }
        composable("polars") {
            Polars(navController, isSwippeEnabled) { newValues ->
                isSwippeEnabled = newValues as Boolean
            }
        }
        composable("map") {
            Map(navController, isSwippeEnabled) { newValues ->
                isSwippeEnabled = newValues as Boolean
            }
        }
        composable("test") {
            Test()
        }
    }
}