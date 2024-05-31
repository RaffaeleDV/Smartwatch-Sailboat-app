package com.example.sailboatapp.presentation.network

import com.example.sailboatapp.presentation.data.readNMEA
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.Base64

class LocalWebSocketListener(private val onMessageReceived: (ByteString) -> Unit) : WebSocketListener() {
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        println("WebSocket closed: $code / $reason")

    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        webSocket.close(1000, null)
        println("WebSocket is closing: $code / $reason")

    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        println("WebSocket connection failed: ${t.message}")
        //remoteConnection()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        println("Received message: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        //println("Received bytes: $bytes")
        //println(bytes.base64())
        //println("NUOVA "+String(Base64.getDecoder().decode(bytes.base64())))
        onMessageReceived(bytes)

    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        println("WebSocket connection opened")
        //webSocket.send("Hello, World!") // Example message

    }
}