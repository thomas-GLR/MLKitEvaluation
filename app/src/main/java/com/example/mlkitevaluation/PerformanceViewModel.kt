package com.example.mlkitevaluation

import android.app.Application
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mlkitevaluation.vo.TotalTextGroundTruth
import com.example.mlkitevaluation.vo.TotalTextOrientation
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.math.min

class PerformanceViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "PerformanceViewModel"

    private lateinit var assetManager: AssetManager
    private val _imagesFilesNames = MutableStateFlow<List<String>>(emptyList())
    val imagesFilesNames: StateFlow<List<String>> = _imagesFilesNames

    init {
        Log.i("ViewModel", "Init view model")
        initViewModel()
        CoroutineScope(Dispatchers.IO).launch {
            loadFilesNames(DatasetConstants.TOTAL_TEXT_IMAGES.folderName)
            processPerformanceEvaluation()
        }
    }

    private fun initViewModel() {
        assetManager = getApplication<Application>().assets
    }

    private fun loadFilesNames(path: String) {
        val assetManager = getApplication<Application>().assets
        val imageFiles = assetManager.list(path) ?: emptyArray()
        if (imageFiles.isEmpty()) {
            Log.i("ViewModel", "Aucune image")
        }
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")

        _imagesFilesNames.value = imageFiles.filter { file ->
            imageExtensions.any { file.endsWith(it, ignoreCase = true) }
        }.toList()
        Log.i("ViewModel", _imagesFilesNames.value.size.toString())
    }

    private fun convertFileNameToBitmap(filePath: String): Bitmap? {
        val `is`: InputStream
        var bitmap: Bitmap? = null
        try {
            `is` = assetManager.open(filePath)
            bitmap = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(
                TAG,
                "Une erreur est survenue lors de la création du Bitmap pour l'image $filePath"
            )
            if (e.message != null) {
                Log.e(
                    TAG,
                    e.message!!
                )
            }
        }

        return bitmap
    }

    private fun processPerformanceEvaluation() {
        val list = imagesFilesNames.value.filter { it == "img11.jpg" }
        list.forEach { fileName ->
            val bitmap =
                convertFileNameToBitmap(DatasetConstants.TOTAL_TEXT_IMAGES.folderName + "/" + fileName)

            if (bitmap != null) {
                Log.i(TAG, "Lancement de la détection de texte sur l'image $fileName")
                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(image)
                    .addOnSuccessListener { texts ->
                        Log.i(
                            TAG,
                            "Détection du texte sur l'image $fileName réussi : \n${texts.text}"
                        )
                        texts.textBlocks.forEach {
                            it.lines.forEach {

                            }
                        }
                    }
                    .addOnFailureListener { e -> // Task failed with an exception
                        e.printStackTrace()
                        Log.e(
                            TAG,
                            "Une erreur est survenue lors de la détection de texte sur l'image $fileName"
                        )
                        if (e.message != null) {
                            Log.e(TAG, e.message!!)
                        }
                    }
            } else {
                Log.e(TAG, "Le Bitmap de l'image $fileName n'a pas pu être créé")
            }
        }
    }

    /**
     *
     */
    private fun processOcrResultForTotalText(recognizedText: Text) {

    }

    /**
     * renvoi vrai si la distance de Levenshtein est supérieure à 0, renvoi 1 sinon.
     */
    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Boolean {
        if (lhs == rhs) return false
        if (lhs.isEmpty() || rhs.isEmpty()) return true

        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1
        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1 until rhsLength) {
            newCost[0] = i
            for (j in 1 until lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                newCost[j] = min(
                    min(cost[j] + 1, newCost[j - 1] + 1),
                    cost[j - 1] + match
                )
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength - 1] != 0
    }

    /**
     * Analyse l'emplacement du texte détecté par la librairie avec le ground truth
     */
    private fun textDetectionEvaluation() {

    }

    /**
     * Analyse le texte détécté par la librairie avec le ground truth
     */
    private fun textRecognitionEvaluation() {

    }

    private fun loadGroundTruth(imageName: String): List<TotalTextGroundTruth> {
        val groundTruthPath = "${DatasetConstants.TOTAL_TEXT_GROUND_TRUTH}/poly_gt_$imageName"

        val fileContent = assetManager.open(groundTruthPath).bufferedReader().use {
            it.readText()
        }

        val regex =
            """x: \[\[(.*?)\]\], y: \[\[(.*?)\]\], ornt: \[u'(.*?)'\], transcriptions: \[u'(.*?)'\]""".toRegex()
        val matches = regex.findAll(fileContent)
        return matches.map { match ->
            val xValues = match.groups[1]?.value?.split(" ")?.let { listOf(it) } ?: emptyList()
            val yValues = match.groups[2]?.value?.split(" ")?.let { listOf(it) } ?: emptyList()
            val orientation = match.groups[3]?.value ?: ""
            val transcription = match.groups[4]?.value ?: ""

            val totalTextOrientation = when (orientation) {
                "c" -> TotalTextOrientation.CURVED
                "h" -> TotalTextOrientation.HORIZONTAL
                "m" -> TotalTextOrientation.MUTLI_ORIENTED
                else -> TotalTextOrientation.OTHER
            }

            // On convertit les valeurs en entier
            val x = xValues.map { list -> list.map { it.toInt() } }
            val y = yValues.map { list -> list.map { it.toInt() } }

            TotalTextGroundTruth(
                x = x,
                y = y,
                orientation = totalTextOrientation,
                transcription = transcription
            )
        }.toList()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                if (application == null) {
                    throw IllegalArgumentException("Application is null")
                } else {
                    PerformanceViewModel(application)
                }
            }
        }
    }
}