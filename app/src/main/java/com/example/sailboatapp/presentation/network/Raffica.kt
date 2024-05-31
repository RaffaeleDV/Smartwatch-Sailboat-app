package com.example.sailboatapp.presentation.network

import kotlinx.serialization.Serializable

@Serializable
data class Raffica(
    val tempo: String, val angolo: String,
    //@SerialName(value = "full_name")
    val velVento: String
)