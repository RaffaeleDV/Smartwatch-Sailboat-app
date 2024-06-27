package com.example.sailboatapp.presentation.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.sailboatapp.presentation.network.Raffica
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
fun checkLocalConnection(): Boolean {
    return locCon.getConnectionState()
}

fun setLocalConnection(state: Boolean) {
    locCon.setConnectionState(state)
}

enum class ConnectionState {
    Local, Remote, Loading
}

var BASE_URL = "192.168.178.48" //Raspberry ip

@Composable
fun Homepage(
    navController: NavHostController,
    isSwippeEnabled: Boolean,
    onSwipeChange: (Boolean) -> Unit
) {

    println("Base url: $BASE_URL")

    onSwipeChange(true)

    var lastVelVento = "0.0"
    var lastMaxWindSpeed = "0.0"
    var lastShipDirection = "0.0"

    var connectionState: ConnectionState by remember { mutableStateOf(ConnectionState.Loading) }

    val localViewModel: LocalViewModel = viewModel()

    var rafficaUiState: RafficaUiState = localViewModel.rafficaUiState
    var raffica = Raffica("", "", "")
    when (rafficaUiState) {
        is RafficaUiState.Error -> println("Error local raffica connection")
        is RafficaUiState.Loading -> println("Loading local raffica connection")
        is RafficaUiState.Success -> {
            //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
            println("Success: local connection Raffica")
            raffica = (localViewModel.rafficaUiState as RafficaUiState.Success).raffica
        }
    }

    var nmeaData = localViewModel.data.collectAsState()
    var nmeaDataRemote = HashMap<String, String>()
    connectionState = ConnectionState.Local
    if (!checkLocalConnection()) {
        val remoteViewModel: RemoteViewModel = viewModel()
        val remoteUiState: RemoteUiState = remoteViewModel.remoteUiState
        connectionState = ConnectionState.Remote
        when (remoteUiState) {
            is RemoteUiState.Error -> println("Error remote connection")
            is RemoteUiState.Loading -> println("Loading remote connection")
            is RemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                println("Success: Remote connection")
                nmeaDataRemote =
                    readNMEA((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
            }
        }
    }

    val maxPages = 3
    val selectedPage by remember { mutableIntStateOf(1) }
    var finalValue by remember { mutableIntStateOf(0) }

    val animatedSelectedPage by animateFloatAsState(
        targetValue = selectedPage.toFloat(), label = "",
    ) {
        finalValue = it.toInt()
    }

    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = animatedSelectedPage - finalValue
            override val selectedPage: Int
                get() = finalValue
            override val pageCount: Int
                get() = maxPages
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
                    text = (if (nmeaData.value["windSpeed"] == null) {
                        if (nmeaDataRemote["windSpeed"].isNullOrEmpty()) {
                            "$lastVelVento $KNOT_SYMBOL"
                        } else {
                            "${nmeaDataRemote["windSpeed"]!!} $KNOT_SYMBOL"
                        }
                    } else {
                        if (nmeaData.value["windSpeed"]!! == "0.0") {
                            "$lastVelVento $KNOT_SYMBOL"
                        } else {
                            lastVelVento = nmeaData.value["windSpeed"]!!
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
                    text = (if (nmeaData.value["shipDirection"] == null) {

                        if (nmeaDataRemote["shipDirection"].isNullOrEmpty()) {
                            "$lastShipDirection $DEGREE_SYMBOL"
                        } else {
                            "${nmeaDataRemote["shipDirection"]!!} $DEGREE_SYMBOL"
                        }
                    } else {
                        if (nmeaData.value["shipDirection"]!! == "0.0") {
                            "$lastShipDirection $DEGREE_SYMBOL"
                        } else {
                            lastShipDirection = nmeaData.value["shipDirection"]!!
                            "$lastShipDirection $DEGREE_SYMBOL"
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

            /*item {
                Button(//Test page button
                    onClick = { navController.navigate("test") },
                    modifier = Modifier.height(30.dp)
                    //.width(150.dp)
                ) {
                    Text("Test")
                }
            }*/
        }

        var textState by remember { mutableStateOf("") }
        Dialog(showDialog = showDialog, onDismissRequest = { showDialog = false }) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "IP raspberry: ")
                Spacer(modifier = Modifier.height(10.dp))
                BasicTextField(modifier = Modifier,//.absoluteOffset { IntOffset(50,160) },
                    value = textState,
                    onValueChange = {
                        textState = it
                        BASE_URL = it
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
                    })
                Spacer(modifier = Modifier.height(20.dp))
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
    }/*Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )*/
}


