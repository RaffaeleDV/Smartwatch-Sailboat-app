package com.example.sailboatapp.presentation.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

private const val BASE_URL =
    "https://bruce.altervista.org/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface RemoteApiService {
    @GET("visualizzazione.php")
    suspend fun getNmea(/*@Path("user")  user: String*/): String

    @GET("prendiAncora.php")
    suspend fun getAncora(): String
}

object RemoteApi {
    val retrofitService: RemoteApiService by lazy {
        retrofit.create(RemoteApiService::class.java)
    }
}