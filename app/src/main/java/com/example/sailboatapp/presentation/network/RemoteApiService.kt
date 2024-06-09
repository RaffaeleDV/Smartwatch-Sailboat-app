package com.example.sailboatapp.presentation.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

private const val BASE_URL = "https://bruce.altervista.org/"

private val retrofit =
    Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl(BASE_URL)
        .build()

interface RemoteApiService {
    @GET("visualizzazione.php")
    suspend fun getNmea(/*@Path("user")  user: String*/): String

    @GET("prendiAncora.php")
    suspend fun getAncora(): String

    @POST("inviaAncora.php")
    suspend fun setAncora(@Body body: String): String
    @GET("Client/stimeVelocita.json")
    suspend fun getStime() : String
}

object RemoteApi {
    val retrofitService: RemoteApiService by lazy {
        retrofit.create(RemoteApiService::class.java)
    }
}