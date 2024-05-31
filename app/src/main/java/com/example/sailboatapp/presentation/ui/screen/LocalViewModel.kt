package com.example.sailboatapp.presentation.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sailboatapp.presentation.data.readNMEA
import com.example.sailboatapp.presentation.network.LocalWebSocketListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString
import java.util.ArrayList
import java.util.Base64

class LocalViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    private val _data = MutableStateFlow<HashMap<String,String>>(HashMap<String,String>())
    val data: StateFlow<HashMap<String,String>> = _data
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

            val client = OkHttpClient()
            val request = Request.Builder().url("ws://192.168.178.48:8080").get().build()
            val listener = LocalWebSocketListener{ bytes ->
                viewModelScope.launch {
                    _data.value = processData(bytes) // Update the state with the received data
                }
            }

            val webSocket: WebSocket = client.newWebSocket(request, listener)

            println(webSocket.request().body.toString())

            client.dispatcher.executorService.shutdown() // Optional: Clean up the client resources

    }

    private fun processData(bytes: ByteString): HashMap<String,String> {

        return readNMEA(String(Base64.getDecoder().decode(bytes.base64())))


    }


}