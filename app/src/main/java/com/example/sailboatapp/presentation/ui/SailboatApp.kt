package com.example.sailboatapp.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.sailboatapp.presentation.MainActivity
import com.example.sailboatapp.presentation.network.ConnectionState
import com.example.sailboatapp.presentation.network.connectionState
import com.example.sailboatapp.presentation.ui.screen.Homepage
import com.example.sailboatapp.presentation.ui.screen.LOG_ENABLED
import com.example.sailboatapp.presentation.ui.screen.LocalViewModel
import com.example.sailboatapp.presentation.ui.screen.Map
import com.example.sailboatapp.presentation.ui.screen.Polars
import com.example.sailboatapp.presentation.ui.screen.RemoteViewModel
import com.example.sailboatapp.presentation.ui.screen.Test
import kotlinx.coroutines.delay


const val DEGREE_SYMBOL = "\u00B0"
const val KNOT_SYMBOL = "Kn"


@Composable
fun SailboatApp(
    mainActivity: MainActivity,
   ) {

    var isSwippeEnabled by remember { mutableStateOf(true) }

    if(LOG_ENABLED) Log.d("DEBUG","connectionState: $connectionState")

    var isReady by remember { mutableStateOf(false) }
    //val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            if(LOG_ENABLED)Log.d("DEBUG","connectionState: $connectionState")
            // Simulate a delay
            if (connectionState == ConnectionState.Remote || connectionState == ConnectionState.Local) {
                isReady = true
                break
            }
            delay(3000)
        }
    }

    if (isReady) {
        // UI that depends on isReady being true
        val navController = rememberSwipeDismissableNavController()
        if(connectionState == ConnectionState.Remote || connectionState == ConnectionState.Local){
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

    } else {
        // Placeholder or Loading UI
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connection ${connectionState}...",
                fontSize = 18.sp)
            /*Button(
                modifier = Modifier.width(100.dp),
                onClick = { mainActivity.recreate() }) {
                Text("Refresh")
            }*/
        }
    }

}
