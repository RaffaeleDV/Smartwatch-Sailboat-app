package com.example.sailboatapp.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class Raffica(
    val tempo: String, val angolo: String,
    //@SerialName(value = "full_name")
    val velVento: String
)