package com.example.sailboatapp.presentation.network

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Anchor(
    @SerializedName(value = "latitudine")
    val latitude: String,
    @SerializedName(value = "longitudine")
    val longitude: String,
    @SerializedName(value = "ancorato")
    val anchored: String,
    @SerializedName(value = "tempo")
    val time: String

)
