package com.example.sailboatapp.presentation.ui.screen

import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sailboatapp.presentation.data.readNMEA
import com.example.sailboatapp.presentation.model.Anchor
import com.example.sailboatapp.presentation.network.LocalApi
import com.example.sailboatapp.presentation.network.LocalWebSocketListener
import com.example.sailboatapp.presentation.model.Raffica
import com.example.sailboatapp.presentation.network.ConnectionState
import com.example.sailboatapp.presentation.network.connectionState
import com.google.gson.JsonObject
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

sealed interface GetAnchorLocalUiState {
    data class Success(val anchor: Anchor) : GetAnchorLocalUiState
    object Error : GetAnchorLocalUiState
    object Loading : GetAnchorLocalUiState
}

sealed interface SetAnchorLocalUiState {
    data class Success(val result: Unit) : SetAnchorLocalUiState
    object Error : SetAnchorLocalUiState
    object Loading : SetAnchorLocalUiState
}

sealed interface StimeVelocitaUiState {
    data class Success(val stimeVelocita: JsonObject) : StimeVelocitaUiState
    object Error : StimeVelocitaUiState
    object Loading : StimeVelocitaUiState
}

sealed interface RecInfoState {
    data class Success(val infoRec: String) : RecInfoState
    object Error : RecInfoState
    object Loading : RecInfoState
}

private var i = 0
private var j = 0
class LocalViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    private val _data = MutableStateFlow<HashMap<String, String>>(HashMap<String, String>())
    val data: StateFlow<HashMap<String, String>> = _data
    var rafficaUiState: RafficaUiState by mutableStateOf(RafficaUiState.Loading)
        private set
    var getAnchorUiState: GetAnchorLocalUiState by mutableStateOf(GetAnchorLocalUiState.Loading)
        private set
    var setAnchorUiState: SetAnchorLocalUiState by mutableStateOf(SetAnchorLocalUiState.Loading)
        private set
    var stimeVelocitaUiState: StimeVelocitaUiState by mutableStateOf(StimeVelocitaUiState.Loading)
        private set
    var recInfoState: RecInfoState by mutableStateOf(RecInfoState.Loading)
        private set

    /**
     * Call on init so we can display status immediately.
     */
    init {
        if(LOG_ENABLED) Log.d("DEBUG","Connessione locale: init $i")
        getNmeaLocal()
        startRepeatingRequests()
        i++
    }

    /**
     * Gets information from the  API Retrofit service and updates
     *
     */
    private fun startRepeatingRequests() {
        viewModelScope.launch {
            while (true) {
                //if (connectionState == ConnectionState.Local) {
                    if (LOG_ENABLED) Log.d("DEBUG", "Connessione locale: repeat $j of $i")
                    getRaffica()
                    getAnchor()
                    getStimeVelocita()
                    recInfo()
                    delay(5000) // Delay for 5 seconds
                    j++
                //}
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

    fun getStimeVelocita(){
        viewModelScope.launch {
            stimeVelocitaUiState = try {
                //println("Try raffica")
                val result = LocalApi.retrofitService.getStimeVelocita()

                //println("Raffica vel: "+ result.velVento)
                StimeVelocitaUiState.Success(
                    result
                )
            } catch (e: IOException) {
                StimeVelocitaUiState.Error
            }
        }

    }

    fun getAnchor() {
        viewModelScope.launch {
            getAnchorUiState = try {
                delay(10000)
                //println("Try raffica")
                val result = LocalApi.retrofitService.getAnchor()
                //println("Ancora: "+ result)
                GetAnchorLocalUiState.Success(
                    result
                )
            } catch (e: IOException) {
                GetAnchorLocalUiState.Error
            }
        }
    }

    fun calculatePolars() {
        viewModelScope.launch {
            try {
                val result = LocalApi.retrofitNmeaForwarderService.calculatePolars()
                if(LOG_ENABLED)Log.d("DEBUG","calculatePolars: $result")

            } catch (e: IOException) {

            }
        }
    }

    fun clearPolars() {
        viewModelScope.launch {
            try {
                val result = LocalApi.retrofitNmeaForwarderService.clearPolars()
                if(LOG_ENABLED)Log.d("DEBUG","clearPolars: $result")

            } catch (e: IOException) {

            }
        }
    }

    fun recPolars(sails : String) {
        viewModelScope.launch {
            try {
                val result = LocalApi.retrofitNmeaForwarderService.recPolars(sails)
                if(LOG_ENABLED)Log.d("DEBUG","recPolars: $result")

            } catch (e: IOException) {

            }
        }
    }
    private fun recInfo() {
        viewModelScope.launch {
            try {
                //println("recInfo")
                val result = LocalApi.retrofitNmeaForwarderService.recInfo()
                recInfoState = RecInfoState.Success(
                    result
                )
                if(LOG_ENABLED)Log.d("DEBUG","recInfo: $result")
            } catch (e: IOException) {
                recInfoState = RecInfoState.Error

            }
        }
    }

    fun setAnchor(latitude : String, longitude : String, anchored : String) {
        viewModelScope.launch {
            try {
                val result = LocalApi.retrofitNmeaForwarderService.setAnchor(latitude, longitude, anchored)
                if(LOG_ENABLED)Log.d("DEBUG","Ancora set: "+ result)
                setAnchorUiState = SetAnchorLocalUiState.Success(
                    result
                )
            } catch (e: IOException) {
                setAnchorUiState = SetAnchorLocalUiState.Error
            }
        }
    }

    private fun getNmeaLocal() {
        val client = OkHttpClient()
        val request = Request.Builder().url("ws://$raspberryIp:$websockifySocket").get().build()
        val listener = LocalWebSocketListener { bytes ->
            viewModelScope.launch {
                _data.value = processData(bytes) // Update the state with the received data
                if(LOG_ENABLED)Log.d("DEBUG","Connessione locale: nmea local received")
            }
        }
        val webSocket: WebSocket = client.newWebSocket(request, listener)
        //println(webSocket.request().body.toString())

        client.dispatcher.executorService.shutdown() // Optional: Clean up the client resources
    }

    private fun processData(bytes: ByteString): HashMap<String, String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            readNMEA(String(Base64.getDecoder().decode(bytes.base64())))
        } else {
            readNMEA(String(android.util.Base64.decode(bytes.base64(), android.util.Base64.DEFAULT)))
        }
    }

}