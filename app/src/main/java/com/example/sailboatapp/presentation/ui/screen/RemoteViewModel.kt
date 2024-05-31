package com.example.sailboatapp.presentation.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sailboatapp.presentation.network.RemoteApi
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface RemoteUiState {
    data class Success(val nmea: String) : RemoteUiState
    object Error : RemoteUiState
    object Loading : RemoteUiState
}

class RemoteViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var remoteUiState: RemoteUiState by mutableStateOf(RemoteUiState.Loading)
        private set
    /**
     * Call on init so we can display status immediately.
     */
    init {
        getNmeaRemote()
    }
    /**
     * Gets information from the  API Retrofit service and updates
     *
     */
    fun getNmeaRemote() {
        viewModelScope.launch {
            remoteUiState = try {
                println("Try")
                val result = RemoteApi.retrofitService.getNmea()
                RemoteUiState.Success(
                    result
                )
            }catch (e: IOException){
                RemoteUiState.Error
            }
        }
    }
}
