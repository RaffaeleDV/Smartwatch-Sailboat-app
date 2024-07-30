package com.example.sailboatapp.presentation.model

import com.google.gson.annotations.SerializedName
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
