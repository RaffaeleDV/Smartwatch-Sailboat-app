package com.example.sailboatapp.presentation.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sailboatapp.presentation.network.RemoteApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface RemoteUiState {
    data class Success(val nmea: String) : RemoteUiState
    object Error : RemoteUiState
    object Loading : RemoteUiState
}

sealed interface AnchorRemoteUiState {
    data class Success(val anchor: String) : AnchorRemoteUiState
    object Error : AnchorRemoteUiState
    object Loading : AnchorRemoteUiState
}

class RemoteViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var remoteUiState: RemoteUiState by mutableStateOf(RemoteUiState.Loading)
        private set
    var anchorRemoteUiState: AnchorRemoteUiState by mutableStateOf(AnchorRemoteUiState.Loading)
        private set

    /**
     * Call on init so we can display status immediately.
     */
    init {
        startRepeatingRequests()
    }

    private fun startRepeatingRequests() {
        viewModelScope.launch {
            while (true) {
                getNmeaRemote()
                getAnchor()
                delay(5000) // Delay for 5 seconds
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
            anchorRemoteUiState = try {
                //("Try")
                val result = RemoteApi.retrofitService.getAncora()
                AnchorRemoteUiState.Success(
                    result
                )
            } catch (e: IOException) {
                AnchorRemoteUiState.Error
            }
        }
    }
}
