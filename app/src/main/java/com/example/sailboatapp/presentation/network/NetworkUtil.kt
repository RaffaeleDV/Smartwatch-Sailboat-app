package com.example.sailboatapp.presentation.network

import android.content.Context
import android.net.ConnectivityManager
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object NetworkUtil {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun isServerReachable(serverUrl: String?): Boolean {
        try {
            val url = URL(serverUrl)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 5000 // 5 seconds timeout
            urlConnection.connect()
            return urlConnection.responseCode == 200
        } catch (e: IOException) {
            println("Connection failed: ${e.message}")
            return false
        }
    }
}