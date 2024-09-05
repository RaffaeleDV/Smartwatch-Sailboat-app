package com.example.sailboatapp.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.sailboatapp.presentation.MainActivity
import com.example.sailboatapp.presentation.network.ConnectionState
import com.example.sailboatapp.presentation.network.checkWConnection

import com.example.sailboatapp.presentation.ui.screen.Homepage
import com.example.sailboatapp.presentation.ui.screen.LOG_ENABLED
import com.example.sailboatapp.presentation.ui.screen.Map
import com.example.sailboatapp.presentation.ui.screen.Polars
import kotlinx.coroutines.delay


const val DEGREE_SYMBOL = "\u00B0"
const val KNOT_SYMBOL = "Kn"

var connectionState: ConnectionState = ConnectionState.Loading

@Composable
fun SailboatApp(
    mainActivity: MainActivity,
) {
    var checkConnection by remember { mutableStateOf("") }
    var isSwippeEnabled by remember { mutableStateOf(true) }

    if (LOG_ENABLED) Log.d("DEBUG", "connectionState: $connectionState")

    var isReady by remember { mutableStateOf(false) }
    //val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            if (LOG_ENABLED) Log.d("DEBUG", "connectionState: $connectionState")
            // Simulate a delay
            checkConnection = checkWConnection
            if (LOG_ENABLED) Log.d("DEBUG", "checkConnection: $checkConnection")
            if (connectionState == ConnectionState.Remote || connectionState == ConnectionState.Local || connectionState == ConnectionState.Offline) {
                isReady = true
                break
            }
            delay(3000)
        }
    }

    if (isReady) {
        // UI that depends on isReady being true
        val navController = rememberSwipeDismissableNavController()
        if (connectionState == ConnectionState.Remote || connectionState == ConnectionState.Local) {
            SwipeDismissableNavHost(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                navController = navController,
                userSwipeEnabled = isSwippeEnabled,
                startDestination = "homepage"
            ) {
                composable("homepage") {
                    Homepage(navController, mainActivity) { newValues ->
                        isSwippeEnabled = newValues
                    }
                }
                composable("polars") {
                    Polars(navController) { newValues ->
                        isSwippeEnabled = newValues
                    }
                }
                composable("map") {
                    Map(navController) { newValues ->
                        isSwippeEnabled = newValues
                    }
                }
                /*composable("test") {
                    Test()
                }*/
            }
        } else if(connectionState == ConnectionState.Offline) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Server non raggiungibile o Dispositivo Offline",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        mainActivity.finish()
                    },
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(text = "Close")
                }
            }
        }
    }else {
        // Placeholder or Loading UI
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Connection ${connectionState}...\n" +
                        "Prova server $checkConnection...",
                fontSize = 18.sp,
                textAlign = TextAlign.Center

            )
            /*Button(
                modifier = Modifier.width(100.dp),
                onClick = { mainActivity.recreate() }) {
                Text("Refresh")
            }*/
        }
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .size(100.dp),
            strokeWidth = 5.dp,
            trackColor = MaterialTheme.colors.primaryVariant,

            )
    }

}
