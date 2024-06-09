package com.example.sailboatapp.presentation.ui.screen

import android.icu.text.DecimalFormat
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.HorizontalPageIndicator
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.maps.android.ktx.utils.heatmaps.heatmapTileProviderWithData




@OptIn(UiToolingDataApi::class)
@Composable
fun Polars(navController: NavHostController) {


    var polarRecState by remember {
        mutableStateOf(false)
    }
    var polarString by remember {
        mutableStateOf("Inizia Registrazione")
    }

    val listState = rememberScalingLazyListState()
    var vignetteState by remember { mutableStateOf(VignettePosition.TopAndBottom) }

    val maxPages = 3
    val selectedPage by remember { mutableIntStateOf(0) }
    var finalValue by remember { mutableIntStateOf(0) }

    val animatedSelectedPage by animateFloatAsState(
        targetValue = selectedPage.toFloat(), label = "",
    ) {
        finalValue = it.toInt()
    }

    var pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = animatedSelectedPage - finalValue
            override val selectedPage: Int
                get() = finalValue
            override val pageCount: Int
                get() = maxPages
        }
    }

    var showVignette by remember {
        mutableStateOf(true)
    }

    var connectionState: ConnectionState by remember { mutableStateOf(ConnectionState.Loading) }


    var stimeVelocita = JsonObject()

    var showDialog by remember { mutableStateOf(false) }

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
                stimeVelocita =
                    Gson().fromJson(
                        (remoteViewModel.getStimeRemoteUiState as GetStimeRemoteUiState.Success).stime,
                        JsonObject::class.java
                    )
                println("Success: Stime velocita remote $stimeVelocita")
            }
        }

    }

    Scaffold(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(), positionIndicator = {
        PositionIndicator(
            scalingLazyListState = listState, modifier = Modifier
        )
    }, vignette = {
        if (showVignette) {
            Vignette(vignettePosition = vignetteState)
        }
    }, timeText = {
        TimeText()
    }, pageIndicator = {
        HorizontalPageIndicator(pageIndicatorState = pageIndicatorState)
    }) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "Polari"
                )
            }
            item { Spacer(modifier = Modifier.height(30.dp)) }
            item {


                    /*Canvas(
                        modifier = Modifier.height(200.dp).width(200.dp)

                    ) {
                        drawArc(
                            color = Color.Red,
                            startAngle = -90f,
                            sweepAngle = 180f,
                            useCenter = false,
                            style = Fill,


                        )
                        drawLine( color = Color.Blue, Offset(size.width / 2, size.width / 2), Offset(size.width , 0f), 2f)

                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val radius = size.minDimension / 2

                        val data = listOf(3f, 2f, 4f, 5f, 3f)

                        // Draw the radar chart outline
                        drawCircle(
                            color = Color.Yellow,
                            radius = radius * 10,
                            style = Stroke(width = 10f)
                        )

                        // Draw the radar chart lines
                        for (i in 0 until data.size) {
                            val angle = Math.PI * i / data.size
                            val startX = centerX + (radius * Math.cos(angle)).toFloat()
                            val startY = centerY + (radius * Math.sin(angle)).toFloat()
                            drawLine(
                                color = Color.Green,
                                start = Offset(centerX, centerY),
                                end = Offset(startX, startY),
                                strokeWidth = 2f
                            )
                        }

                        // Draw the data points
                        val path = Path()
                        for (i in 0 until data.size) {
                            val angle = Math.PI * i / data.size
                            val value = data[i]
                            val x = centerX + (radius * value * Math.cos(angle)).toFloat()
                            val y = centerY + (radius * value * Math.sin(angle)).toFloat()
                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                            drawCircle(
                                color = Color.Blue,
                                center = Offset(x, y),
                                radius = 8f
                            )
                        }
                        // Connect the last point to the first point to close the path
                        path.close()
                        // Draw the filled area
                        drawPath(
                            path = path,
                            color = Color.Blue.copy(alpha = 0.3f)
                        )
                    }
*/

                /*var data = PolarChartData(
                    windAngles = listOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f),
                    windSpeeds = listOf(0f, 6f, 8f, 6f, 3f, 6f, 8f, 6f)
                )

                AndroidView(
                    factory = { context ->
                        RadarChart(context).apply {
                            description.isEnabled = false
                            webLineWidth = 1f
                            webColor = ColorTemplate.PASTEL_COLORS.first()
                            webLineWidthInner = 1f
                            webColorInner = ColorTemplate.PASTEL_COLORS.first()
                            webAlpha = 100


                            val xAxis: XAxis = xAxis
                            xAxis
                            xAxis.textSize = 9f
                            xAxis.yOffset = 0f
                            xAxis.xOffset = 0f
                            xAxis.valueFormatter = object : ValueFormatter() {
                                private val labels = arrayOf("0°", "45°", "90°", "135°", "180°")
                                override fun getFormattedValue(value: Float): String {
                                    return labels[(value / 45).toInt() % labels.size]
                                }
                            }
                            xAxis.textColor = ColorTemplate.PASTEL_COLORS.last()

                            val yAxis: YAxis = yAxis
                            yAxis.setLabelCount(5, true)
                            yAxis.textSize = 9f
                            yAxis.axisMinimum = 0f
                            yAxis.axisMaximum = 10f
                            yAxis.setDrawLabels(false)

                            legend.isEnabled = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Adjust height as needed
                    ,
                    update = { chart ->
                       // val entries = data { RadarEntry(it) }

                        var windSpeedsJson = JsonArray()

                        println(stimeVelocita.getAsJsonObject("default"))
                        var default = stimeVelocita.getAsJsonObject("default")

                        val entries = ArrayList<RadarEntry>()
                        if(default != null) {
                            windSpeedsJson = default.getAsJsonArray("velocitaVento")
                        }

                        windSpeedsJson.forEachIndexed(
                            action = { index, element ->
                                entries.add(RadarEntry(element.asFloat))
                            }
                        )

                        val dataSet = RadarDataSet(entries, "Polar Chart").apply {
                            color = ColorTemplate.COLORFUL_COLORS.first()
                            fillColor = ColorTemplate.COLORFUL_COLORS.first()
                            setDrawFilled(true)
                            fillAlpha = 180
                            lineWidth = 2f
                        }
                        chart.data = RadarData(dataSet).apply {
                            setValueTextSize(18f)
                            setDrawValues(false)
                        }

                        chart.invalidate()
                    }
                )*/

                Column () {
                    val numRows = 9
                    val numCols = 8

                    var vela = ""
                    var windAnglesJson: JsonArray
                    var windSpeedsJson = JsonArray()
                    var stimeJson = JsonArray()

                    var windAngle = arrayListOf("-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1")
                    var windSpeed = arrayListOf("-1", "-1", "-1", "-1", "-1", "-1", "-1")
                    var stime = ArrayList<Any>()




                    stimeVelocita.keySet().forEach {
                        vela = it
                        windAnglesJson =
                            stimeVelocita.getAsJsonObject(it).getAsJsonArray("angoliVento")
                        windSpeedsJson = stimeVelocita.getAsJsonObject(it)
                            .getAsJsonArray("velocitaVento")
                        stimeJson = stimeVelocita.getAsJsonObject(it)
                            .getAsJsonArray("stimeVelocitaBarca")

                        println("Vela: $vela")
                        println("WindAngles: $windAnglesJson")
                        println("WindSpeeds: $windSpeedsJson")

                        //println(windSpeedsJson.get(0))
                        windSpeed.clear()
                        for (jsonElement in windSpeedsJson) {
                            when (jsonElement) {
                                is JsonElement -> {
                                    windSpeed.add(jsonElement.toString())
                                }
                            }
                        }

                        windAngle.clear()
                        for (jsonElement in windAnglesJson) {
                            when (jsonElement) {
                                is JsonElement -> {
                                    windAngle.add(jsonElement.toString())
                                }
                            }
                        }

                        stime.clear()
                        for (jsonArray in stimeJson) {
                            when (jsonArray) {
                                is JsonArray -> {
                                    stime.add(jsonArray.toString())
                                }
                            }
                        }
                        //println("Stime 0 ${stimeJson[0]}")

                        println("Stime json: $stimeJson")
                        stime.forEach { println("Stime: $it") }

                        ConstraintLayout(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .height(100.dp)
                                .width(200.dp)
                        ) {
                            // Create an array of refs for all cells
                            val cells = Array(numRows) { Array(numCols) { createRef() } }

                            for (row in 0 until numRows) {
                                for (col in 0 until numCols) {
                                    val isHeaderRow = row == 0
                                    val isHeaderColumn = col == 0

                                    println("Row & Col: $row $col")


                                    Text(
                                        text = if (isHeaderRow && isHeaderColumn) vela else if (isHeaderRow) "${windSpeed[col - 1]} kn" else if (isHeaderColumn) "${windAngle[row - 1]}°" else if (!stimeJson.isEmpty) String.format(
                                            "%.2f",
                                            stimeJson[row - 1].asJsonArray[col - 1].asDouble
                                        ) else "-1",
                                        //text = if (isHeaderRow && isHeaderColumn) "${stimeVelocita.keySet()}" else if (isHeaderRow) "Header ${col + 1}" else if (isHeaderColumn) "Header ${row + 1}" else "R${row + 1}C${col + 1}",

                                        fontSize = 10.sp,
                                        fontWeight = if (isHeaderRow || isHeaderColumn) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier
                                            .background(if (isHeaderRow || isHeaderColumn) Color.Gray else Color.LightGray)
                                            .padding(8.dp)
                                            .constrainAs(cells[row][col]) {
                                                // Set the constraints for each cell
                                                top.linkTo(
                                                    if (row == 0) parent.top else cells[row - 1][col].bottom,
                                                    margin = 8.dp
                                                )
                                                start.linkTo(
                                                    if (col == 0) parent.start else cells[row][col - 1].end,
                                                    margin = 8.dp
                                                )
                                                width = Dimension.wrapContent
                                                height = Dimension.wrapContent
                                            }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(200.dp))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(30.dp)) }
            item {
                Button(//registra button
                    onClick = {
                        if (polarRecState) {
                            polarRecState = false
                            polarString = "Inizia registrazione"
                        } else {
                            polarRecState = true
                            polarString = "Termina"
                            showDialog = true
                        }
                    }, modifier = Modifier
                        .height(30.dp)
                        .width(150.dp)
                ) {
                    Text(polarString)
                }
            }
            item {
                var textState by remember { mutableStateOf("") }/*BasicTextField(
                    modifier = Modifier.fillMaxSize(),
                    value = textState,
                    onValueChange = {textState = it},
                    textStyle = TextStyle.Default,
                    decorationBox = {
                        innerTextField ->
                        Row (
                            modifier = Modifier
                                .background(MaterialTheme.colors.secondary)
                                .padding(5.dp)
                        ) {
                            innerTextField()
                        }
                    }
                )*/
                Dialog(showDialog = showDialog, onDismissRequest = { showDialog = false }) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Configurazione vele: ")
                        Spacer(modifier = Modifier.height(10.dp))
                        BasicTextField(modifier = Modifier,//.absoluteOffset { IntOffset(50,160) },
                            value = textState,
                            onValueChange = { textState = it },
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
            }
        }
    }

}

class PolarChartData(windAngles: List<Float>, windSpeeds: List<Float>)
