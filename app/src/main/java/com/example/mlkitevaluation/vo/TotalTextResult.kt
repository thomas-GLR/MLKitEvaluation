package com.example.mlkitevaluation.vo

data class TotalTextResult(
    val orientation: TotalTextOrientation,
    var charDistance: Int,
    var totalChars: Int,
    var wordDistance: Int,
    var totalWords: Int
) {
    constructor(orientation: TotalTextOrientation) : this(orientation, 0, 0, 0, 0)

    fun updateTotalTextResult(charDistance: Int, totalChars: Int, wordDistance: Int, totalWords: Int) {
        this.charDistance += charDistance
        this.totalChars += totalChars
        this.wordDistance += wordDistance
        this.totalWords += totalWords
    }
}