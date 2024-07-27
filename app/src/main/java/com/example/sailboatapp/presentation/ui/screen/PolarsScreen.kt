package com.example.sailboatapp.presentation.ui.screen


import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
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
import com.example.sailboatapp.presentation.network.ConnectionState
import com.example.sailboatapp.presentation.network.connectionState
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import java.io.BufferedReader
import java.io.InputStreamReader


@Composable
fun Polars(
    navController: NavHostController,
    isSwippeEnabled: Boolean,
    localViewModel: LocalViewModel,
    remoteViewModel: RemoteViewModel,
    onSwipeChange: (Boolean) -> Unit
) {
    onSwipeChange(false)

    var polarRecState by remember {
        mutableStateOf(false)
    }
    var polarString by remember {
        mutableStateOf("load")
    }

    val listState = rememberScalingLazyListState()
    val vignetteState by remember { mutableStateOf(VignettePosition.TopAndBottom) }



    val showVignette by remember {
        mutableStateOf(true)
    }

    //var connectionState: ConnectionState by remember { mutableStateOf(ConnectionState.Loading) }

    //var stimeVelocita = JsonObject()

    var showDialog by remember { mutableStateOf(false) }
    var showChart by remember { mutableStateOf(false) }

    /*val localViewModel: LocalViewModel = viewModel()
    val remoteViewModel: RemoteViewModel = viewModel()*/



    if(connectionState == ConnectionState.Local){
        //recInfo local
        val recInfoUiState: RecInfoState = localViewModel!!.recInfoState

        when (recInfoUiState) {
            is RecInfoState.Error -> if(LOG_ENABLED) Log.d("DEBUG","Error recInfo local")
            is RecInfoState.Loading -> if(LOG_ENABLED) Log.d("DEBUG","Loading recInfo local")
            is RecInfoState.Success -> {
                val result = (localViewModel!!.recInfoState as RecInfoState.Success).infoRec
                if(LOG_ENABLED) Log.d("DEBUG","Success: RecInfo local $result")
                if(result == "true"){
                    polarRecState = true
                    polarString = "Termina"
                }else{
                    polarRecState = false
                    polarString = "Inizia registrazione"
                }
            }
        }

        //Stime velocita local
        val stimeVelocitaUiState: StimeVelocitaUiState = localViewModel!!.stimeVelocitaUiState

        when (stimeVelocitaUiState) {
            is StimeVelocitaUiState.Error -> if(LOG_ENABLED) Log.d("DEBUG","Error stime velocita local")
            is StimeVelocitaUiState.Loading -> if(LOG_ENABLED) Log.d("DEBUG","Loading stime velocita local")
            is StimeVelocitaUiState.Success -> {
                connectionState = ConnectionState.Local
                val result =
                    (localViewModel!!.stimeVelocitaUiState as StimeVelocitaUiState.Success).stimeVelocita
                if(LOG_ENABLED) Log.d("DEBUG","Success: Stime velocita local $result")

                stimeVelocita = result
                result.keySet().forEach{
                    if(it == "inProgress"){
                        if(LOG_ENABLED) Log.d("DEBUG","Calcolo in corso")
                    }


                }
                //if(LOG_ENABLED) Log.d("DEBUG","keyset = " + result.keySet().toString())
                //stimeVelocita = Gson().fromJson(result, Array<JsonObject>::class.java)
                //if(LOG_ENABLED) Log.d("DEBUG","Stime velocita: $stimeVelocita")
            }
        }

    }else if(connectionState == ConnectionState.Remote){
        //Stime velocita remote
        val getstimeRemoteUiState: GetStimeRemoteUiState = remoteViewModel!!.getStimeRemoteUiState

        when (getstimeRemoteUiState) {
            is GetStimeRemoteUiState.Error -> if(LOG_ENABLED) Log.d("DEBUG","Error stime velocita remote")
            is GetStimeRemoteUiState.Loading -> if(LOG_ENABLED) Log.d("DEBUG","Loading stime velocita remote")
            is GetStimeRemoteUiState.Success -> {
                //println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
                if(LOG_ENABLED) Log.d("DEBUG","Success: Stime velocita remote")
                stimeVelocita = Gson().fromJson(
                    (remoteViewModel!!.getStimeRemoteUiState as GetStimeRemoteUiState.Success).stime,
                    JsonObject::class.java
                )
                if(LOG_ENABLED) Log.d("DEBUG","Success: Stime velocita remote $stimeVelocita")
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
    }/*, pageIndicator = {
        HorizontalPageIndicator(pageIndicatorState = pageIndicatorState)
    }*/) {

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState,
            verticalArrangement = Arrangement.Center,
            autoCentering = AutoCenteringParams(itemIndex = 3),
        ) {
            item(10) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "Polari"
                )
            }
            item { Spacer(modifier = Modifier.height(10.dp)) }
            item {
                Button(//Back Button
                    onClick = {
                        navController.navigate("homepage")

                    }, colors = ButtonDefaults.buttonColors(
                        //backgroundColor = Color(orange), // Background color
                        contentColor = Color.Black
                    ), modifier = Modifier
                        //.size(30.dp)
                        .height(30.dp)
                        .width(50.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_back),
                        contentDescription = "Back",
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            if(LOG_ENABLED) Log.d("DEBUG","connectionState = $connectionState")
            if(connectionState == ConnectionState.Local){
                item { Spacer(modifier = Modifier.height(20.dp)) }
                item {
                    Button(//registra button
                        onClick = {
                            if (polarRecState) {
                                polarRecState = false
                                localViewModel!!.recPolars("")
                                polarString = "Inizia registrazione"
                            } else {
                                polarRecState = true
                                //polarString = "Termina"
                                showDialog = true
                            }
                        }, modifier = Modifier
                            .height(30.dp)
                            .width(150.dp)
                            //.alpha(if (connectionState == ConnectionState.Local) 1f else 0f)
                    ) {
                        Text(polarString)
                    }
                }
                item { Spacer(modifier = Modifier.height(30.dp)) }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(//Calcola button
                            onClick = { localViewModel!!.calculatePolars() },
                            modifier = Modifier
                                .height(30.dp)
                                .width(70.dp)
                                //.alpha(if (connectionState == ConnectionState.Local) 1f else 0f)
                        ) {
                            Text("Calcola")
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Button(//Clear button
                            onClick = { localViewModel!!.clearPolars() },
                            modifier = Modifier
                                .height(30.dp)
                                //.alpha(if (connectionState == ConnectionState.Local) 1f else 0f)
                        ) {
                            Text("Clean")
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(30.dp)) }
            item {
                Button(//grafico button
                    onClick = {
                        showChart = true
                    }, modifier = Modifier
                        .height(30.dp)
                        .width(150.dp)
                ) {
                    Text("Grafico")
                }
            }/*item {
                Column() {
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

                        //if(LOG_ENABLED) Log.d("DEBUG","Vela: $vela")
                        //if(LOG_ENABLED) Log.d("DEBUG","WindAngles: $windAnglesJson")
                        //if(LOG_ENABLED) Log.d("DEBUG","WindSpeeds: $windSpeedsJson")

                        //if(LOG_ENABLED) Log.d("DEBUG",windSpeedsJson.get(0))
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
                        //if(LOG_ENABLED) Log.d("DEBUG","Stime 0 ${stimeJson[0]}")

                        //if(LOG_ENABLED) Log.d("DEBUG","Stime json: $stimeJson")
                        stime.forEach { if(LOG_ENABLED) Log.d("DEBUG","Stime: $it") }

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

                                    //if(LOG_ENABLED) Log.d("DEBUG","Row & Col: $row $col")


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
            }*/
            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
        var textState by remember { mutableStateOf("") }
        Dialog(showDialog = showDialog, onDismissRequest = { showDialog = false }) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Configurazione vele: ")
                Spacer(modifier = Modifier.height(10.dp))
                BasicTextField(modifier = Modifier,
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
                        if(LOG_ENABLED) Log.d("DEBUG","Text= $textState")
                        localViewModel!!.recPolars(textState)
                        if(textState != ""){
                            polarString = "Termina"
                        }
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
        //Dialog chart
        Dialog(showDialog = showChart, onDismissRequest = { showChart = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.Black,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AndroidView(modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.Center),
                    factory = { context ->
                        val v = GeckoView(context)
                        val session = GeckoSession().apply {
                            // Workaround for Bug 1758212
                            setContentDelegate(object : GeckoSession.ContentDelegate {})
                        }
                        val runtimeSettings =
                            GeckoRuntimeSettings.Builder().consoleOutput(true).build()

                        var sRuntime: GeckoRuntime? = null

                        if (sRuntime == null) {
                            // GeckoRuntime can only be initialized once per process
                            //sRuntime = GeckoRuntime.create(context, runtimeSettings)
                            sRuntime = GeckoRuntime.getDefault(context)
                        }

                        session.open(sRuntime)
                        v.setSession(session)

                        /*var vela = ""
                        var windAnglesJson = JsonArray()
                        var windSpeedsJson = JsonArray()
                        var stimeJson = JsonArray()
                        var windAngle = arrayListOf("-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1")
                        var windSpeed = arrayListOf("-1", "-1", "-1", "-1", "-1", "-1", "-1")
                        var stime = ArrayList<Any>()*/

                        stimeVelocita.keySet().forEach{
                            if(it == "inProgress"){
                                if(LOG_ENABLED) Log.d("DEBUG","Calcolo in corso")
                            }


                        }

                        /*stimeVelocita.keySet().forEach {
                            vela = it
                            windAnglesJson =
                                stimeVelocita.getAsJsonObject(it).getAsJsonArray("angoliVento")
                            windSpeedsJson =
                                stimeVelocita.getAsJsonObject(it).getAsJsonArray("velocitaVento")
                            stimeJson = stimeVelocita.getAsJsonObject(it)
                                .getAsJsonArray("stimeVelocitaBarca")

                            if(LOG_ENABLED) Log.d("DEBUG","Vela: $vela")
                            if(LOG_ENABLED) Log.d("DEBUG","WindAngles: $windAnglesJson")
                            if(LOG_ENABLED) Log.d("DEBUG","WindSpeeds: $windSpeedsJson")

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

                            if(LOG_ENABLED) Log.d("DEBUG","Stime json: $stimeJson")
                            stime.forEach { if(LOG_ENABLED) Log.d("DEBUG","Stime: $it") }
                        }*/


                        //val apkURI: URI = File(context.packageResourcePath).toURI()
                        //val assetsURL = "jar:$apkURI!/assets/"
                        //val myURL = assetsURL + "test2.html"

                        //var file = readAssetFile(context, "plotly-2.25.2.min.js")
                        //if(LOG_ENABLED) Log.d("DEBUG","File: $file ${file.length}")

                        var span = ""

                        if(LOG_ENABLED) Log.d("DEBUG","Numero vele: ${stimeVelocita.keySet().size}")
                        if (stimeVelocita.keySet().size > 1) {
                            span =
                                "<span style=\"display:block; height: 100px; padding: 400px;\"></span>"
                        }

                        //<script src="https://cdn.plot.ly/plotly-2.25.2.min.js"></script>
                        //https://bruce.altervista.org/Client/script/plotly-2.25.2.min.js

                        val libraryAddress : String

                        if(isConnectionLocal()){
                            libraryAddress = "http://$raspberryIp:$websockifySocket/script/plotly-2.25.2.min.js"
                        }else{
                            libraryAddress = "https://cdn.plot.ly/plotly-2.25.2.min.js"
                        }

                        /*val test = """
                             <!DOCTYPE html>
<html>
<head>
<title>Page Title</title>
</head>
<style>
        body, html {
            height: 100%;
            margin: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            background-color:  rgb(170, 211, 223);
        }
    </style>
<body>

<h1>This is a Heading</h1>
<p>This is a paragraph.</p>

</body>
</html>
                        """*/

                        val total = """
                            <!DOCTYPE html>
<html lang="it">
   <head>
      <title>Page Title</title>
      <link rel="stylesheet" href='style.css'>
      <style>
         body, html {
         height: 100%;
         margin: 0;
         display: flex;
         justify-content: center;
         align-items: center;
         background-color: rgb(170, 211, 223);
         }
         .centered-div {
         width: 600px;
         height: 600px;
         background-color: lightgray;
         text-align: center;
         }
         .container {
         display: flex;
         flex-direction: column; /* Arrange children in a column */
         justify-content: center; /* Center children vertically */
         align-items: center; /* Center children horizontally */
         height: 100vh; /* Full height of the viewport */
         width: 100%; /* Full width of the viewport */
         box-sizing: border-box;
         }
         .grafico-polare {
         background-color: lightblue;
         padding: 20px;
         margin: 10px 0; /* To separate multiple divs vertically */
         }
      </style>
      <script src="$libraryAddress"></script>
      <script>console.log("Test = Prova1");</script>
   </head>
   <body>
      <div id='container' class="container">
         $span
      </div>
      <div id='stime' style="display:none" >
         $stimeVelocita    
      </div>
      <script>
         console.log("script");
         var stime = document.getElementById("stime").innerHTML;
         var dati = JSON.parse(stime);
         var key = Object.keys(dati);
         if(key == "inProgress"){             
             var title = document.createElement("h1");
             document.getElementById("container").appendChild(title);
             title.innerHTML = "Calcolo in corso";
             location.reload();
            
         }else{
             draw(JSON.parse(stime));
         }
         
         function draw(data) {
             var colori = ["blue", "red", "green", "orangered", "teal", "black", "purple", "orange", "royalblue", "springgreen", "deeppink"];
             var jsonObject = data;    
             console.log(typeof jsonObject);
             console.log(jsonObject);
         
             const keys = Object.keys(jsonObject);
             console.log(keys);
             
              keys.forEach((key) => {
                 var vela = key;
                 var velocitaVento = jsonObject[key]["velocitaVento"];
                 var angoliVento = jsonObject[key]["angoliVento"];
                 var sizeVelocita = velocitaVento.length;
                 var sizeAngoli = angoliVento.length;
         
                 var angoloVento = angoliVento;
                 var i, j;
                 var dati = [];
                 for (j = 0; j < sizeVelocita; j++) {
                     var temp = [];
                     for (i = 0; i < sizeAngoli; i++) {
                         temp[i] = jsonObject[key]["stimeVelocitaBarca"][i][j].toFixed(2);
                     }
                     var data = {
                         r: temp,
                         theta: angoloVento,
                         type: "scatterpolar",
                         line: {
                             color: colori[j],
                             width: 3,
                         },
                         marker: {
                             size: 10,
                         },
                         name: velocitaVento[j] + "kn",
                     };
                     dati[j] = data
                 }
                 console.log(dati);        
                 var config = {
                     displayModeBar: false,
                     staticPlot: true,
                 };
                 var layout = {
                     polar: {
                         bgcolor: 'rgb(137, 185, 199)',
                         radialaxis: {
                             angle: 90,
                             range: [2, 4, 6, 8, 10],
                             tickfont: {
                                 size: 26,
                             },
                             startangle: 45,
                         },
                         angularaxis: {
                             tickmode: "array",
                             tickvals: [0, 45, 52, 60, 75, 90, 110, 120, 135, 150, 165, 180],
                             ticktext: [0, 45, 52, 60, 75, 90, 110, 120, 135, 150, 165, 180].map((angle) => angle + "\u00B0"),
                             tickfont: {
                                 size: 26,
                             },
                             direction: "counterclockwise",
                             constrain: "domain",
                         },
                         sector: [-90, 90],
                     },
                     showlegend: false,
                     paper_bgcolor: 'rgb(170, 211, 223)',
                     width: 700,
                     height: 700,
                     margin: {
                         t: 0,
                         b: 0,
                         l: 10,
                         r: 0,
                     },
         
                 };
                 
                 var grafico = document.createElement("div");
                 var title = document.createElement("h1");
                 title.innerHTML = key;
                 grafico.className = "grafico-polare";
                 document.getElementById("container").appendChild(title);
                 document.getElementById("container").appendChild(grafico);
                 Plotly.newPlot(grafico, dati, layout, config);
                 ruotaGrafico(grafico);
              
              });
              function ruotaGrafico(graficoNuovo) {
                 var angolare = graficoNuovo._fullLayout.polar.angularaxis;
                 var nuovaDirezione = angolare.direction === "clockwise" ? "counterclockwise" : "clockwise";
                 Plotly.relayout(graficoNuovo, {
                     "polar.angularaxis.direction": nuovaDirezione
                 });
             }
                 
             
             
         }    
      </script>
   </body>
</html>           
        """

               /*         val test2 = """
                            <!DOCTYPE html>
<html lang="it">
   <head>
      <title>Page Title</title>
      <link rel="stylesheet" href='style.css'>
      <style>
         body, html {
         height: 100%;
         margin: 0;
         display: flex;
         justify-content: center;
         align-items: center;
         background-color: rgb(170, 211, 223);
         }
      </style>
      <script src="http://$raspberryIp:8080/script/plotly-2.25.2.min.js"></script>
      <script>console.log("Test = Prova1");</script>
   </head>
   <body>
      <div id='container' class="container">
         $span
         <h1>Test</h1>
      </div>
      <div id='stime' style="display:none" >
         $stimeVelocita
      </div>
      <script>
         console.log("script");
         var stime = document.getElementById("stime").innerHTML;
         var dati = JSON.parse(stime);
         var key = Object.keys(dati);

      </script>
   </body>
</html>
        """*/



                        session.load(GeckoSession.Loader().data(total, "text/html"))

                        //session.load(GeckoSession.Loader().uri(myURL))


                        v

                    }, update = { v ->
                    })
            }
        }
    }

}

fun createDiv(id: String, content: String): String {

    return "<div id=$id>$content</div>"

}
fun readAssetFile(context: Context, fileName: String): String {
    return context.assets.open(fileName).use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.readText()
        }
    }
}







