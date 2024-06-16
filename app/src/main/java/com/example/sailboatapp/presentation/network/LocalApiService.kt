package com.example.sailboatapp.presentation.network

import com.example.sailboatapp.presentation.ui.screen.BASE_URL
import com.google.gson.JsonObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val SOCKET_BASE_URL = "8080"

/*private const val BASE_URL_NMEA_FORWARDER =
    "http://192.168.178.48" //Websocket*/
private const val SOCKET_NMEA_FORWARDER = "8000"


private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl("http://$BASE_URL:$SOCKET_BASE_URL/")
    .build()

private val retrofitNmeaForwarder = Retrofit.Builder()
    //.addConverterFactory(GsonConverterFactory.create())
    .baseUrl("http://$BASE_URL:$SOCKET_NMEA_FORWARDER/")
    .build()

interface LocalApiService{
    @GET("file.json")
    suspend fun getRaffica(/*@Path("user")  user: String*/) : Raffica

    @GET("ancora.json")
    suspend fun getAnchor() : Anchor
    @GET("stimeVelocita.json")
    suspend fun getStimeVelocita() : JsonObject

    @GET("ancora")
    suspend fun setAnchor(@Query("latitudine")latitude : String, @Query("longitudine")longitude : String, @Query("ancorato")anchored : String)
}

object LocalApi{
    val retrofitService : LocalApiService by lazy {
        retrofit.create(LocalApiService::class.java)
    }
    val retrofitNmeaForwarderService : LocalApiService by lazy {
        retrofitNmeaForwarder.create(LocalApiService::class.java)
    }
}