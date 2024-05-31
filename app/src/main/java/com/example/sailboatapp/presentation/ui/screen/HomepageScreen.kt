package com.example.sailboatapp.presentation.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.sailboatapp.R
import java.util.Calendar
import java.util.TimeZone

@Composable
fun Homepage(navController: NavHostController) {

    val listState = rememberScalingLazyListState()
    val vignetteState by remember {  mutableStateOf(VignettePosition.TopAndBottom) }

    val maxPages = 3

    val localViewModel: LocalViewModel = viewModel()

    var latitude = localViewModel.data.collectAsState()
    //by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }



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
                    text = "latitude.value"
                )
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    fontSize = 15.sp,
                    text = (
                            if(latitude.value.get("latitude").isNullOrEmpty())
                                "0.0"
                            else
                                latitude.value.get("latitude")!!
                    )
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


