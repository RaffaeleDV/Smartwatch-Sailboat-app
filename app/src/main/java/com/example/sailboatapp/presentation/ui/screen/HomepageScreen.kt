package com.example.sailboatapp.presentation.ui.screen

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.dialog.Dialog
import com.example.sailboatapp.R
import com.example.sailboatapp.presentation.MainActivity
import com.example.sailboatapp.presentation.data.readNMEA
import com.example.sailboatapp.presentation.model.Raffica
import com.example.sailboatapp.presentation.network.ConnectionState
import com.example.sailboatapp.presentation.network.InstantiateViewModel
import com.example.sailboatapp.presentation.network.ServerConfig.RASPBERRY_IP_DEFAULT
import com.example.sailboatapp.presentation.network.ServerConfig.WEBSOCKIFY_SOCKET_DEFAULT
import com.example.sailboatapp.presentation.ui.DEGREE_SYMBOL
import com.example.sailboatapp.presentation.ui.KNOT_SYMBOL
import com.example.sailboatapp.presentation.ui.connectionState
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class LocalConnection {
    private var localConnectionState: Boolean = false
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

const val LOG_ENABLED = true

var raspberryIp = RASPBERRY_IP_DEFAULT //Raspberry ip
var websockifySocket = WEBSOCKIFY_SOCKET_DEFAULT

fun saveString(context: Context, key: String, value: String) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.putString(key, value)
    editor.apply()
}

fun getString(context: Context, key: String): String? {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, null)
}

//var localViewModel: LocalViewModel? = null
//var remoteViewModel: RemoteViewModel = RemoteViewModel()

var nmeaDataRemote = HashMap<String, String>()
var nmeaDataLocal: State<HashMap<String, String>>? = null

var stimeVelocita = JsonObject()

@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun Homepage(
    navController: NavHostController,
    mainActivity: MainActivity,
    onSwipeChange: (Boolean) -> Unit
) {

    onSwipeChange(false)
    //Change config ip (store & read)
    val context = LocalContext.current
    val savedIp = getString(LocalContext.current, "ip")
    if (LOG_ENABLED) Log.d("DEBUG", "savedIp: $savedIp")
    //println("Saved IP= $savedIp")

    val regex = Regex("^\\d+\\.\\d+\\.\\d+\\.\\d+$")

    if (savedIp != null && savedIp != "" && regex.matches(savedIp)) {
        raspberryIp = savedIp
    }
    val savedSocket = getString(LocalContext.current, "socket")
    if (LOG_ENABLED) Log.d("DEBUG", "Saved socket= $savedIp")
    if (savedSocket != null && savedIp != "") {
        websockifySocket = savedSocket
    }
    if (LOG_ENABLED) Log.d("DEBUG", "Base url: $raspberryIp")
    if (LOG_ENABLED) Log.d("DEBUG", "Base socket: $websockifySocket")

    var lastVelVento = "0.0"
    var lastMaxWindSpeed = "0.0"
    var lastShipDirection = "0.0"

    var raffica = Raffica("", "", "")

    if (connectionState == ConnectionState.Local) {

        val localViewModel = InstantiateViewModel.instantiateLocalViewModel()

        //Raffica local
        val rafficaUiState: RafficaUiState = localViewModel.rafficaUiState

        when (rafficaUiState) {
            is RafficaUiState.Error -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Error local raffica connection"
            )

            is RafficaUiState.Loading -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Loading local raffica connection"
            )

            is RafficaUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                if (LOG_ENABLED) Log.d("DEBUG", "Success: local connection Raffica")
                raffica = (localViewModel.rafficaUiState as RafficaUiState.Success).raffica
                if (LOG_ENABLED) Log.d("DEBUG", "Connessione locale: raffica")
            }
        }

        //Neam sentence local from websocket
        nmeaDataLocal = localViewModel.data.collectAsState()

    } else if (connectionState == ConnectionState.Remote) {

        //Nmea sentence remote
        val remoteViewModel = InstantiateViewModel.instantiateRemoteViewModel()

        val remoteUiState: RemoteUiState = remoteViewModel.remoteUiState
        when (remoteUiState) {
            is RemoteUiState.Error -> {
                if (LOG_ENABLED) Log.d("DEBUG", "Error remote connection nmea")
            }

            is RemoteUiState.Loading -> if (LOG_ENABLED) Log.d("DEBUG", "Loading remote connection")
            is RemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)

                if (LOG_ENABLED) Log.d("DEBUG", "Success: Remote connection nmea")
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

    val focusRequester = rememberActiveFocusRequester()

    val coroutineScope = rememberCoroutineScope()

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
        }) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight()
                .onRotaryScrollEvent {
                    if (LOG_ENABLED) Log.d("DEBUG", "Rotatory event")
                    coroutineScope.launch {
                        if (LOG_ENABLED) Log.d("DEBUG", "Rotatory coroutine")
                        listState.scrollBy(it.verticalScrollPixels)
                    }
                    true // it means that we are handling the event with this callback
                }
                .focusRequester(focusRequester)
                .focusable(),
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
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 15.sp,
                    text = (

                            if (connectionState == ConnectionState.Remote) {
                                if (nmeaDataRemote["windSpeed"].isNullOrEmpty()) {
                                    "$lastVelVento $KNOT_SYMBOL"
                                } else {
                                    "${nmeaDataRemote["windSpeed"]!!} $KNOT_SYMBOL"
                                }
                            } else {
                                if (nmeaDataLocal?.value != null) {
                                    if (nmeaDataLocal?.value?.get("windSpeed") != null) {
                                        if (nmeaDataLocal?.value?.get("windSpeed") == "0.0") {
                                            "$lastVelVento $KNOT_SYMBOL"
                                        } else {
                                            lastVelVento = nmeaDataLocal?.value?.get("windSpeed")!!
                                            "$lastVelVento $KNOT_SYMBOL"
                                        }
                                    } else {
                                        "$lastVelVento $KNOT_SYMBOL"
                                    }
                                } else {
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
                        if (raffica.velVento == "0.0" || raffica.velVento == "") {
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
                    text = (
                            if (connectionState == ConnectionState.Remote) {
                                if (nmeaDataRemote["shipDirection"].isNullOrEmpty()) {
                                    "$lastShipDirection$DEGREE_SYMBOL"
                                } else {
                                    "${nmeaDataRemote["shipDirection"]!!}$DEGREE_SYMBOL"
                                }
                            } else {
                                if (nmeaDataLocal?.value?.get("shipDirection") != null) {
                                    if (nmeaDataLocal?.value?.get("shipDirection") == "0.0") {
                                        "$lastShipDirection$DEGREE_SYMBOL"
                                    } else {
                                        lastShipDirection =
                                            nmeaDataLocal?.value?.get("shipDirection")!!
                                        "$lastShipDirection$DEGREE_SYMBOL"
                                    }
                                } else {
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
            /*item {
                Button(//Test page button
                    onClick = { navController.navigate("test") },
                    modifier = Modifier.height(30.dp)
                    //.width(150.dp)
                ) {
                    Text("Test")
                }
            }*/
            item { Spacer(modifier = Modifier.height(10.dp)) }
            item {
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

        LaunchedEffect(Unit) {
            if (LOG_ENABLED) Log.d("DEBUG", "Rotatory focus")
            focusRequester.requestFocus()
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
                    textIp = raspberryIp
                    BasicTextField(
                        modifier = Modifier,//.absoluteOffset { IntOffset(50,160) },
                        value = textIp,
                        onValueChange = {
                            textIp = it
                            if (textIp == "0") {
                                raspberryIp = RASPBERRY_IP_DEFAULT
                                saveString(context, "ip", RASPBERRY_IP_DEFAULT)
                            } else {
                                raspberryIp = it
                                saveString(context, "ip", raspberryIp)
                            }
                        },
                        textStyle = TextStyle.Default,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
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
                    textSocket = websockifySocket
                    BasicTextField(
                        modifier = Modifier,//.absoluteOffset { IntOffset(50,160) },
                        value = textSocket,
                        onValueChange = {
                            textSocket = it
                            if (textSocket == "0") {
                                websockifySocket = WEBSOCKIFY_SOCKET_DEFAULT
                                saveString(context, "socket", WEBSOCKIFY_SOCKET_DEFAULT)
                            } else {
                                websockifySocket = it
                                saveString(context, "socket", websockifySocket)
                            }
                        },
                        //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.),
                        textStyle = TextStyle.Default,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
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
                            mainActivity.recreate()
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


