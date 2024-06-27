package com.example.sailboatapp.presentation.ui.screen

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
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
import com.example.sailboatapp.presentation.ui.KNOT_SYMBOL
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
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
    windAngles.forEachIndexed(action = { i, element ->
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

    windSpeeds.forEachIndexed(action = { i, element ->
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
    return mediaPonderata;
}

fun routeCalculator(
    destinationDirection: Double,
    windDirection: Double,
    windSpeed: Double,
    trueWindAngle: Double,
    stimeVelocita: JsonObject
): Array<String> {
    var vMaxEff = -1.0
    var optimalSail = ""
    var vMax = -1.0
    var velocitas: ArrayList<Double> = arrayListOf()
    var velocitasEff: ArrayList<Double> = arrayListOf()
    var maxAngle = -1

    stimeVelocita.keySet().forEach {
        var vela = it
        var windAngles = stimeVelocita.getAsJsonObject(it).getAsJsonArray("angoliVento")
        var windSpeeds = stimeVelocita.getAsJsonObject(it).getAsJsonArray("velocitaVento")
        var stime = stimeVelocita.getAsJsonObject(it).getAsJsonArray("stimeVelocitaBarca")

        println("Vela: $vela")
        println("WindAngles: $windAngles")
        println("WindSpeeds: $windSpeeds")
        println("Stime: $stime")

        var angoliIndex = trovaAngoli(windAngles, destinationDirection)

        var velocitaIndex = trovaVelocita(windSpeeds, windSpeed)

        val vel = mediaPonderata(
            angoliIndex[0],
            destinationDirection,
            stime,
            angoliIndex[1],
            velocitaIndex[0],
            velocitaIndex[1],
            windSpeed
        )
        velocitas.add(vel)

        if (vel < 0) {
            return arrayOf("vel < 0")
        }

        val distanza1 = abs(angoliIndex[0].asList()[0] - destinationDirection)
        val distanza2 = abs(angoliIndex[0].asList()[1] - destinationDirection)
        val peso1 = 1 - distanza1 / (distanza1 + distanza2)
        val peso2 = 1 - peso1
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
    //println("relativeWindDirection: $relativeWindDirection")
    //Normalize the angle
    if (relativeWindDirection < 0) {
        relativeWindDirection += 360.0
    } else if (relativeWindDirection > 360) {
        relativeWindDirection -= 360.0
    }
    //println("twa pre adjust: $relativeWindDirection")
    //Adjuste the range
    if (relativeWindDirection > 180) {
        relativeWindDirection = 360.0 - relativeWindDirection
    }
    return relativeWindDirection;
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
    //println("2input data= "+latitude1+" "+longitude1+ " "+latitude2+" "+longitude2)
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
    //println("1input data= "+latitude1+" "+longitude1+ " "+latitude2+" "+longitude2)
    return (getDistanceBetweenPoints(
        latitude1, longitude1, latitude2, longitude2, unit = "kilometers"
    ) * 1000).toInt()
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
    println("Anchor distance: $distance")
    if (distance > anchorDistanceLimitMeters) {
        return true
    }
    return false
}
@Composable
fun Map(
    navController: NavHostController, isSwippeEnabled: Boolean, onSwipeChange: (Boolean) -> Unit
) {

    onSwipeChange(false)
    //val ok = getDistanceBetweenPointsMeters("45.4641943", "9.1896346", "40.8358846", "14.2487679")
    //println("Napoli milano: " + ok)
    var shipPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var shipDirection by remember { mutableDoubleStateOf(0.0) }
    var destinationPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var destinationDirection by remember { mutableDoubleStateOf(0.0) }
    val destinationLine = ArrayList<LatLng>()
    destinationLine.add(shipPosition)
    destinationLine.add(destinationPosition)

    var windDirection by remember { mutableDoubleStateOf(0.0) }
    var windSpeed by remember { mutableDoubleStateOf(0.0) }
    var speedOverGround by remember { mutableDoubleStateOf(0.0) }
    var courseOverGround by remember { mutableDoubleStateOf(0.0) }
    var trueWindAngle by remember { mutableDoubleStateOf(0.0) }

    var stimeVelocita: JsonObject = JsonObject()

    var anchorVisibility by remember { mutableStateOf(false) }
    var destinationPositionVisibility by remember { mutableStateOf(false) }
    var destinationLineVisibility by remember { mutableStateOf(false) }
    var directionButtonVisibility by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    var colorAnchor by remember { mutableLongStateOf(orange) }

    var connectionState: ConnectionState by remember { mutableStateOf(ConnectionState.Loading) }

    val localViewModel: LocalViewModel = viewModel()
    val remoteViewModel: RemoteViewModel = viewModel()

    //Stime velocita local
    val stimeVelocitaUiState: StimeVelocitaUiState = localViewModel.stimeVelocitaUiState

    when (stimeVelocitaUiState) {
        is StimeVelocitaUiState.Error -> println("Error stime velocita local")
        is StimeVelocitaUiState.Loading -> println("Loading stime velocita local")
        is StimeVelocitaUiState.Success -> {

            var result =
                (localViewModel.stimeVelocitaUiState as StimeVelocitaUiState.Success).stimeVelocita
            println("Success: Stime velocita local $result")

            stimeVelocita = result

            /*result.keySet().forEach{
            }*/
            //println("keyset = " + result.keySet().toString())
            //stimeVelocita = Gson().fromJson(result, Array<JsonObject>::class.java)
            //println("Stime velocita: $stimeVelocita")
        }
    }

    if (!checkLocalConnection()) {
        //Stime velocita remote
        val getstimeRemoteUiState: GetStimeRemoteUiState = remoteViewModel.getStimeRemoteUiState
        connectionState = ConnectionState.Remote
        when (getstimeRemoteUiState) {
            is GetStimeRemoteUiState.Error -> println("Error stime velocita remote")
            is GetStimeRemoteUiState.Loading -> println("Loading stime velocita remote")
            is GetStimeRemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                println("Success: Stime velocita remote")
                stimeVelocita = Gson().fromJson(
                    (remoteViewModel.getStimeRemoteUiState as GetStimeRemoteUiState.Success).stime,
                    JsonObject::class.java
                )
                println("Success: Stime velocita remote $stimeVelocita")
            }
        }
    }

    var anchorLocal: Anchor = Anchor("0.0", "0.0", "-1", "")
    var anchorRemote = ""
    var anchorRemoteObj = Anchor("0.0", "0.0", "-1", "")
    var anchorDistanceMeters by remember { mutableIntStateOf(0) }

    //Set Anchor state
    val setAnchorLocalUiState: SetAnchorLocalUiState = localViewModel.setAnchorUiState

    when (setAnchorLocalUiState) {
        is SetAnchorLocalUiState.Error -> println("Error set local anchor")
        is SetAnchorLocalUiState.Loading -> println("Loading set local anchor")
        is SetAnchorLocalUiState.Success -> {
            //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
            println("Success: Local connection set anchor")
            val result = (localViewModel.setAnchorUiState as SetAnchorLocalUiState.Success).result
            println("Result set anchor: $result")
        }
    }

    //Anchor Local
    val getAnchorLocalUiState: GetAnchorLocalUiState = localViewModel.getAnchorUiState

    when (getAnchorLocalUiState) {
        is GetAnchorLocalUiState.Error -> println("Error get local anchor")
        is GetAnchorLocalUiState.Loading -> println("Loading get local anchor")
        is GetAnchorLocalUiState.Success -> {
            //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
            println("Success: Local connection get anchor")
            anchorLocal = (localViewModel.getAnchorUiState as GetAnchorLocalUiState.Success).anchor
            println("Ancora locale: $anchorLocal")
            connectionState = ConnectionState.Local
        }
    }

    if (!checkLocalConnection()) {
        //Anchor Remote
        val anchorRemoteUiState: GetAnchorRemoteUiState = remoteViewModel.getAnchorRemoteUiState
        connectionState = ConnectionState.Remote
        when (anchorRemoteUiState) {
            is GetAnchorRemoteUiState.Error -> {
                println("Error remote anchor")
            }
            is GetAnchorRemoteUiState.Loading -> {
                println("Loading remote anchor")
            }
            is GetAnchorRemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                println("Success: Remote connection anchor")
                anchorRemote =
                    (remoteViewModel.getAnchorRemoteUiState as GetAnchorRemoteUiState.Success).anchor
                println("Anchor remote: $anchorRemote")
                val list = anchorRemote.split(" ")
                println(list.toString())
                println("." + list[2] + ".")
                anchorRemoteObj = Anchor(
                    latitude = String.format("%.7f", list[0].toDouble()),
                    longitude = String.format("%.7f", list[1].toDouble()),
                    anchored = list[2],
                    time = list[3]
                )
                println("Anchor remoteObj: $anchorRemoteObj")
            }
        }
    }
    //set anchor marker
    if (anchorLocal.anchored.isEmpty() || anchorLocal.anchored == "-1") {
        println("set remote anchor marker")
        if (anchorRemoteObj.anchored.isEmpty()) {

        } else {
            if (anchorRemoteObj.anchored == "-1") {
                anchorRemoteObj
            } else {
                if (anchorRemoteObj.anchored == "1") {
                    anchorVisibility = true
                    colorAnchor = red
                } else if (anchorRemoteObj.anchored == "0") {
                    anchorVisibility = false
                    colorAnchor = orange
                }
            }
        }
    } else {
        println("set local anchor marker")
        if (anchorLocal.anchored == "1") {
            anchorVisibility = true
            colorAnchor = red
        } else if (anchorLocal.anchored == "0") {
            anchorVisibility = false
            colorAnchor = orange
        }
        //println(ship_position.toString())
    }
    //nmeaData local
    val nmeaData = localViewModel.data.collectAsState()
    var nmeaDataRemote = HashMap<String, String>()

    if (!checkLocalConnection()) {
        //NmeaData remote
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

    val cameraZoom = 5f
    println("reset camera init: $shipPosition")
    var cameraPositionState by remember {
        mutableStateOf(
            CameraPositionState(
                position = CameraPosition.fromLatLngZoom(shipPosition, cameraZoom)
            )
        )
    }
    var resetCameraCount by remember { mutableIntStateOf(0) }
    println("reset camera count: $resetCameraCount")

    //Nmea data
    if (nmeaData.value["latitude"].isNullOrEmpty() && nmeaData.value["longitude"].isNullOrEmpty()) {
        if (nmeaDataRemote["latitude"].isNullOrEmpty() || nmeaDataRemote["longitude"].isNullOrEmpty() || nmeaDataRemote["longitude"] == "0.0") {
            shipPosition
        } else {
            println("Ship position & NMEAdata remote")
            shipPosition = LatLng(
                nmeaDataRemote["latitude"]!!.toDouble(), nmeaDataRemote["longitude"]!!.toDouble()
            )
            shipDirection = nmeaDataRemote["shipDirection"]!!.toDouble()

            windDirection = nmeaDataRemote["windDirection"]!!.toDouble()
            windSpeed = nmeaDataRemote["windSpeed"]!!.toDouble()
            courseOverGround = nmeaDataRemote["courseOverGround"]!!.toDouble()
            speedOverGround = nmeaDataRemote["speedOverGround"]!!.toDouble()

            if (shipPosition.latitude != 0.0 && resetCameraCount <= 3) {
                println("reset camera remote: $shipPosition")
                /*cameraPositionState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(
                            shipPosition, cameraZoom
                        )
                    )
                )*/
                resetCameraCount++
                println("reset camera count: $resetCameraCount")
            }
            println("Ancora remota: " + anchorRemoteObj.latitude + " " + anchorRemoteObj.longitude + " Ship: " + shipPosition.toString())
            println(
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
    } else {
        println("Ship & NMEAdata local")
        if (nmeaData.value["latitude"]!! == "0.0" || nmeaData.value.isNullOrEmpty()) {
            shipPosition
        } else {
            shipPosition = LatLng(
                nmeaData.value["latitude"]!!.toDouble(), nmeaData.value["longitude"]!!.toDouble()
            )
            shipDirection = nmeaData.value["shipDirection"]!!.toDouble()

            windDirection = nmeaData.value["windDirection"]!!.toDouble()
            windSpeed = nmeaData.value["windSpeed"]!!.toDouble()
            courseOverGround = nmeaData.value["courseOverGround"]!!.toDouble()
            speedOverGround = nmeaData.value["shipSpeed"]!!.toDouble()

            if (shipPosition.latitude != 0.0 && resetCameraCount <= 3) {
                println("reset camera local: $shipPosition")
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(shipPosition, cameraZoom)
                }
                resetCameraCount++
                println("reset camera count: $resetCameraCount")
            }
            println("Ancora remota: " + anchorLocal.latitude + " " + anchorLocal.longitude + " Ship: " + shipPosition.toString())
            println(
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
        //println(ship_position.toString())
    }
    trueWindAngle = getTWA(windDirection, shipDirection)

    val route = routeCalculator(
        destinationDirection, windDirection, windSpeed, trueWindAngle, stimeVelocita
    )


    var lastMaxAngle by remember { mutableStateOf(0) }
    var lastOptimalSail by remember { mutableStateOf("") }
    var lastVMax by remember { mutableStateOf("") }
    var lastVMaxEff by remember { mutableStateOf("") }


    var maxAngle = if (route[0].toInt() != -1) {
        lastMaxAngle = route[0].toInt()
        lastMaxAngle
    } else lastMaxAngle

    var optimalSail = if (route[1] != "") {
        lastOptimalSail = route[1]
        lastOptimalSail
    } else lastOptimalSail
    var vMax = if (route[2].toDouble() != -1.0) {
        lastVMax = String.format("%.2f", route[2].toDouble())
        lastVMax
    } else lastVMax
    var vMaxEff = if (route[3].toDouble() != -1.0) {
        lastVMaxEff = String.format("%.2f", route[3].toDouble())
        lastVMaxEff
    } else lastVMaxEff

    route.forEach {
        println("Route: " + it)
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
                item { Text(text = "Rotta: " + String.format("%.2f", destinationDirection)) }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item { Text(text = "TWS: $windSpeed") }
                item { Text(text = "TWA: " + String.format("%.2f", trueWindAngle)) }
                item { Text(text = "COG: $courseOverGround") }
                item { Text(text = "SOG: $speedOverGround") }
                item { Text(text = "Wind direction: $windDirection") }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item { Text(text = "Sugg. Route: $maxAngle") }
                item { Text(text = "Sugg. sail conf.: $optimalSail") }
                item { Text(text = "Estimated Speed: ${vMax} $KNOT_SYMBOL") }
                item { Text(text = "Estimated VMG: $vMaxEff $KNOT_SYMBOL") }
                item { Spacer(modifier = Modifier.height(10.dp)) }
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
        GoogleMap(mergeDescendants = true,
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
                destinationPositionVisibility = true
                destinationLineVisibility = true
                directionButtonVisibility = true
                destinationPosition = it
                destinationDirection = angleBetweenPoints(
                    destinationPosition.latitude.toString(),
                    destinationPosition.longitude.toString(),
                    shipPosition.latitude.toString(),
                    shipPosition.longitude.toString()
                )
                println(
                    "Angolo destinazione: $destinationDirection"
                )
            }) {
            Marker(
                state = MarkerState(position = shipPosition),
                rotation = shipDirection.toFloat(),
                anchor = Offset(0.5f, 0.5f),
                alpha = 10F,
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_ship_marker),
                //title = "Nave",
                //snippet = "Descrizione\n Vento: 10"
            )
            Marker(
                state = MarkerState(position = shipPosition),
                rotation = 0f,
                anchor = Offset(0.5f, 0.5f),
                alpha = 10F,
                visible = anchorVisibility,
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_anchor),
                title = "Ancora",
                snippet = "$anchorDistanceMeters m",
                zIndex = 1F
            )
            Marker(
                state = MarkerState(position = destinationPosition),
                rotation = 0f,
                //anchor = Offset(0.5f, 0.5f),
                visible = destinationPositionVisibility,
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_destination_icon),
                title = "Destinazione",
                //snippet = "Descrizione",
                zIndex = 1F
            )
            Polyline(
                points = destinationLine,
                visible = destinationLineVisibility,
                width = 5F,
                color = Color.Red
            )
        }
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (leftButton, centerButton, rightButton, bottomButton) = createRefs()/*Text(
                modifier = Modifier.constrainAs(centerButton){
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                text = "Ancora"
            )*/
            Button(//Anchor Button
                onClick = {
                    anchorVisibility = !anchorVisibility
                    if (colorAnchor == orange) colorAnchor = red
                    else colorAnchor = orange
                    if (connectionState == ConnectionState.Local) {
                        if (anchorLocal.latitude != null && anchorLocal.latitude != "0.0") {
                            println("send local anchor: $anchorLocal")
                            localViewModel.setAnchor(
                                shipPosition.latitude.toString(),
                                shipPosition.longitude.toString(),
                                if (anchorLocal.anchored == "1") "0" else "1"
                            )
                            if (anchorLocal.anchored == "1") anchorLocal.anchored =
                                "0" else anchorLocal.anchored = "1"
                        }
                    } else {
                        if (anchorRemoteObj.latitude != null && anchorRemoteObj.latitude != "0.0") {
                            if (anchorRemoteObj.anchored == "1") anchorRemoteObj.anchored =
                                "0" else anchorRemoteObj.anchored = "1"
                            anchorRemoteObj.latitude = shipPosition.latitude.toString()
                            anchorRemoteObj.longitude = shipPosition.longitude.toString()
                            val body = Gson().toJson(anchorRemoteObj)
                            println("send remote anchor $body")
                            println("result: " + remoteViewModel.setAnchor(body))
                        }

                    }
                    //navController.navigate("off")
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
            Button(//Direction Button
                onClick = {
                    if (connectionState == ConnectionState.Local && directionButtonVisibility) {
                        showDialog = true
                    }
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(orange), // Background color
                    contentColor = Color.Black
                ), modifier = Modifier
                    .size(30.dp)
                    .alpha(if (connectionState == ConnectionState.Local && directionButtonVisibility) 1f else 0f)
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
            Button(
            //Reset position
                onClick = {
                    if (shipPosition.latitude != 0.0) {
                        println("reset camera button: $shipPosition")
                        cameraPositionState.move(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(
                                    shipPosition, cameraZoom
                                )
                            )
                        )
                    }
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
                    },
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