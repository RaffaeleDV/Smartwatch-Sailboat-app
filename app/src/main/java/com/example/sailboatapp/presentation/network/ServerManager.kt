package com.example.sailboatapp.presentation.network

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.sailboatapp.presentation.network.NetworkUtil.isNetworkAvailable
import com.example.sailboatapp.presentation.network.NetworkUtil.isServerReachable
import com.example.sailboatapp.presentation.ui.screen.LOG_ENABLED
import com.example.sailboatapp.presentation.ui.screen.raspberryIp
import com.example.sailboatapp.presentation.ui.screen.websockifySocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

enum class ConnectionState {
    Local, Remote, Loading, Offline
}

var connectionState: ConnectionState = ConnectionState.Loading

class ServerManager(context: Context) : CoroutineScope {
    private val context: Context = context

   private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var serverCheckJob: Job? = null

    fun startServerCheck() {
        serverCheckJob = launch {
            while (isActive) {
                if (isNetworkAvailable(context)) {
                    if(LOG_ENABLED) Log.d("DEBUG","ServerManager: Network is available")
                    withContext(Dispatchers.IO) {
                        if (isServerReachable("http://$raspberryIp:$websockifySocket/")) {
                            if(LOG_ENABLED) Log.d("DEBUG","ServerManager: Local server is reachable")
                            connectionState = ConnectionState.Local
                        } else {
                            if (isServerReachable("https://${ServerConfig.REMOTE_SERVER}/")) {
                                if(LOG_ENABLED) Log.d("DEBUG","ServerManager: Remote server is reachable")
                                connectionState = ConnectionState.Remote
                            } else {
                                if(LOG_ENABLED) Log.d("DEBUG","ServerManager: No server is reachable")
                                connectionState = ConnectionState.Offline
                            }
                        }
                    }
                }
                if(LOG_ENABLED) Log.d("DEBUG","ServerManager: Status: $connectionState")
                delay(5000) // check every 5 seconds
            }
        }
    }

    fun stopServerCheck() {
        serverCheckJob?.cancel()
    }
}