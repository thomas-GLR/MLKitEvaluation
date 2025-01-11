package com.example.mlkitevaluation.vo

data class TotalTextGroundTruth(
    val x: List<List<Float>>,
    val y: List<List<Float>>,
    val orientation: TotalTextOrientation,
    val transcription: String
)