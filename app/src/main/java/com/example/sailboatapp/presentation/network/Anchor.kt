package com.example.sailboatapp.presentation.network

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Anchor(
    @SerializedName(value = "latitudine")
    var latitude: String,
    @SerializedName(value = "longitudine")
    var longitude: String,
    @SerializedName(value = "ancorato")
    var anchored: String,
    @SerializedName(value = "tempo")
    val time: String

)
