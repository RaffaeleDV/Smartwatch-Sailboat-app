package com.example.sailboatapp.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sailboatapp.presentation.ui.screen.LocalViewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.sailboatapp.presentation.ui.screen.Homepage
import com.example.sailboatapp.presentation.ui.screen.Polars
import com.example.sailboatapp.presentation.ui.screen.Map


/*fun connectToWebSocket() {

}*/



@Composable
fun SailboatApp() {



    //localConnection()

   /* val remoteViewModel: RemoteViewModel = viewModel()

    val remoteUiState : RemoteUiState = remoteViewModel.remoteUiState

    when(remoteUiState){
        is RemoteUiState.Error -> println("Error")
        is RemoteUiState.Loading -> println("Loading")
        is RemoteUiState.Success -> println((remoteViewModel.remoteUiState as RemoteUiState.Success).nmea)
    }
*/



    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        modifier = Modifier,
        //.fillMaxHeight()
        //.fillMaxWidth(),
        navController = navController,
        userSwipeEnabled = false,
        startDestination = "homepage"
    ) {
        composable("homepage") {
            Homepage(navController)
        }
        composable("polars") {
            Polars(navController)
        }
        composable("map") {
            Map(navController)
        }
    }
}



/*
@Composable
fun Homepage(navController: NavHostController) {

    val listState = rememberScalingLazyListState()
    val vignetteState by remember {  mutableStateOf(VignettePosition.TopAndBottom) }

    val maxPages = 3
    var selectedPage by remember { mutableStateOf(1) }
    var finalValue by remember { mutableStateOf(0) }

    val animatedSelectedPage by animateFloatAsState(
        targetValue = selectedPage.toFloat(),
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

    val showVignette  by remember {
        mutableStateOf(true)
    }

    Scaffold(
        modifier = Modifier,
        // .fillMaxWidth()
        // .fillMaxHeight(),
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState,

                modifier = Modifier
            )
        },
        vignette = {
            if (showVignette) {
                Vignette(vignettePosition = vignetteState)
            }
        },
        timeText = {
            TimeText()
        },
        pageIndicator = {
            HorizontalPageIndicator(pageIndicatorState = pageIndicatorState)
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState,
            // verticalArrangement = Arrangement.Top
        ) {
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "Vel. Vento:"
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 15.sp,
                    text = "12.6kn"
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
                    text = "42.6kn"
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
                    text = "22°"
                )
            }
            item { Spacer(modifier = Modifier.height(10.dp)) }
            item {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    Button(//Polars page button

                        onClick = { navController.navigate("polars") },
                        modifier = Modifier
                            .height(30.dp)
                        //.width(150.dp)
                    ) {
                        Text("Polari")

                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(//Map page button

                        onClick = { navController.navigate("map") },
                        modifier = Modifier
                            .height(30.dp)
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
        }
    }
    /*Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )*/
}


@Composable
fun Polars(navController: NavHostController) {

    var polarRecState by remember {
        mutableStateOf(false)
    }
    var polarString by remember {
        mutableStateOf("Inizia Registrazione")
    }

    val listState = rememberScalingLazyListState()
    var vignetteState by remember {  mutableStateOf(VignettePosition.TopAndBottom)}

    val maxPages = 3
    var selectedPage by remember { mutableStateOf(0) }
    var finalValue by remember { mutableStateOf(0) }

    val animatedSelectedPage by animateFloatAsState(
        targetValue = selectedPage.toFloat(),
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

    var showVignette  by remember {
        mutableStateOf(true)
    }

    var showDialog by remember { mutableStateOf(false) }



    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState,
                modifier = Modifier
            )
        },
        vignette = {

            if (showVignette) {
                Vignette(vignettePosition = vignetteState)
            }
        },
        timeText = {
            TimeText()
        },

        pageIndicator = {
            HorizontalPageIndicator(pageIndicatorState = pageIndicatorState)
        }

    ) {
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
            item { Spacer(modifier = Modifier.height(50.dp)) }
            item {
                Button(//registra button

                    onClick = {
                        if(polarRecState){
                            polarRecState = false
                            polarString = "Inizia registrazione"

                        }
                        else{
                            polarRecState = true
                            polarString = "Termina"
                            showDialog = true
                        }


                    },
                    modifier = Modifier
                        .height(30.dp)
                        .width(150.dp)
                ) {
                    Text(polarString)

                }
            }
            item {
                var textState by remember { mutableStateOf("") }
                /*BasicTextField(
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

                Dialog(
                    showDialog = showDialog,
                    onDismissRequest = { showDialog = false}) {
                    Column (
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Configurazione vele: ")
                        Spacer(modifier = Modifier.height(10.dp))
                        BasicTextField(
                            modifier = Modifier,//.absoluteOffset { IntOffset(50,160) },
                            value = textState,
                            onValueChange = {textState = it},
                            textStyle = TextStyle.Default,
                            singleLine = true,
                            decorationBox = {
                                    innerTextField ->
                                Row (
                                    modifier = Modifier
                                        .background(MaterialTheme.colors.primary)

                                        .padding(5.dp)


                                ) {
                                    innerTextField()

                                }
                            }

                        )
                        Spacer(modifier = Modifier.height(20.dp))
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
        }
    }

}


@Composable
fun Map(navController: NavHostController){
    var ship_position  by remember { mutableStateOf( LatLng(40.6, 14.75))}
    var destination_position by remember { mutableStateOf( LatLng(0.0, 0.0))}
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

    //BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.ic_action_ship_marker)
    //Bitmap.createScaledBitmap(BitmapFactory.decodeFile("/markerIcon_out"),100,100,false)
    //Bitmap.createScaledBitmap(BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.ic_action_ship_marker),100,100,false)

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


 */