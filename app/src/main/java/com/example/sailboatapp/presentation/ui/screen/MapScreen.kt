package com.example.sailboatapp.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog

import com.example.sailboatapp.R
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

@Composable
fun Map(navController: NavHostController){
    var ship_position  by remember { mutableStateOf( LatLng(40.6, 14.75)) }
    var destination_position by remember { mutableStateOf( LatLng(0.0, 0.0)) }
    var destination_line = ArrayList<LatLng>()
    destination_line.add(ship_position)
    destination_line.add(destination_position)

    var anchor_visibility by remember { mutableStateOf(false) }
    var destination_position_visibility by remember { mutableStateOf(false) }
    var destination_line_visibility by remember { mutableStateOf(false) }
    var map_visibility by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    var color_anchor by remember { mutableStateOf(orange) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ship_position, 10f)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        //autoCentering = AutoCenteringParams(itemIndex = 0),
        //state = listState
    )
    {
        Dialog(
            showDialog = showDialog,
            onDismissRequest = { showDialog = false}) {
            ScalingLazyColumn (
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
        if(map_visibility) {
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
                    .absoluteOffset { IntOffset(5, 160) }
                    .size(30.dp)
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
                    .absoluteOffset { IntOffset(160, 320) }
                    //.offset(70.dp, 160.dp)
                    .size(30.dp)
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
                    .absoluteOffset { IntOffset(320, 165) }
                    //.offset(70.dp, 160.dp)
                    .size(30.dp)
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