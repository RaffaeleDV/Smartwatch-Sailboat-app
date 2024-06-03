package com.example.sailboatapp.presentation.ui.screen

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import com.example.sailboatapp.R
import com.example.sailboatapp.presentation.data.readNMEA
import com.example.sailboatapp.presentation.network.Anchor
import com.example.sailboatapp.presentation.orange
import com.example.sailboatapp.presentation.red
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun getDistanceBetweenPoints(
    latitude1: String,
    longitude1: String,
    latitude2: String,
    longitude2: String,
    unit: String
): Double {
    //println("2input data= "+latitude1+" "+longitude1+ " "+latitude2+" "+longitude2)
    val R = 6371.0 // Radius of the Earth in kilometers
    val latDistance = Math.toRadians(latitude1.toDouble() - latitude2.toDouble())
    var lonDistance = Math.toRadians(longitude1.toDouble() - longitude2.toDouble())
    val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(Math.toRadians(latitude1.toDouble())) * cos(Math.toRadians(latitude2.toDouble())) *
            sin(lonDistance / 2) * sin(lonDistance / 2)
    val c = asin(sqrt(a))

    if (unit.equals("kilometers")) {
        return 2 * R * c;
    }
    return -1.0
}

//trasformo i kilometri in metri
fun getDistanceBetweenPointsMeters(
    latitude1: String,
    longitude1: String,
    latitude2: String,
    longitude2: String,
    unit: String = "kilometers"
): Int {
    //println("1input data= "+latitude1+" "+longitude1+ " "+latitude2+" "+longitude2)
    return (getDistanceBetweenPoints(
        latitude1,
        longitude1,
        latitude2,
        longitude2,
        unit = "kilometers"
    ) * 1000).toInt();
}

var anchorDistanceLimitMeters: Double = 100.0
fun checkAnchorDistance(anchorLatLng: LatLng, shipLatLng: LatLng): Boolean {
    //println("input data check= "+anchorLatLng.toString()+shipLatLng.toString())
    val distance = getDistanceBetweenPointsMeters(
        anchorLatLng.latitude.toString(),
        anchorLatLng.longitude.toString(),
        shipLatLng.latitude.toString(),
        shipLatLng.longitude.toString(),
        "kilometers"
    )
    //val b = BigDecimal(distance).toPlainString()
    println("Anchor distance: " + distance)
    if (distance > anchorDistanceLimitMeters) {
        return true
    }
    return false
}


@Composable
fun Map(navController: NavHostController) {

    //val ok = getDistanceBetweenPointsMeters("45.4641943", "9.1896346", "40.8358846", "14.2487679")
    //println("Napoli milano: " + ok)
    var ship_position by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var destination_position by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var destination_line = ArrayList<LatLng>()
    destination_line.add(ship_position)
    destination_line.add(destination_position)

    var anchor_visibility by remember { mutableStateOf(false) }
    var destination_position_visibility by remember { mutableStateOf(false) }
    var destination_line_visibility by remember { mutableStateOf(false) }
    var map_visibility by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    var color_anchor by remember { mutableStateOf(orange) }

    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ship_position, 10f)
    }

    var connectionState by remember { mutableStateOf("") }

    val localViewModel: LocalViewModel = viewModel()

    var anchorLocal: Anchor = Anchor("0.0", "0.0", "-1", "")
    var anchorRemote = ""
    var anchorRemoteObj = Anchor("0.0", "0.0", "-1", "")
    //Anchor Local
    var anchorLocalUiState: AnchorLocalUiState = localViewModel.anchorUiState

    when (anchorLocalUiState) {
        is AnchorLocalUiState.Error -> println("Error local anchor")
        is AnchorLocalUiState.Loading -> println("Loading local anchor")
        is AnchorLocalUiState.Success -> {
            //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
            println("Success: Local connection anchor")
            anchorLocal = (localViewModel.anchorUiState as AnchorLocalUiState.Success).anchor
            println(anchorLocal.anchored)
        }
    }

    if (!checkLocalConnection()) {
        val remoteViewModel: RemoteViewModel = viewModel()
        //Anchor Remote
        val anchorRemoteUiState: AnchorRemoteUiState = remoteViewModel.anchorRemoteUiState
        connectionState = "Remote"
        when (anchorRemoteUiState) {
            is AnchorRemoteUiState.Error -> println("Error remote anchor")
            is AnchorRemoteUiState.Loading -> println("Loading remote anchor")
            is AnchorRemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                println("Success: Remote connection anchor")
                anchorRemote =
                    (remoteViewModel.anchorRemoteUiState as AnchorRemoteUiState.Success).anchor
                println(anchorRemote)
                var list = anchorRemote.split(" ")
                println(list.toString())
                println("." + list[2] + ".")
                anchorRemoteObj =
                    Anchor(
                        String.format("%.7f", list[0].toDouble()),
                        String.format("%.7f", list[1].toDouble()), list[2], list[3]
                    )
            }
        }
    }

    //set anchor marker
    if (anchorLocal.anchored.isNullOrEmpty() || anchorLocal.anchored.equals("-1")) {
        println("set remote anchor")
        if (anchorRemoteObj.anchored.isNullOrEmpty()) {

        } else {
            if (anchorRemoteObj.anchored.equals("-1")) {
                anchorRemoteObj
            } else {
                if (anchorRemoteObj.anchored.equals("1")) {
                    anchor_visibility = true
                    color_anchor = red
                } else if (anchorRemoteObj.anchored.equals("0")) {
                    anchor_visibility = false
                    color_anchor = orange
                }
            }
        }
    } else {
        println("set local anchor1")
        if (anchorLocal.anchored.equals("1")) {
            anchor_visibility = true
            color_anchor = red
        } else if (anchorLocal.anchored.equals("0")) {
            anchor_visibility = false
            color_anchor = orange
        }
        //println(ship_position.toString())
    }
    //nmeaData local
    var nmeaData = localViewModel.data.collectAsState()
    var nmeaDataRemote = HashMap<String, String>()
    connectionState = "Local"

    if (!checkLocalConnection()) {
        //NmeaData remote
        val remoteViewModel: RemoteViewModel = viewModel()
        val remoteUiState: RemoteUiState = remoteViewModel.remoteUiState
        connectionState = "Remote"
        when (remoteUiState) {
            is RemoteUiState.Error -> println("Error")
            is RemoteUiState.Loading -> println("Loading")
            is RemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                println("Success: Remote connection")
                nmeaDataRemote =
                    readNMEA((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
            }
        }
    }

    //Ship Position
    if (nmeaData.value.get("latitude").isNullOrEmpty() && nmeaData.value.get("longitude")
            .isNullOrEmpty()
    ) {
        if (nmeaDataRemote.get("latitude").isNullOrEmpty() && nmeaData.value.get("longitude")
                .isNullOrEmpty()
        ) {
            ship_position
        } else {
            println("Ship position remote")
            ship_position = LatLng(
                nmeaDataRemote.get("latitude")!!.toDouble(),
                nmeaDataRemote.get("longitude")!!.toDouble()
            )
            println(anchorRemoteObj.latitude + " " + anchorRemoteObj.longitude + "  " + ship_position.toString())
            println(
                checkAnchorDistance(
                    LatLng(
                        anchorRemoteObj.latitude.toDouble(),
                        anchorRemoteObj.longitude.toDouble()
                    ), ship_position
                )
            )
        }
    } else {
        println("Ship position local")
        if (nmeaData.value.get("latitude")!!.equals("0.0")) {
            ship_position
        } else {
            ship_position = LatLng(
                nmeaData.value.get("latitude")!!.toDouble(),
                nmeaData.value.get("longitude")!!.toDouble()
            )
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(ship_position, 10f)
            }
            println(anchorLocal.latitude + " " + anchorLocal.longitude + "  " + ship_position.toString())
            println(
                checkAnchorDistance(
                    LatLng(
                        anchorLocal.latitude.toDouble(),
                        anchorLocal.longitude.toDouble()
                    ), ship_position
                )
            )
            if (checkAnchorDistance(
                    LatLng(
                        anchorLocal.latitude.toDouble(),
                        anchorLocal.longitude.toDouble()
                    ), ship_position
                )
            ) {
                val context = LocalContext.current
                /*
                var v = context.getSystemService(Vibrator::class.java) as Vibrator
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                */
                var mp: MediaPlayer = MediaPlayer.create(context, R.raw.notification)
                mp.setOnCompletionListener {
                    it.release()
                }
                mp.start()
            }
        }
        //println(ship_position.toString())
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        //autoCentering = AutoCenteringParams(itemIndex = 0),
        //state = listState
    )
    {
        Dialog(
            showDialog = showDialog,
            onDismissRequest = { showDialog = false }) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item { Text(text = "Rotta: ") }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item { Text(text = "TWS:") }
                item { Text(text = "TWA:") }
                item { Text(text = "COG:") }
                item { Text(text = "SOG:") }
                item { Text(text = "Wind direction:") }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item { Text(text = "Sugg. Route:") }
                item { Text(text = "Sugg. sail conf.:") }
                item { Text(text = "Estimated Speed:") }
                item { Text(text = "Estimated VMG:") }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item {
                    Button(
                        onClick = {
                            showDialog = false
                        },
                        modifier = Modifier
                            .size(30.dp)
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
        GoogleMap(
            mergeDescendants = true,
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                zoomGesturesEnabled = true,
                zoomControlsEnabled = false
            ),
            properties = MapProperties(
                //isMyLocationEnabled = true,
            ),
            onMapLongClick = {
                destination_position_visibility = true
                destination_line_visibility = true
                destination_position = it
            }
        ) {
            Marker(
                state = MarkerState(position = ship_position),
                rotation = 90f,
                anchor = Offset(0.5f, 0.5f),
                alpha = 10F,
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_ship_marker),
                title = "Nave",
                //snippet = "Descrizione\n Vento: 10"
            )
            Marker(
                state = MarkerState(position = ship_position),
                rotation = 0f,
                anchor = Offset(0.5f, 0.5f),
                alpha = 10F,
                visible = anchor_visibility,
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_anchor),
                title = "Ancora",
                //snippet = "Descrizione",
                zIndex = 1F
            )
            Marker(
                state = MarkerState(position = destination_position),
                rotation = 0f,
                //anchor = Offset(0.5f, 0.5f),
                visible = destination_position_visibility,
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_destination_icon),
                title = "Destinazione",
                //snippet = "Descrizione",
                zIndex = 1F
            )
            Polyline(
                points = destination_line,
                visible = destination_line_visibility,
                width = 5F,
                color = Color.Red
            )
        }
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (leftButton, centerButton, rightButton, bottomButton) = createRefs()
            Button(//Anchor Button
                onClick = {
                    if (anchor_visibility)
                        anchor_visibility = false
                    else
                        anchor_visibility = true
                    if (color_anchor == orange)
                        color_anchor = red
                    else
                        color_anchor = orange
                    //navController.navigate("off")
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(color_anchor), // Background color
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    //.absoluteOffset { IntOffset(5, 160) }
                    .size(30.dp)
                    .constrainAs(leftButton){
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 5.dp)
                    }
            ) {
                //Text("On")
                Icon(
                    painter = painterResource(id = R.drawable.ic_action_anchor_icon),
                    contentDescription = "Anchor",
                    modifier = Modifier.size(15.dp)
                )
            }
            Button(//Direction Button
                onClick = {
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(orange), // Background color
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    //.absoluteOffset { IntOffset(160, 320) }
                    //.offset(70.dp, 160.dp)
                    .size(30.dp)
                    .constrainAs(bottomButton) {
                        bottom.linkTo(parent.bottom, margin = 5.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                //Text("On")
                Icon(
                    painter = painterResource(id = R.drawable.ic_action_direction_icon),
                    contentDescription = "Anchor",
                    modifier = Modifier.size(15.dp)
                )
            }
            Button(//Reset position
                onClick = {
                    cameraPositionState.move(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(
                                ship_position,
                                10f
                            )
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(orange), // Background color
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    //.absoluteOffset { IntOffset(320, 165) }
                    //.offset(70.dp, 160.dp)
                    .size(30.dp)
                    .constrainAs(rightButton) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 5.dp)
                    }
            ) {
                //Text("On")
                Icon(
                    painter = painterResource(id = R.drawable.ic_action_reset_position_icon),
                    contentDescription = "reset position",
                    modifier = Modifier.size(15.dp)
                )
            }

        }


    }
}