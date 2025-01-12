package com.example.mlkitevaluation.vo

data class TotalTextImageResult(
    val imageName: String,
    val datasetLabel: String,
    val mlKitElement: String,
    val charDistance: Int,
    val wordDistance: Int
)