package com.example.sailboatapp.presentation.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.edgeSwipeToDismiss
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import com.example.sailboatapp.R
import com.example.sailboatapp.presentation.model.Anchor
import com.example.sailboatapp.presentation.network.ConnectionState
import com.example.sailboatapp.presentation.network.InstantiateViewModel
import com.example.sailboatapp.presentation.orange
import com.example.sailboatapp.presentation.red
import com.example.sailboatapp.presentation.ui.DEGREE_SYMBOL
import com.example.sailboatapp.presentation.ui.KNOT_SYMBOL
import com.example.sailboatapp.presentation.ui.connectionState
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun trovaAngoli(windAngles: JsonArray, destinationDirection: Double): Array<Array<Int>> {
    var angolo1 = 0
    var angolo2 = 0
    var indice1 = 0
    var indice2 = 0
    val sizeAngoli = windAngles.size()

    if (destinationDirection < 52) {
        angolo1 = 52
        angolo2 = 52
        indice1 = 0
        indice2 = 0
    } else if (destinationDirection > 150) {
        angolo1 = 150
        angolo2 = 150
        indice1 = 7
        indice2 = 7
    }
    windAngles.forEachIndexed(action = { i, _ ->
        val angoloCorrente = windAngles[i]
        val angoloSuccessivo = windAngles[(i + 1) % sizeAngoli]
        if (angoloCorrente.asInt <= destinationDirection && angoloSuccessivo.asInt >= destinationDirection) {
            angolo1 = angoloCorrente.asInt
            angolo2 = angoloSuccessivo.asInt
            indice1 = i
            indice2 = (i + 1) % sizeAngoli
        }
    })
    val angoli = arrayOf(angolo1, angolo2)
    val indici = arrayOf(indice1, indice2)
    val angoliIndex = arrayOf(angoli, indici)
    return angoliIndex
}

fun trovaVelocita(windSpeeds: JsonArray, windSpeed: Double): Array<Array<Int>> {
    var v1 = -1
    var v2 = -1
    var indice1 = -1
    var indice2 = -1
    val sizeVelocita = windSpeeds.size()

    windSpeeds.forEachIndexed(action = { i, _ ->
        val vCorrente = windSpeeds[i]
        val vSuccessivo = windSpeeds[(i + 1) % sizeVelocita]

        if (windSpeed < 6 || windSpeed > 20) {
            v1 = vCorrente.asInt
            v2 = vCorrente.asInt
            indice1 = i
            indice2 = i
        }
        if (vCorrente.asInt <= windSpeed && vSuccessivo.asInt >= windSpeed) {
            v1 = vCorrente.asInt
            v2 = vSuccessivo.asInt
            indice1 = i
            indice2 = (i + 1) % sizeVelocita
        }


    })
    val velocita = arrayOf(v1, v2)
    val index = arrayOf(indice1, indice2)
    val velocitaIndex = arrayOf(velocita, index)

    return velocitaIndex
}

fun mediaPonderata(
    angoli: Array<Int>,
    destinationDirection: Double,
    stime: JsonArray,
    indiciAngoli: Array<Int>,
    velocita: Array<Int>,
    indiciVelocita: Array<Int>,
    windSpeed: Double
): Double {
    val distanza1 = abs(angoli[0] - destinationDirection)
    val distanza2 = abs(angoli[1] - destinationDirection)
    val peso1 = 1 - distanza1 / (distanza1 + distanza2)
    val peso2 = 1 - peso1
    val v1 = abs(velocita[0] - windSpeed)
    val v2 = abs(velocita[1] - windSpeed)
    val pesov1 = 1 - v1 / (v1 + v2)
    val pesov2 = 1 - pesov1

    val mediaPonderata =
        (peso1 * pesov1 * stime[indiciAngoli[0]].asJsonArray[indiciVelocita[0]].asInt + peso1 * pesov2 * stime[indiciAngoli[0]].asJsonArray[indiciVelocita[1]].asInt + peso2 * pesov1 * stime[indiciAngoli[1]].asJsonArray[indiciVelocita[0]].asInt + peso2 * pesov2 * stime[indiciAngoli[1]].asJsonArray[indiciVelocita[1]].asInt)
    return mediaPonderata
}

fun routeCalculator(
    destinationDirection: Double,
    windDirection: Double,
    windSpeed: Double,
    trueWindAngle: Double,
    stimeVelocita: JsonObject
): Array<String> {
    var maxAngle = -1
    var optimalSail = ""
    var vMax = -1.0
    var vMaxEff = -1.0

    val velocitas: ArrayList<Double> = arrayListOf()
    val velocitasEff: ArrayList<Double> = arrayListOf()


    stimeVelocita.keySet().forEach {

        if (it == "inProgress") {
            return arrayOf(maxAngle.toString(), it, vMax.toString(), vMaxEff.toString())
        }

        val vela = it
        val windAngles = stimeVelocita.getAsJsonObject(it).getAsJsonArray("angoliVento")
        val windSpeeds = stimeVelocita.getAsJsonObject(it).getAsJsonArray("velocitaVento")
        val stime = stimeVelocita.getAsJsonObject(it).getAsJsonArray("stimeVelocitaBarca")

        if (LOG_ENABLED) Log.d("DEBUG", "Vela: $vela")
        if (LOG_ENABLED) Log.d("DEBUG", "WindAngles: $windAngles")
        if (LOG_ENABLED) Log.d("DEBUG", "WindSpeeds: $windSpeeds")
        if (LOG_ENABLED) Log.d("DEBUG", "Stime: $stime")

        val angoliIndex = trovaAngoli(windAngles, destinationDirection)

        val velocitaIndex = trovaVelocita(windSpeeds, windSpeed)

        val vel = mediaPonderata(
            angoliIndex[0],
            destinationDirection,
            stime,
            angoliIndex[1],
            velocitaIndex[0],
            velocitaIndex[1],
            windSpeed
        )
        //velocitas.add(vel)

        if (vel < 0) {
            return arrayOf("vel < 0")
        }

        val distanza1 = abs(angoliIndex[0].asList()[0] - destinationDirection)
        val distanza2 = abs(angoliIndex[0].asList()[1] - destinationDirection)
        //val peso1 = 1 - distanza1 / (distanza1 + distanza2)
        //val peso2 = 1 - peso1
        var angoloSugg = -1
        if (distanza1 >= distanza2) {
            angoloSugg = angoliIndex[0].asList()[0]
        } else if (distanza2 > distanza1) {
            angoloSugg = angoliIndex[0].asList()[1]
        }
        //calcolo la velocità effettiva
        val vEffettiva = vel * cos(trueWindAngle)
        velocitasEff.add(vEffettiva)
        if (vEffettiva > vMax) {
            vMaxEff = vEffettiva
            optimalSail = it
            vMax = vel
            maxAngle = angoloSugg
        }
    }
    return arrayOf(maxAngle.toString(), optimalSail, vMax.toString(), vMaxEff.toString())
}


fun getTWA(windDirection: Double, shipDirection: Double): Double {
    var relativeWindDirection = windDirection - shipDirection
    //if(LOG_ENABLED)Log.d("DEBUG","relativeWindDirection: $relativeWindDirection")
    //Normalize the angle
    if (relativeWindDirection < 0) {
        relativeWindDirection += 360.0
    } else if (relativeWindDirection > 360) {
        relativeWindDirection -= 360.0
    }
    //if(LOG_ENABLED)Log.d("DEBUG","twa pre adjust: $relativeWindDirection")
    //Adjuste the range
    if (relativeWindDirection > 180) {
        relativeWindDirection = 360.0 - relativeWindDirection
    }
    return relativeWindDirection
}

fun angleBetweenPoints(
    fromLatitude: String, fromLongitude: String, toLatitude: String, toLongitude: String
): Double {
    val x = toLatitude.toDouble() - fromLatitude.toDouble()
    val y = toLongitude.toDouble() - fromLongitude.toDouble()

    var angle = atan2(y, x)
    angle = Math.toDegrees(angle)

    // Assicuriamoci che l'angolo sia positivo
    if (angle < 0) {
        angle += 360
    }
    // Aggiungiamo l'offset per orientare correttamente l'angolo
    val orientamento = 90 // Nord è 90 gradi
    var angleDef = (angle - orientamento) % 360
    if (angleDef < 0) {
        angleDef += 360
    }
    return angleDef
}


fun getDistanceBetweenPoints(
    latitude1: String, longitude1: String, latitude2: String, longitude2: String, unit: String
): Double {
    //if(LOG_ENABLED)Log.d("DEBUG","2input data= "+latitude1+" "+longitude1+ " "+latitude2+" "+longitude2)
    val r = 6371.0 // Radius of the Earth in kilometers
    val latDistance = Math.toRadians(latitude1.toDouble() - latitude2.toDouble())
    val lonDistance = Math.toRadians(longitude1.toDouble() - longitude2.toDouble())
    val a =
        sin(latDistance / 2) * sin(latDistance / 2) + cos(Math.toRadians(latitude1.toDouble())) * cos(
            Math.toRadians(latitude2.toDouble())
        ) * sin(lonDistance / 2) * sin(lonDistance / 2)
    val c = asin(sqrt(a))

    if (unit == "kilometers") {
        return 2 * r * c
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
    //if(LOG_ENABLED)Log.d("DEBUG","1input data= "+latitude1+" "+longitude1+ " "+latitude2+" "+longitude2)
    return (getDistanceBetweenPoints(
        latitude1, longitude1, latitude2, longitude2, unit
    ) * 1000).toInt()
}

var anchorDistanceLimitMeters: Double = 20.0
fun checkAnchorDistance(anchorLatLng: LatLng, shipLatLng: LatLng): Boolean {
    //if(LOG_ENABLED)Log.d("DEBUG","input data check= "+anchorLatLng.toString()+shipLatLng.toString())
    val distance = getDistanceBetweenPointsMeters(
        anchorLatLng.latitude.toString(),
        anchorLatLng.longitude.toString(),
        shipLatLng.latitude.toString(),
        shipLatLng.longitude.toString(),
        "kilometers"
    )
    //val b = BigDecimal(distance).toPlainString()
    if (LOG_ENABLED) Log.d("DEBUG", "Anchor distance: $distance")
    if (distance > anchorDistanceLimitMeters) {
        return true
    }
    return false
}

private var destination: Marker? = null
private var ship: Marker? = null
private var anchor: Marker? = null
var mapView: MapView? = null
var cameraUpdate: Int = 0

@SuppressLint("ClickableViewAccessibility")
@Composable
fun Map(
    navController: NavHostController,
    onSwipeChange: (Boolean) -> Unit
) {

    onSwipeChange(false)
    //val ok = getDistanceBetweenPointsMeters("45.4641943", "9.1896346", "40.8358846", "14.2487679")
    //if(LOG_ENABLED)Log.d("DEBUG","Napoli milano: " + ok)
    var shipPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var shipDirection by remember { mutableDoubleStateOf(0.0) }
    var destinationPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var destinationDirection by remember { mutableDoubleStateOf(0.0) }
    /*val destinationLine = ArrayList<LatLng>()
    destinationLine.add(shipPosition)
    destinationLine.add(destinationPosition)*/

    var windDirection by remember { mutableDoubleStateOf(0.0) }
    var windSpeed by remember { mutableDoubleStateOf(0.0) }
    var speedOverGround by remember { mutableDoubleStateOf(0.0) }
    var courseOverGround by remember { mutableDoubleStateOf(0.0) }
    var trueWindAngle by remember { mutableDoubleStateOf(0.0) }

    //var stimeVelocita = JsonObject()

    //Anchor marker variables
    var anchorVisibility by remember { mutableStateOf(false) }
    /*var destinationPositionVisibility by remember { mutableStateOf(false) }
    var destinationLineVisibility by remember { mutableStateOf(false) }*/
    var directionButtonVisibility by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }


    //Anchor variables
    var anchorLocal = Anchor("0.0", "0.0", "-1", "")
    var anchorRemote = ""
    var anchorRemoteObj = Anchor("0.0", "0.0", "-1", "")
    var anchorDistanceMeters by remember { mutableIntStateOf(0) }
    var colorAnchor by remember { mutableLongStateOf(orange) }




    if (connectionState == ConnectionState.Local) {
        val localViewModel = InstantiateViewModel.instantiateLocalViewModel()

        //Stime velocita local
        val stimeVelocitaUiState: StimeVelocitaUiState = localViewModel.stimeVelocitaUiState

        when (stimeVelocitaUiState) {
            is StimeVelocitaUiState.Error -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Error stime velocita local"
            )

            is StimeVelocitaUiState.Loading -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Loading stime velocita local"
            )

            is StimeVelocitaUiState.Success -> {

                val result =
                    (localViewModel.stimeVelocitaUiState as StimeVelocitaUiState.Success).stimeVelocita
                if (LOG_ENABLED) Log.d("DEBUG", "Success: Stime velocita local $result")

                stimeVelocita = result
                if (LOG_ENABLED) Log.d("DEBUG", "Connessione locale: stime velocità")

                /*result.keySet().forEach{
                }*/
                //if(LOG_ENABLED)Log.d("DEBUG","keyset = " + result.keySet().toString())
                //stimeVelocita = Gson().fromJson(result, Array<JsonObject>::class.java)
                //if(LOG_ENABLED)Log.d("DEBUG","Stime velocita: $stimeVelocita")
            }
        }

        //Set Anchor state local
        val setAnchorLocalUiState: SetAnchorLocalUiState = localViewModel.setAnchorUiState

        when (setAnchorLocalUiState) {
            is SetAnchorLocalUiState.Error -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Error set local anchor"
            )

            is SetAnchorLocalUiState.Loading -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Loading set local anchor"
            )

            is SetAnchorLocalUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                if (LOG_ENABLED) Log.d("DEBUG", "Success: Local connection set anchor")
                val result =
                    (localViewModel.setAnchorUiState as SetAnchorLocalUiState.Success).result
                if (LOG_ENABLED) Log.d("DEBUG", "Result set anchor local: $result")
            }
        }

        //Get Anchor Local
        val getAnchorLocalUiState: GetAnchorLocalUiState = localViewModel.getAnchorUiState

        when (getAnchorLocalUiState) {
            is GetAnchorLocalUiState.Error -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Error get local anchor"
            )

            is GetAnchorLocalUiState.Loading -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Loading get local anchor"
            )

            is GetAnchorLocalUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                if (LOG_ENABLED) Log.d("DEBUG", "Success: Local connection get anchor")
                anchorLocal =
                    (localViewModel.getAnchorUiState as GetAnchorLocalUiState.Success).anchor
                if (LOG_ENABLED) Log.d("DEBUG", "Get Ancora locale: $anchorLocal")
            }
        }
        //set anchor marker local
        if (anchorLocal.anchored.isNotEmpty() && anchorLocal.anchored != "-1") {

            if (LOG_ENABLED) Log.d("DEBUG", "set local anchor marker")
            if (anchorLocal.anchored == "1") {
                anchorVisibility = true
                updateAnchor(true)
                colorAnchor = red
            } else if (anchorLocal.anchored == "0") {
                anchorVisibility = false
                updateAnchor(false)
                colorAnchor = orange
            }
            //if(LOG_ENABLED)Log.d("DEBUG",ship_position.toString())
        }

        nmeaDataLocal = localViewModel.data.collectAsState()
        //Nmea data local
        if (nmeaDataLocal?.value?.get("latitude")
                ?.isNullOrEmpty() == false && nmeaDataLocal?.value?.get("longitude")
                ?.isNullOrEmpty() == false
        ) {

            if (LOG_ENABLED) Log.d("DEBUG", "Ship & NMEAdata local")
            if (nmeaDataLocal?.value?.get("latitude") != "0.0" || !nmeaDataLocal?.value.isNullOrEmpty()) {
                if (nmeaDataLocal?.value?.get("latitude") != null && nmeaDataLocal?.value?.get("longitude") != null) {
                    if (LOG_ENABLED) Log.d(
                        "DEBUG",
                        "Ship local: " + nmeaDataLocal?.value?.get("latitude") + " " + nmeaDataLocal?.value?.get(
                            "longitude"
                        )
                    )
                    shipPosition = LatLng(
                        nmeaDataLocal?.value?.get("latitude")!!.toDouble(),
                        nmeaDataLocal?.value?.get("longitude")!!.toDouble()
                    )
                }
                if (nmeaDataLocal?.value?.get("shipDirection") != null) {
                    shipDirection = nmeaDataLocal?.value?.get("shipDirection")!!.toDouble()
                }
                if (nmeaDataLocal?.value?.get("windDirection") != null) {
                    windDirection = nmeaDataLocal?.value?.get("windDirection")!!.toDouble()
                }
                if (nmeaDataLocal?.value?.get("windSpeed") != null) {
                    windSpeed = nmeaDataLocal?.value?.get("windSpeed")!!.toDouble()
                }
                if (nmeaDataLocal?.value?.get("courseOverGround") != null) {
                    courseOverGround = nmeaDataLocal?.value?.get("courseOverGround")!!.toDouble()

                }
                if (nmeaDataLocal?.value?.get("shipSpeed") != null) {
                    speedOverGround = nmeaDataLocal?.value?.get("shipSpeed")!!.toDouble()

                }
                if (LOG_ENABLED) Log.d(
                    "DEBUG",
                    "Anchor local: " + anchorLocal.latitude + " " + anchorLocal.longitude + " Ship local: " + shipPosition.toString()
                )
                if (LOG_ENABLED) Log.d(
                    "DEBUG",
                    "Anchor is distanced " + checkAnchorDistance(
                        LatLng(
                            anchorLocal.latitude.toDouble(), anchorLocal.longitude.toDouble()
                        ), shipPosition
                    )
                )
                if (checkAnchorDistance(
                        LatLng(
                            anchorLocal.latitude.toDouble(), anchorLocal.longitude.toDouble()
                        ), shipPosition
                    ) && anchorLocal.anchored == "1"
                ) {
                    val context = LocalContext.current
                    val mp: MediaPlayer = MediaPlayer.create(context, R.raw.notification)
                    mp.setOnCompletionListener {
                        it.release()
                    }
                    mp.start()
                }
                anchorDistanceMeters = getDistanceBetweenPointsMeters(
                    anchorLocal.latitude,
                    anchorLocal.longitude,
                    shipPosition.latitude.toString(),
                    shipPosition.longitude.toString(),
                    "kilometers"
                )
            }
            //if(LOG_ENABLED)Log.d("DEBUG",ship_position.toString())
        }

        /*LaunchedEffect(key1 = nmeaDataLocal?.value) {
            if(LOG_ENABLED)Log.d("DEBUG","LaunchedEffect on nmeaDataLocal")
            updateShipPosition(shipPosition, shipDirection)
        }*/
    } else if (connectionState == ConnectionState.Remote) {
        val remoteViewModel = InstantiateViewModel.instantiateRemoteViewModel()

        //Stime velocita remote
        val getstimeRemoteUiState: GetStimeRemoteUiState = remoteViewModel.getStimeRemoteUiState

        when (getstimeRemoteUiState) {
            is GetStimeRemoteUiState.Error -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Error stime velocita remote"
            )

            is GetStimeRemoteUiState.Loading -> if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Loading stime velocita remote"
            )

            is GetStimeRemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                if (LOG_ENABLED) Log.d("DEBUG", "Success: Stime velocita remote")
                stimeVelocita = Gson().fromJson(
                    (remoteViewModel.getStimeRemoteUiState as GetStimeRemoteUiState.Success).stime,
                    JsonObject::class.java
                )
                if (LOG_ENABLED) Log.d("DEBUG", "Success: Stime velocita remote $stimeVelocita")
            }
        }

        //Anchor Remote
        val anchorRemoteUiState: GetAnchorRemoteUiState = remoteViewModel.getAnchorRemoteUiState

        when (anchorRemoteUiState) {
            is GetAnchorRemoteUiState.Error -> {
                if (LOG_ENABLED) Log.d("DEBUG", "Error remote get anchor")
            }

            is GetAnchorRemoteUiState.Loading -> {
                if (LOG_ENABLED) Log.d("DEBUG", "Loading remote get anchor")
            }

            is GetAnchorRemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                if (LOG_ENABLED) Log.d("DEBUG", "Success: Remote connection get anchor")
                anchorRemote =
                    (remoteViewModel.getAnchorRemoteUiState as GetAnchorRemoteUiState.Success).anchor
                if (LOG_ENABLED) Log.d("DEBUG", "Anchor remote: $anchorRemote")
                val list = anchorRemote.split(" ")
                if (LOG_ENABLED) Log.d("DEBUG", list.toString())
                if (LOG_ENABLED) Log.d("DEBUG", "." + list[2] + ".")
                anchorRemoteObj = Anchor(
                    latitude = String.format(Locale.ENGLISH, "%.7f", list[0].toDouble()),
                    longitude = String.format(Locale.ENGLISH, "%.7f", list[1].toDouble()),
                    anchored = list[2],
                    time = list[3]
                )
                if (LOG_ENABLED) Log.d("DEBUG", "Anchor remoteObj: $anchorRemoteObj")
            }
        }
        //set anchor marker remote
        if (LOG_ENABLED) Log.d("DEBUG", "set remote anchor marker")
        if (anchorRemoteObj.anchored.isNotEmpty()) {
            if (anchorRemoteObj.anchored != "-1") {
                if (anchorRemoteObj.anchored == "1") {
                    anchorVisibility = true
                    updateAnchor(true)
                    colorAnchor = red
                } else if (anchorRemoteObj.anchored == "0") {
                    anchorVisibility = false
                    updateAnchor(false)
                    colorAnchor = orange
                }
            }
        }
        //NMEAData remote
        if (!nmeaDataRemote["latitude"].isNullOrEmpty() && !nmeaDataRemote["longitude"].isNullOrEmpty() && nmeaDataRemote["longitude"] != "0.0") {
            if (LOG_ENABLED) Log.d("DEBUG", "Ship position & NMEAdata remote")
            shipPosition = LatLng(
                nmeaDataRemote["latitude"]!!.toDouble(), nmeaDataRemote["longitude"]!!.toDouble()
            )
            shipDirection = nmeaDataRemote["shipDirection"]!!.toDouble()

            windDirection = nmeaDataRemote["windDirection"]!!.toDouble()
            windSpeed = nmeaDataRemote["windSpeed"]!!.toDouble()
            courseOverGround = nmeaDataRemote["courseOverGround"]!!.toDouble()
            speedOverGround = nmeaDataRemote["speedOverGround"]!!.toDouble()

            if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Anchor remote: " + anchorRemoteObj.latitude + " " + anchorRemoteObj.longitude + " Ship remote: " + shipPosition.toString()
            )
            if (LOG_ENABLED) Log.d(
                "DEBUG",
                "Anchor is distanced " + checkAnchorDistance(
                    LatLng(
                        anchorRemoteObj.latitude.toDouble(), anchorRemoteObj.longitude.toDouble()
                    ), shipPosition
                )
            )
            if (checkAnchorDistance(
                    LatLng(
                        anchorRemoteObj.latitude.toDouble(), anchorRemoteObj.longitude.toDouble()
                    ), shipPosition
                ) && anchorRemoteObj.anchored == "1"
            ) {
                val context = LocalContext.current
                val mp: MediaPlayer = MediaPlayer.create(context, R.raw.notification)
                mp.setOnCompletionListener {
                    it.release()
                }
                mp.start()
            }
            anchorDistanceMeters = getDistanceBetweenPointsMeters(
                anchorRemoteObj.latitude,
                anchorRemoteObj.longitude,
                shipPosition.latitude.toString(),
                shipPosition.longitude.toString(),
                "kilometers"
            )
        }


    }

    /* //nmeaData local
     val nmeaDataLocal = localViewModel.data.collectAsState()
     var nmeaDataRemote = HashMap<String, String>()

     if (!isConnectionLocal()) {
         //NmeaData remote
         val remoteUiState: RemoteUiState = remoteViewModel.remoteUiState
         connectionState = ConnectionState.Remote
         when (remoteUiState) {
             is RemoteUiState.Error -> if(LOG_ENABLED)Log.d("DEBUG","Error remote connection")
             is RemoteUiState.Loading -> if(LOG_ENABLED)Log.d("DEBUG","Loading remote connection")
             is RemoteUiState.Success -> {
                 //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                 if(LOG_ENABLED)Log.d("DEBUG","Success: Remote connection")
                 nmeaDataRemote =
                     readNMEA((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
             }
         }
     }*/


    trueWindAngle = getTWA(windDirection, shipDirection)

    val route = routeCalculator(
        destinationDirection, windDirection, windSpeed, trueWindAngle, stimeVelocita
    )

    var maxAngle by remember { mutableIntStateOf(0) }
    var optimalSail by remember { mutableStateOf("") }
    var vMax by remember { mutableStateOf("") }
    var vMaxEff by remember { mutableStateOf("") }
    var errorMessage = ""
    var errorFlag by remember { mutableStateOf(false) }

    if (route[1] == "inProgress") {
        errorFlag = true
        errorMessage = "Dati in elaborazione. Impossibile calcolare la rotta."
    }

    if (route[0] == "-1" && route[1] == "" && route[2] == "-1.0" && route[3] == "-1.0") {
        errorFlag = true
        errorMessage = "Rotta ottimale non disponibile."
    }

    if (route[0] != "-1" && route[1] != "" && route[2] != "-1.0" && route[3] != "-1.0") {
        errorFlag = false
        maxAngle = route[0].toInt()
        optimalSail = route[1]
        vMax = String.format(Locale.ENGLISH, "%.2f", route[2].toDouble())
        vMaxEff = String.format(Locale.ENGLISH, "%.2f", route[3].toDouble())

    }
    route.forEach {
        if (LOG_ENABLED) Log.d("DEBUG", "Route: $it")
    }

    //Maps variables

    val context = LocalContext.current
    // Configure OSMDroid
    Configuration.getInstance()
        .load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    val tileSource: OnlineTileSourceBase?

    val localTileSource = XYTileSource(
        "LocalServer",
        1,
        8,
        256,
        ".png",
        arrayOf("http://$raspberryIp:8081/data/OAM-World-1-8-J80/"),
        "© OpenStreetMap contributors"
    )

    var maxZoom = 13.0

    if (connectionState == ConnectionState.Local) {
        if (LOG_ENABLED) Log.d("DEBUG", "Local tiles")
        maxZoom = 8.0
        tileSource = localTileSource
    } else {
        if (LOG_ENABLED) Log.d("DEBUG", "Remote tiles")
        tileSource = TileSourceFactory.MAPNIK
    }
    Box(
        modifier = Modifier.fillMaxSize()
        //autoCentering = AutoCenteringParams(itemIndex = 0),
        //state = listState
    ) {
        Dialog(showDialog = showDialog, onDismissRequest = { showDialog = false }) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Text(
                        text = "Rotta: " + String.format(
                            Locale.ENGLISH,
                            "%.2f$DEGREE_SYMBOL",
                            destinationDirection
                        )
                    )
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item { Text(text = "TWS: $windSpeed $KNOT_SYMBOL") }
                item {
                    Text(
                        text = "TWA: " + String.format(
                            Locale.ENGLISH, "%.2f$DEGREE_SYMBOL", trueWindAngle
                        )
                    )
                }
                item { Text(text = "COG: $courseOverGround$DEGREE_SYMBOL") }
                item { Text(text = "SOG: $speedOverGround $KNOT_SYMBOL") }
                item { Text(text = "Wind direction: $windDirection$DEGREE_SYMBOL") }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                if (errorFlag) {
                    item { Text(text = errorMessage) }
                } else {
                    item { Text(text = "Sugg. Route: $maxAngle$DEGREE_SYMBOL") }
                    item { Text(text = "Sugg. sail conf.: $optimalSail") }
                    item { Text(text = "Est. Speed: $vMax $KNOT_SYMBOL") }
                    item { Text(text = "Est. VMG: $vMaxEff $KNOT_SYMBOL") }
                    item { Spacer(modifier = Modifier.height(10.dp)) }
                }
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
        val state = rememberSwipeToDismissBoxState()
        // AndroidView to embed the MapView
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .edgeSwipeToDismiss(state, 20.dp),
            factory = { ctx ->
                MapView(ctx).apply {

                    mapView = this
                    //this.maxZoomLevel = maxZoom

                    setTileSource(tileSource)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)
                    controller.setZoom(maxZoomLevel)
                    controller.setCenter(
                        GeoPoint(
                            shipPosition.latitude, shipPosition.longitude
                        )
                    )
                    if (LOG_ENABLED) Log.d("DEBUG", "create destination marker")
                    if (destination == null) {
                        destination = Marker(this)
                    }
                    destination?.icon = ResourcesCompat.getDrawable(
                        resources, R.drawable.ic_action_destination_icon, null
                    )
                    destination?.position = GeoPoint(0.0, 0.0)
                    destination?.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )
                    destination?.setVisible(false)
                    this.overlays.add(destination)
                    this.invalidate()
                    //Ship marker
                    if (ship == null) {
                        ship = Marker(this)
                    }
                    ship?.position = GeoPoint(shipPosition.latitude, shipPosition.longitude)
                    //ship?.title = "title"
                    //ship?.snippet = "description"
                    val icon = ResourcesCompat.getDrawable(
                        resources, R.drawable.ic_action_ship_marker, null
                    )
                    //icon?.mutate()?.setTint(0xFFFFFFFF.toInt())
                    ship?.icon = icon
                    ship?.rotation = shipDirection.toFloat()
                    ship?.alpha = 1f
                    ship?.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_CENTER
                    )
                    ship?.setInfoWindow(null)
                    this.overlays.add(ship)
                    this.invalidate()
                    //Anchor marker
                    if (anchor == null) {
                        anchor = Marker(this)
                    }
                    anchor?.position = GeoPoint(shipPosition.latitude, shipPosition.longitude)
                    //anchor?.title = "title"
                    //anchor?.snippet = "description"
                    //icon?.mutate()?.setTint(0xFFFFFFFF.toInt())
                    anchor?.icon = ResourcesCompat.getDrawable(
                        resources, R.drawable.ic_action_anchor_icon, null
                    )
                    //anchor?.rotation = 90.0f
                    //anchor?.alpha = 1f
                    anchor!!.setVisible(anchorVisibility)
                    anchor?.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_CENTER
                    )
                    anchor?.setInfoWindow(null)
                    this.overlays.add(anchor)
                    this.invalidate()
                    //Long press  destination marker
                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            if (p != null) {
                                if (LOG_ENABLED) Log.d("DEBUG", "Long press")
                                if (connectionState == ConnectionState.Local) {
                                    if (LOG_ENABLED) Log.d("DEBUG", "Add destination")
                                    updateDestinationPosition(LatLng(p.latitude, p.longitude))

                                    directionButtonVisibility = true
                                    destinationPosition = LatLng(p.latitude, p.longitude)
                                    destinationDirection = angleBetweenPoints(
                                        destinationPosition.latitude.toString(),
                                        destinationPosition.longitude.toString(),
                                        shipPosition.latitude.toString(),
                                        shipPosition.longitude.toString()
                                    )
                                    if (LOG_ENABLED) Log.d(
                                        "DEBUG",
                                        "Angolo destinazione: $destinationDirection"
                                    )
                                    return true
                                }
                            }
                            return false
                        }

                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            return false
                        }
                    })
                    overlays.add(eventsOverlay)

                    /*setOnTouchListener { _, event ->
                        when (event.action) {
                            MotionEvent.CLASSIFICATION_TWO_FINGER_SWIPE -> {
                                // Handle touch events
                                if(LOG_ENABLED)Log.d("DEBUG","TouchListener")
                                this.onTouchEvent(event)
                                true
                            }

                            else -> false
                        }
                    }*/


                }
            }
        )

        LaunchedEffect(shipPosition) {
            updateShipPosition(shipPosition, shipDirection)
        }
        /*LaunchedEffect(shipDirection) {
            updateShipPosition(shipPosition, shipDirection)
        }*/

        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (leftButton, topButton, topButton2, rightButton, bottomButton) = createRefs()

            Button(//Back Button
                onClick = {
                    navController.navigate("homepage")
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(orange), // Background color
                    contentColor = Color.Black
                ), modifier = Modifier
                    .size(30.dp)
                    .constrainAs(topButton) {
                        top.linkTo(parent.top, margin = 5.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_action_back),
                    contentDescription = "Back",
                    modifier = Modifier.size(15.dp)
                )
            }
            /*if (connectionState == ConnectionState.Local && directionButtonVisibility) {
                Button(//Rotta Button
                    onClick = {
                        showDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(orange), // Background color
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .height(20.dp)
                        .width(40.dp)
                        .constrainAs(topButton2) {
                            top.linkTo(topButton.bottom, margin = 5.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_direction_icon),
                        contentDescription = "Route",
                        modifier = Modifier.size(15.dp)
                    )
                }
            }*/
            Button(//Anchor Button
                onClick = {
                    anchorVisibility = !anchorVisibility
                    updateAnchor(anchorVisibility)
                    if (colorAnchor == orange) colorAnchor = red
                    else colorAnchor = orange
                    if (connectionState == ConnectionState.Local) {
                        if (anchorLocal.latitude != "0.0") {
                            if (LOG_ENABLED) Log.d("DEBUG", "send local anchor: $anchorLocal")
                            if (LOG_ENABLED) Log.d(
                                "DEBUG",
                                "send anchored: ${if (anchorLocal.anchored == "1") "0" else "1"}"
                            )
                            val localViewModel = InstantiateViewModel.instantiateLocalViewModel()
                            localViewModel.setAnchor(
                                shipPosition.latitude.toString(),
                                shipPosition.longitude.toString(),
                                if (anchorLocal.anchored == "1") "0" else "1"
                            )
                            if (anchorLocal.anchored == "1") anchorLocal.anchored =
                                "0" else anchorLocal.anchored = "1"
                        }
                    } else if (connectionState == ConnectionState.Remote) {
                        if (anchorRemoteObj.latitude != "0.0") {
                            if (anchorRemoteObj.anchored == "1") anchorRemoteObj.anchored =
                                "0" else anchorRemoteObj.anchored = "1"
                            anchorRemoteObj.latitude = shipPosition.latitude.toString()
                            anchorRemoteObj.longitude = shipPosition.longitude.toString()
                            val body = Gson().toJson(anchorRemoteObj)
                            if (LOG_ENABLED) Log.d("DEBUG", "send remote anchor $body")
                            val remoteViewModel = InstantiateViewModel.instantiateRemoteViewModel()
                            if (LOG_ENABLED) Log.d(
                                "DEBUG",
                                "result: " + (remoteViewModel.setAnchor(body))
                            )
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(colorAnchor), // Background color
                    contentColor = Color.Black
                ), modifier = Modifier
                    //.absoluteOffset { IntOffset(5, 160) }
                    .size(30.dp)
                    .constrainAs(leftButton) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 5.dp)
                    }) {
                //Text("On")
                Icon(
                    painter = painterResource(id = R.drawable.ic_action_anchor_icon),
                    contentDescription = "Anchor",
                    modifier = Modifier.size(15.dp)
                )
            }
            if (connectionState == ConnectionState.Local && directionButtonVisibility) {
                Button(//Rotta Button
                    onClick = {
                        showDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(orange), // Background color
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .size(30.dp)
                        //.alpha(if (connectionState == ConnectionState.Local && directionButtonVisibility) 1f else 0f)
                        .constrainAs(bottomButton) {
                            bottom.linkTo(parent.bottom, margin = 5.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }) {
                    //Text("On")
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_direction_icon),
                        contentDescription = "Anchor",
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Button(
                //Reset position
                onClick = {
                    mapView?.controller?.setCenter(
                        GeoPoint(
                            shipPosition.latitude,
                            shipPosition.longitude
                        )
                    )
                    mapView?.controller?.setZoom(10.0)
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(orange), // Background color
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .size(30.dp)
                    .constrainAs(rightButton) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 5.dp)
                    },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_action_reset_position_icon),
                    contentDescription = "reset position",
                    modifier = Modifier.size(15.dp)
                )
            }

        }
    }
}

fun updateShipPosition(shipPosition: LatLng, shipDirection: Double) {
    if (LOG_ENABLED) Log.d("DEBUG", "Update ship position: $shipPosition")
    ship?.position = GeoPoint(shipPosition.latitude, shipPosition.longitude)
    ship?.rotation = 360 - shipDirection.toFloat()
    anchor?.position = GeoPoint(shipPosition.latitude, shipPosition.longitude)
    mapView?.invalidate()

    if (cameraUpdate <= 1) {
        mapView?.controller?.setCenter(
            GeoPoint(
                shipPosition.latitude,
                shipPosition.longitude
            )
        )
        mapView?.controller?.setZoom(10.0)
        cameraUpdate++
    }
}

fun updateDestinationPosition(destinationPosition: LatLng) {
    if (LOG_ENABLED) Log.d("DEBUG", "Update destination position: $destinationPosition")
    destination?.position = GeoPoint(destinationPosition.latitude, destinationPosition.longitude)
    destination?.setVisible(true)
    mapView?.invalidate()
}

fun updateAnchor(visibility: Boolean) {
    anchor?.setVisible(visibility)
    mapView?.invalidate()
}
/*
@Composable
fun DirectionalPad(
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .background(Color.Transparent)
            //.border(1.dp, Color.Black)
            .height(70.dp)
            .width(70.dp)
    ) {
        val (leftButton, topButton, rightButton, bottomButton) = createRefs()
        val bSize = 25.dp
        val iSize = 10.dp
        Button(//Direction up Button
            onClick = onUpClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(orange), // Background color
                contentColor = Color.Black
            ), modifier = Modifier
                .size(bSize)
                //.padding(10.dp)
                .constrainAs(topButton) {
                    top.linkTo(parent.top, margin = 5.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_action_up),
                contentDescription = "up",
                modifier = Modifier.size(iSize)
            )
        }
        Button(//Direction Left Button
            onClick = onLeftClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(orange), // Background color
                contentColor = Color.Black
            ), modifier = Modifier
                //.padding(10.dp)
                .size(bSize)
                .constrainAs(leftButton) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 5.dp)
                }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_action_left),
                contentDescription = "left",
                modifier = Modifier.size(iSize)
            )
        }
        Button(//Navigation down Button
            onClick = onDownClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(orange), // Background color
                contentColor = Color.Black
            ),
            modifier = Modifier
                //.padding(10.dp)
                .size(bSize)
                .constrainAs(bottomButton) {
                    bottom.linkTo(parent.bottom, margin = 5.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_action_down),
                contentDescription = "down",
                modifier = Modifier.size(iSize)
            )
        }
        Button(
            //Navigation right Button
            onClick = onRightClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(orange), // Background color
                contentColor = Color.Black
            ),
            modifier = Modifier
                //.padding(10.dp)
                .size(bSize)
                .constrainAs(rightButton) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 5.dp)
                },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_action_right),
                contentDescription = "right",
                modifier = Modifier.size(iSize)
            )
        }
    }
    *//*Box() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(Color.Transparent)
                .padding(0.dp)
        ) {
            Button(
                onClick = onUpClick,
                modifier = Modifier
                    .padding(0.dp)
                    .size(15.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(orange), // Background color
                    contentColor = Color.Black
                ),
            ) {
                //Text("Up")
            }
            Row {
                Button(
                    onClick = onLeftClick,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(orange), // Background color
                        contentColor = Color.Black
                    ),
                ) {
                    //Text("Left")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRightClick,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(orange), // Background color
                        contentColor = Color.Black
                    ),
                ) {
                    //Text("Right")
                }
            }
            Button(
                onClick = onDownClick,
                modifier = Modifier
                    .padding(4.dp)
                    .size(15.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(orange), // Background color
                    contentColor = Color.Black
                ),
            ) {
                //Text("Down")
            }
        }
    }*//*
}*/
/*

fun panMap(latDelta: Double, lonDelta: Double) {
    val currentCenter = mapView?.mapCenter as GeoPoint
    val newCenter = GeoPoint(currentCenter.latitude + latDelta, currentCenter.longitude + lonDelta)
    mapView?.controller?.setCenter(newCenter)
}*/
