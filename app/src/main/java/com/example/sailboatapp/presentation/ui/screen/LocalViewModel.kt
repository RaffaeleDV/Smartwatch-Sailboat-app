package com.example.sailboatapp.presentation.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sailboatapp.presentation.data.readNMEA
import com.example.sailboatapp.presentation.network.Anchor
import com.example.sailboatapp.presentation.network.LocalApi
import com.example.sailboatapp.presentation.network.LocalWebSocketListener
import com.example.sailboatapp.presentation.network.Raffica
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString
import java.io.IOException
import java.util.Base64

sealed interface RafficaUiState {
    data class Success(val raffica: Raffica) : RafficaUiState
    object Error : RafficaUiState
    object Loading : RafficaUiState
}

sealed interface AnchorLocalUiState {
    data class Success(val anchor: Anchor) : AnchorLocalUiState
    object Error : AnchorLocalUiState
    object Loading : AnchorLocalUiState
}


class LocalViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    private val _data = MutableStateFlow<HashMap<String, String>>(HashMap<String, String>())
    val data: StateFlow<HashMap<String, String>> = _data
    var rafficaUiState: RafficaUiState by mutableStateOf(RafficaUiState.Loading)
        private set

    var anchorUiState: AnchorLocalUiState by mutableStateOf(AnchorLocalUiState.Loading)
        private set

    /**
     * Call on init so we can display status immediately.
     */
    init {
        getNmeaLocal()
        startRepeatingRequests()
    }

    /**
     * Gets information from the  API Retrofit service and updates
     *
     */
    private fun startRepeatingRequests() {
        viewModelScope.launch {
            while (true) {
                getRaffica()
                getAnchor()
                delay(5000) // Delay for 5 seconds
            }
        }
    }

    fun getRaffica() {
        viewModelScope.launch {
            rafficaUiState = try {
                //println("Try raffica")

                val result = LocalApi.retrofitService.getRaffica()

                //println("Raffica vel: "+ result.velVento)
                RafficaUiState.Success(
                    result
                )
            } catch (e: IOException) {
                RafficaUiState.Error
            }
        }
    }

    fun getAnchor() {
        viewModelScope.launch {
            anchorUiState = try {
                //println("Try raffica")
                val result = LocalApi.retrofitService.getAnchor()

                //println("Ancora: "+ result)
                AnchorLocalUiState.Success(
                    result
                )
            } catch (e: IOException) {
                AnchorLocalUiState.Error
            }
        }
    }

    fun getNmeaLocal() {

        val client = OkHttpClient()
        val request = Request.Builder().url("ws://192.168.178.48:8080").get().build()
        val listener = LocalWebSocketListener { bytes ->
            viewModelScope.launch {
                _data.value = processData(bytes) // Update the state with the received data
            }
        }
        val webSocket: WebSocket = client.newWebSocket(request, listener)

        //println(webSocket.request().body.toString())

        client.dispatcher.executorService.shutdown() // Optional: Clean up the client resources
    }

    private fun processData(bytes: ByteString): HashMap<String, String> {
        return readNMEA(String(Base64.getDecoder().decode(bytes.base64())))
    }


}