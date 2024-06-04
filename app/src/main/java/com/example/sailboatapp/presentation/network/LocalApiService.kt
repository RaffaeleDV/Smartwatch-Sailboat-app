package com.example.sailboatapp.presentation.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL =
    "http://192.168.178.48:8080/"

private const val BASE_URL_NMEA_FORWARDER =
    "http://192.168.178.48:8000/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private val retrofitNmeaForwarder = Retrofit.Builder()
    //.addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL_NMEA_FORWARDER)
    .build()

interface LocalApiService{
    @GET("file.json")
    suspend fun getRaffica(/*@Path("user")  user: String*/) : Raffica

    @GET("ancora.json")
    suspend fun getAnchor() : Anchor

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