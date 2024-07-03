package com.example.sailboatapp.presentation.ui.screen

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.dialog.Dialog
import com.example.sailboatapp.R
import com.example.sailboatapp.presentation.data.readNMEA
import com.example.sailboatapp.presentation.model.Raffica
import com.example.sailboatapp.presentation.ui.DEGREE_SYMBOL
import com.example.sailboatapp.presentation.ui.KNOT_SYMBOL


class LocalConnection {
    var localConnectionState: Boolean = false

    fun setConnectionState(state: Boolean) {
        localConnectionState = state
    }

    fun getConnectionState(): Boolean {
        return localConnectionState
    }
}

var locCon = LocalConnection()
fun isConnectionLocal(): Boolean {
    return locCon.getConnectionState()
}

fun setLocalConnection(state: Boolean) {
    locCon.setConnectionState(state)
}

enum class ConnectionState {
    Local, Remote, Loading, Offline
}

const val  RASPBERRY_IP_DEFAULT = "192.168.178.48" //Raspberry ip default
const val WEBSOCKIFY_SOCKET_DEFAULT = "8080"


var raspberryIp = RASPBERRY_IP_DEFAULT //Raspberry ip
var websockifySocket = WEBSOCKIFY_SOCKET_DEFAULT



@Composable
fun Homepage(
    navController: NavHostController,
    isSwippeEnabled: Boolean,
    onSwipeChange: (Boolean) -> Unit
) {

    onSwipeChange(false)

    println("Base url: $raspberryIp")

    var lastVelVento = "0.0"
    var lastMaxWindSpeed = "0.0"
    var lastShipDirection = "0.0"

    var connectionState: ConnectionState by remember { mutableStateOf(ConnectionState.Loading) }

    val localViewModel: LocalViewModel = viewModel()

    //Raffica local
    val rafficaUiState: RafficaUiState = localViewModel.rafficaUiState
    var raffica = Raffica("", "", "")
    when (rafficaUiState) {
        is RafficaUiState.Error -> println("Error local raffica connection")
        is RafficaUiState.Loading -> println("Loading local raffica connection")
        is RafficaUiState.Success -> {
            //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
            connectionState = ConnectionState.Local
            println("Success: local connection Raffica")
            raffica = (localViewModel.rafficaUiState as RafficaUiState.Success).raffica
        }
    }
    //Neam sentence local from websocket
    val nmeaDataLocal = localViewModel.data.collectAsState()
    var nmeaDataRemote = HashMap<String, String>()
    connectionState = ConnectionState.Local
    //Neam sentence remote
    if (!isConnectionLocal()) {
        val remoteViewModel: RemoteViewModel = viewModel()
        val remoteUiState: RemoteUiState = remoteViewModel.remoteUiState
        connectionState = ConnectionState.Remote
        when (remoteUiState) {
            is RemoteUiState.Error -> {
                connectionState = ConnectionState.Offline
                println("Error remote connection")
            }

            is RemoteUiState.Loading -> println("Loading remote connection")
            is RemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                connectionState = ConnectionState.Remote
                println("Success: Remote connection")
                nmeaDataRemote =
                    readNMEA((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
            }
        }
    }

    val listState = rememberScalingLazyListState()
    val vignetteState by remember { mutableStateOf(VignettePosition.TopAndBottom) }

    val showVignette by remember {
        mutableStateOf(true)
    }

    var showDialog by remember { mutableStateOf(false) }


    Scaffold(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState, modifier = Modifier
            )
        }, vignette = {
            if (showVignette) {
                Vignette(vignettePosition = vignetteState)
            }
        }, timeText = {
            TimeText()
        })
    /* pageIndicator = {
         HorizontalPageIndicator(pageIndicatorState = pageIndicatorState)
     })*/ {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            autoCentering = AutoCenteringParams(itemIndex = 4),
            state = listState,
        ) {
            item(10) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.Red,
                    fontSize = 8.sp,
                    text = connectionState.toString()
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "Vel. Vento"
                )
            }
            item {

                //var lastlast = "0.0"
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 15.sp,
                    text = (if (nmeaDataLocal.value["windSpeed"] == null) {
                        if (nmeaDataRemote["windSpeed"].isNullOrEmpty()) {
                            "$lastVelVento $KNOT_SYMBOL"
                        } else {
                            "${nmeaDataRemote["windSpeed"]!!} $KNOT_SYMBOL"
                        }
                    } else {
                        if (nmeaDataLocal.value["windSpeed"]!! == "0.0") {
                            "$lastVelVento $KNOT_SYMBOL"
                        } else {
                            lastVelVento = nmeaDataLocal.value["windSpeed"]!!
                            "$lastVelVento $KNOT_SYMBOL"
                        }

                    })
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "Vel. Raffica:"
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 15.sp,
                    text = (if (connectionState == ConnectionState.Remote) {

                        if (nmeaDataRemote["maxWindSpeed"].isNullOrEmpty()) {
                            "$lastMaxWindSpeed $KNOT_SYMBOL"
                        } else {
                            "${nmeaDataRemote["maxWindSpeed"]!!} $KNOT_SYMBOL"
                        }
                    } else if (connectionState == ConnectionState.Offline) {
                        "- $KNOT_SYMBOL"
                    } else {
                        if (raffica.velVento == "0.0") {
                            "$lastMaxWindSpeed $KNOT_SYMBOL"
                        } else {
                            lastMaxWindSpeed = raffica.velVento
                            "$lastMaxWindSpeed $KNOT_SYMBOL"
                        }

                    })
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "Orientamento:"
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 15.sp,
                    text = (if (nmeaDataLocal.value["shipDirection"] == null) {

                        if (nmeaDataRemote["shipDirection"].isNullOrEmpty()) {
                            "$lastShipDirection$DEGREE_SYMBOL"
                        } else {
                            "${nmeaDataRemote["shipDirection"]!!}$DEGREE_SYMBOL"
                        }
                    } else {
                        if (nmeaDataLocal.value["shipDirection"]!! == "0.0") {
                            "$lastShipDirection$DEGREE_SYMBOL"
                        } else {
                            lastShipDirection = nmeaDataLocal.value["shipDirection"]!!
                            "$lastShipDirection$DEGREE_SYMBOL"
                        }

                    })
                )
            }
            item { Spacer(modifier = Modifier.height(10.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Button(//Polars page button
                        onClick = { navController.navigate("polars") },
                        modifier = Modifier.height(30.dp)
                        //.width(150.dp)
                    ) {
                        Text("Polari")
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(//Map page button
                        onClick = { navController.navigate("map") },
                        modifier = Modifier.height(30.dp)
                    ) {
                        //Text("On")
                        Icon(
                            painter = painterResource(id = R.drawable.ic_action_map_icon),
                            contentDescription = "Anchor",
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(10.dp)) }
            item {
                Button(//Config page button
                    onClick = { showDialog = true },
                    modifier = Modifier.height(30.dp)
                    //.width(150.dp)
                ) {
                    Text("Config")
                    //Icon(painter = , contentDescription = )
                }
            }
            item {
                Button(//Test page button
                    onClick = { navController.navigate("test") },
                    modifier = Modifier.height(30.dp)
                    //.width(150.dp)
                ) {
                    Text("Test")
                }
            }
        }

        var textIp by remember { mutableStateOf("") }
        var textSocket by remember { mutableStateOf("") }
        Dialog(showDialog = showDialog, onDismissRequest = { showDialog = false }) {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
                autoCentering = AutoCenteringParams(itemIndex = 4),
                //state = listState,
            ) {
                item(10) { Text(text = "IP corrente:", fontSize = 10.sp) }
                item { Text(text = "$raspberryIp:$websockifySocket", fontSize = 10.sp) }
                item { Text(text = "IP raspberry: ") }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item {
                    BasicTextField(
                        modifier = Modifier,//.absoluteOffset { IntOffset(50,160) },
                        value = textIp,
                        onValueChange = {
                            textIp = it
                            raspberryIp = it
                        },
                        textStyle = TextStyle.Default,
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colors.primary)
                                    .padding(5.dp)
                            ) {
                                innerTextField()
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item { Text(text = "Socket raspberry: ") }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item {
                    BasicTextField(
                        modifier = Modifier,//.absoluteOffset { IntOffset(50,160) },
                        value = textSocket,

                        onValueChange = {
                            textSocket = it
                            websockifySocket = it
                        },
                        //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.),
                        textStyle = TextStyle.Default,
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colors.primary)
                                    .padding(5.dp)
                            ) {
                                innerTextField()
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
                item {
                    Button(
                        onClick = {
                            showDialog = false
                        }, modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_action_done_icon),
                            contentDescription = "Done",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
        }
    }
}


