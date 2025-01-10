package com.example.mlkitevaluation.vo

data class TotalTextGroundTruth(
    val x: List<List<Int>>,
    val y: List<List<Int>>,
    val orientation: TotalTextOrientation,
    val transcription: String
)