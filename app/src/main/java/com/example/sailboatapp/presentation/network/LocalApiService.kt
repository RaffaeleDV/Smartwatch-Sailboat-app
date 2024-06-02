package com.example.sailboatapp.presentation.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

private const val BASE_URL =
    "http://192.168.178.48:8080/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface LocalApiService{
    @GET("file.json")
    suspend fun getRaffica(/*@Path("user")  user: String*/) : Raffica

    @GET("ancora.json")
    suspend fun getAnchor() : Anchor
}

object LocalApi{
    val retrofitService : LocalApiService by lazy {
        retrofit.create(LocalApiService::class.java)
    }
}