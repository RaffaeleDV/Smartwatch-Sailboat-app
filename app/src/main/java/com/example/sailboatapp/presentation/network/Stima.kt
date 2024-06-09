package com.example.sailboatapp.presentation.network

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class Stima (
    var vela : String,
    var velocitaVento : JsonArray,
    var angoliVento :JsonArray,
    var stimeVelocitaBarca : JsonArray
    )