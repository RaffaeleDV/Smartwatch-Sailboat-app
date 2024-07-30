package com.example.sailboatapp.presentation.ui.screen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sailboatapp.presentation.network.RemoteApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException


private var i = 0
private var j = 0

sealed interface RemoteUiState {
    data class Success(val nmea: String) : RemoteUiState
    object Error : RemoteUiState
    object Loading : RemoteUiState
}

sealed interface GetAnchorRemoteUiState {
    data class Success(val anchor: String) : GetAnchorRemoteUiState
    object Error : GetAnchorRemoteUiState
    object Loading : GetAnchorRemoteUiState
}

sealed interface SetAnchorRemoteUiState {
    data class Success(val anchor: String) : SetAnchorRemoteUiState
    object Error : SetAnchorRemoteUiState
    object Loading : SetAnchorRemoteUiState
}

sealed interface GetStimeRemoteUiState {
    data class Success(val stime: String) : GetStimeRemoteUiState
    object Error : GetStimeRemoteUiState
    object Loading : GetStimeRemoteUiState
}

class RemoteViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var remoteUiState: RemoteUiState by mutableStateOf(RemoteUiState.Loading)
        private set
    var getAnchorRemoteUiState: GetAnchorRemoteUiState by mutableStateOf(GetAnchorRemoteUiState.Loading)
        private set
    var setAnchorRemoteUiState: SetAnchorRemoteUiState by mutableStateOf(SetAnchorRemoteUiState.Loading)
        private set
    var getStimeRemoteUiState: GetStimeRemoteUiState by mutableStateOf(GetStimeRemoteUiState.Loading)

    /**
     * Call on init so we can display status immediately.
     */
    init {
        if (LOG_ENABLED) Log.d("DEBUG", "Connessione remota: init $i")
        startRepeatingRequests()
        i++
    }

    private fun startRepeatingRequests() {
        viewModelScope.launch {
            while (true) {
                /*if(connectionState == ConnectionState.Remote){*/
                if (LOG_ENABLED) Log.d("DEBUG", "Connessione remota: repeat $j of $i")
                getNmeaRemote()
                getAnchor()
                getStimeVelocita()
                delay(5000) // Delay for 5 seconds
                j++
                //}
            }
        }
    }

    /**
     * Gets information from the  API Retrofit service and updates
     *
     */
    fun getNmeaRemote() {
        viewModelScope.launch {
            remoteUiState = try {
                //("Try")
                val result = RemoteApi.retrofitService.getNmea()

                RemoteUiState.Success(
                    result
                )
            } catch (e: IOException) {
                RemoteUiState.Error
            }
        }
    }

    fun getAnchor() {
        viewModelScope.launch {
            getAnchorRemoteUiState = try {
                //("Try")
                val result = RemoteApi.retrofitService.getAncora()
                GetAnchorRemoteUiState.Success(
                    result
                )
            } catch (e: IOException) {
                GetAnchorRemoteUiState.Error
            }
        }
    }

    fun setAnchor(body: String) {
        viewModelScope.launch {
            setAnchorRemoteUiState = try {
                //("Try")
                val result = RemoteApi.retrofitService.setAncora(body)
                SetAnchorRemoteUiState.Success(
                    result
                )
            } catch (e: IOException) {
                SetAnchorRemoteUiState.Error
            }
        }
    }

    fun getStimeVelocita() {
        viewModelScope.launch {
            getStimeRemoteUiState = try {
                //("Try")
                val result = RemoteApi.retrofitService.getStime()
                GetStimeRemoteUiState.Success(
                    result
                )
            } catch (e: IOException) {
                GetStimeRemoteUiState.Error
            }
        }
    }
}
