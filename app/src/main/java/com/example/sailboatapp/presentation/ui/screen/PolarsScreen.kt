package com.example.sailboatapp.presentation.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    var showDialog by remember { mutableStateOf(false) }

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
            item { Spacer(modifier = Modifier.height(50.dp)) }
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