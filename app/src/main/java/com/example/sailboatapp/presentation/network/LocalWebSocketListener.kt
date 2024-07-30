package com.example.sailboatapp.presentation.network

import android.util.Log
import com.example.sailboatapp.presentation.ui.screen.LOG_ENABLED
import com.example.sailboatapp.presentation.ui.screen.setLocalConnection
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class LocalWebSocketListener(private val onMessageReceived: (ByteString) -> Unit) :
    WebSocketListener() {
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        if (LOG_ENABLED) Log.d("DEBUG", "WebSocket closed: $code / $reason")

    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        webSocket.close(1000, null)
        if (LOG_ENABLED) Log.d("DEBUG", "WebSocket is closing: $code / $reason")

    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        if (LOG_ENABLED) Log.d("DEBUG", "WebSocket connection failed: ${t.message}")
        setLocalConnection(false)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        if (LOG_ENABLED) Log.d("DEBUG", "Received message: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        //if(LOG_ENABLED) Log.d("DEBUG","Received bytes: $bytes")
        //if(LOG_ENABLED) Log.d("DEBUG",bytes.base64())
        //if(LOG_ENABLED) Log.d("DEBUG","NUOVA "+String(Base64.getDecoder().decode(bytes.base64())))
        if (LOG_ENABLED) Log.d("DEBUG", "Connessione locale: websocket message received")
        onMessageReceived(bytes)


    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        if (LOG_ENABLED) Log.d("DEBUG", "WebSocket connection opened")
        setLocalConnection(true)
        //webSocket.send("Hello, World!") // Example message

    }

}