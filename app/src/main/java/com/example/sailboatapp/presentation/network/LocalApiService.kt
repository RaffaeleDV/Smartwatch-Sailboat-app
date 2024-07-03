package com.example.sailboatapp.presentation.network

import com.example.sailboatapp.presentation.model.Anchor
import com.example.sailboatapp.presentation.model.Raffica
import com.example.sailboatapp.presentation.ui.screen.raspberryIp
import com.example.sailboatapp.presentation.ui.screen.websockifySocket
import com.google.gson.JsonObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


//private const val SOCKET_BASE_URL = "8080" //webserver websockify

private const val SOCKET_NMEA_FORWARDER = "8000" //webserver nmea forwarder

//private const val BASE_URL_NMEA_FORWARDER = "http://192.168.178.48" //Websocket




private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl("http://$raspberryIp:$websockifySocket/")
    .build()

private val retrofitNmeaForwarder = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl("http://$raspberryIp:$SOCKET_NMEA_FORWARDER/")
    .build()

interface LocalApiService{
    @GET("file.json")
    suspend fun getRaffica() : Raffica

    @GET("ancora.json")
    suspend fun getAnchor() : Anchor
    @GET("stimeVelocita.json")
    suspend fun getStimeVelocita() : JsonObject

    @GET("ancora")
    suspend fun setAnchor(@Query("latitudine")latitude : String, @Query("longitudine")longitude : String, @Query("ancorato")anchored : String)

    @GET("ancora?pulsanteCalcola=true")
    suspend fun calculatePolars()
    @GET("ancora")
    suspend fun recPolars(@Query("vele") sails : String)
    @GET("ancora?info=true")
    suspend fun recInfo() : String
    @GET("ancora?pulsanteClear=true")
    suspend fun clearPolars()


}

object LocalApi{
    val retrofitService : LocalApiService by lazy {
        retrofit.create(LocalApiService::class.java)
    }
    val retrofitNmeaForwarderService : LocalApiService by lazy {
        retrofitNmeaForwarder.create(LocalApiService::class.java)
    }
}