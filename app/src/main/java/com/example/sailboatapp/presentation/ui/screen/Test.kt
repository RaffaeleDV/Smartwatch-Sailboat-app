package com.example.sailboatapp.presentation.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


private var destination: Marker? = null
private var ship: Marker? = null
private var anchor: Marker? = null


@SuppressLint("ClickableViewAccessibility")
@Composable
fun Test() {

    val scrollState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .onRotaryScrollEvent {
                    if (LOG_ENABLED) Log.d("DEBUG", "Rotatory event")
                    coroutineScope.launch {
                        scrollState.scrollBy(it.verticalScrollPixels)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            state = scrollState,
            verticalArrangement = Arrangement.Center
        ) {
            items(100) { index ->
                Text(text = index.toString())
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }


}

fun addOrUpdateMarker(
    mapView: MapView, latitude: Double, longitude: Double, title: String, description: String
) {
    if (destination == null) {
        println("new destination")
        // Create new marker if it doesn't exist
        destination = Marker(mapView)
        destination!!.position = GeoPoint(latitude, longitude)
        destination!!.title = title
        destination!!.snippet = description
        destination!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(destination)
        mapView.invalidate()
    } else {
        // Update existing marker position
        println("update destination")
        destination!!.position = GeoPoint(latitude, longitude)
        mapView.invalidate()
    }
}