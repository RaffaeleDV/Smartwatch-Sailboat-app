package com.example.sailboatapp.presentation.ui.screen

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.view.MotionEvent
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.wear.compose.foundation.SwipeToDismissBoxState
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.widget.SwipeDismissFrameLayout
import com.example.sailboatapp.R
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker


private var destination: Marker? = null
private var ship: Marker? = null
private var anchor: Marker? = null


@SuppressLint("ClickableViewAccessibility")
@Composable
fun Test() {


    val context = LocalContext.current
    // Configure OSMDroid
    Configuration.getInstance()
        .load(context, context.getSharedPreferences("osmdroid", MODE_PRIVATE))

    val localTileSource = XYTileSource(
        "LocalServer",
        0,
        18,
        256,
        ".png",
        arrayOf("http://' + $raspberryIp + ':8081/data/OAM-W1-8-EPmid9-13-J70/"),
        "© OpenStreetMap contributors"
    )

    var mapView: MapView? by remember { mutableStateOf(null) }


    // AndroidView to embed the MapView

    AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->

        /*val swipeDismissFrameLayout = SwipeDismissFrameLayout(context)
        swipeDismissFrameLayout.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )*/

        val mapview = MapView(ctx)

        mapview.setTileSource(TileSourceFactory.MAPNIK)
        mapview.setMultiTouchControls(true)
        mapview.setBuiltInZoomControls(false)
        mapview.controller.setZoom(10.0)
        mapview.controller.setCenter(
            GeoPoint(
                26.14234000, -81.89596000
            )
        ) // Example: London coordinates
        //Ship marker
        if (ship == null) {
            ship = Marker(mapview)
        }
        ship?.position = GeoPoint(26.14234000, -82.00596000)
        //ship?.title = "title"
        //ship?.snippet = "description"


        ship?.rotation = 90.0f
        ship?.alpha = 1f
        //marker.setVisible(false)
        ship?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapview.overlays.add(ship)
        mapview.invalidate()

        /*swipeDismissFrameLayout.addView(mapview)
        swipeDismissFrameLayout.isSwipeable = false*/

        mapview.systemUiVisibility = WindowManager.LayoutParams.FLAG_FULLSCREEN

        //mapview.isClickable = false

        mapview.isEnabled = false

        /*mapView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.performClick()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> false

            }
            false // Let the MapView handle the touch event
        }*/



        mapview


    })


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