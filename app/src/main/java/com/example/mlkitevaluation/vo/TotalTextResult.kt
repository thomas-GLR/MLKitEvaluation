package com.example.mlkitevaluation.vo

data class TotalTextResult(
    val orientation: TotalTextOrientation,
    var charDistanceInError: Int,
    var totalChars: Int,
    var wordDistanceInError: Int,
    var totalWords: Int
) {
    constructor(orientation: TotalTextOrientation) : this(orientation, 0, 0, 0, 0)

    fun updateTotalTextResult(charDistance: Int, totalChars: Int, wordDistance: Int, totalWords: Int) {
        this.charDistanceInError += charDistance
        this.totalChars += totalChars
        this.wordDistanceInError += wordDistance
        this.totalWords += totalWords
    }
}