package com.example.mlkitevaluation

import android.app.Application
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mlkitevaluation.vo.TotalTextGroundTruth
import com.example.mlkitevaluation.vo.TotalTextImageResult
import com.example.mlkitevaluation.vo.TotalTextOrientation
import com.example.mlkitevaluation.vo.TotalTextResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Element
import com.google.mlkit.vision.text.Text.TextBlock
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import java.io.IOException
import java.io.InputStream
import java.util.Arrays

class PerformanceViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "PerformanceViewModel"

    private lateinit var assetManager: AssetManager

    private val _imagesFilesNames = MutableStateFlow<List<String>>(emptyList())
    val imagesFilesNames: StateFlow<List<String>> = _imagesFilesNames

    private val _totalNumberImages = MutableStateFlow(0)
    val totalNumberImages: StateFlow<Int> = _totalNumberImages

    private val _currentNumberImagesProcess = MutableStateFlow(0)
    val currentNumberImagesProcess: StateFlow<Int> = _currentNumberImagesProcess

    private val _totalTextCurvedResult =
        MutableStateFlow(TotalTextResult(TotalTextOrientation.CURVED))
    val totalTextCurvedResult = _totalTextCurvedResult
    private val _totalTextMultiOrientedResult =
        MutableStateFlow(TotalTextResult(TotalTextOrientation.MULTI_ORIENTED))
    val totalTextMultiOrientedResult = _totalTextMultiOrientedResult
    private val _totalTextHorizontalResult =
        MutableStateFlow(TotalTextResult(TotalTextOrientation.HORIZONTAL))
    val totalTextHorizontalResult = _totalTextHorizontalResult

    private val _imagesResultsByImagesNames: MutableStateFlow<MutableMap<String, MutableList<TotalTextImageResult>>> =
        MutableStateFlow(mutableMapOf())
    val imagesResultsByImagesNames = _imagesResultsByImagesNames

    init {
        Log.i("ViewModel", "Init view model")
        initViewModel()
        processTotalTextDataset()
    }

    private fun processTotalTextDataset() {
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

        _totalNumberImages.value = _imagesFilesNames.value.size
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
                        _currentNumberImagesProcess.value += 1
                        Log.i(
                            TAG,
                            "Détection du texte sur l'image $fileName réussi : \n${texts.text}"
                        )

                        val detectedElements: List<Element> = logTextDetectionInformationAndGetDetectedElements(texts)
                        val groundTruthList = loadGroundTruth(fileName.substringBeforeLast("."))

                        val matches = createMatchesFromElementAndGroundTruth(detectedElements, groundTruthList)

                        updateResultsFromMatches(matches, fileName)
                    }
                    .addOnFailureListener { e -> // Task failed with an exception
                        _currentNumberImagesProcess.value += 1
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

    private fun createMatchesFromElementAndGroundTruth(detectedElements: List<Element>, groundTruthList: List<TotalTextGroundTruth>): List<Pair<Element, TotalTextGroundTruth?>> {
        return detectedElements.map { detection ->
            val detectionPolygon = createPolygon(detection.cornerPoints!!)

            val bestMatch = groundTruthList.maxByOrNull { gt ->
                val groundTruthPolygon = createPolygon(
                    gt.x[0].zip(gt.y[0])
                        .map { (x, y) -> Point(x.toInt(), y.toInt()) }
                        .toTypedArray()
                )
                calculateIoU(detectionPolygon, groundTruthPolygon)
            }

            Pair(detection, bestMatch)
        }
    }

    private fun updateResultsFromMatches(matches: List<Pair<Element, TotalTextGroundTruth?>>, fileName: String) {
        matches.forEach {
            val elementText: String = it.first.text
            val gtMatch: TotalTextGroundTruth? = it.second
            if (gtMatch != null) {
                val transcription = gtMatch.transcription
                Log.i(TAG, "Match entre $elementText et $transcription")

                val charDistance: Int =
                    levenshteinDistance(elementText, transcription)
                val wordDistance: Int = if (charDistance == 0) 0 else 1

                val totalTextImageResult = TotalTextImageResult(
                    fileName,
                    transcription,
                    elementText,
                    charDistance,
                    wordDistance
                )
                _imagesResultsByImagesNames.value.putIfAbsent(fileName, mutableListOf())
                _imagesResultsByImagesNames.value[fileName]?.add(totalTextImageResult)

                when (gtMatch.orientation) {
                    TotalTextOrientation.CURVED -> {
                        _totalTextCurvedResult.value.updateTotalTextResult(
                            charDistance,
                            elementText.length,
                            wordDistance,
                            1
                        )
                    }

                    TotalTextOrientation.HORIZONTAL -> {
                        _totalTextHorizontalResult.value.updateTotalTextResult(
                            charDistance,
                            elementText.length,
                            wordDistance,
                            1
                        )
                    }

                    TotalTextOrientation.MULTI_ORIENTED -> {
                        _totalTextMultiOrientedResult.value.updateTotalTextResult(
                            charDistance,
                            elementText.length,
                            wordDistance,
                            1
                        )
                    }

                    TotalTextOrientation.OTHER -> Log.i(TAG, "# orientation")
                }
            } else {
                Log.i(TAG, "Aucun match pour $elementText")
            }
        }
    }

    private fun logTextDetectionInformationAndGetDetectedElements(texts: Text): List<Element> {
        val detectedElements: MutableList<Element> = mutableListOf()
        texts.textBlocks.forEach { textBlock ->
            Log.d(TAG, "TextBlock text is: " + textBlock.text)
            Log.d(TAG, "TextBlock boundingbox is: " + textBlock.boundingBox)
            Log.d(
                TAG,
                "TextBlock cornerpoint is: " + Arrays.toString(textBlock.cornerPoints)
            )
            textBlock.lines.forEach { line ->
                Log.d(TAG, "Line text is: " + line.text)
                Log.d(TAG, "Line boundingbox is: " + line.boundingBox)
                Log.d(
                    TAG,
                    "Line cornerpoint is: " + Arrays.toString(line.cornerPoints)
                )
                Log.d(TAG, "Line confidence is: " + line.confidence)
                Log.d(TAG, "Line angle is: " + line.angle)
                line.elements.forEach { element ->
                    detectedElements.add(element)
                    Log.d(TAG, "Element text is: " + element.text)
                    Log.d(TAG, "Element boundingbox is: " + element.boundingBox)
                    Log.d(
                        TAG,
                        "Element cornerpoint is: " + Arrays.toString(element.cornerPoints)
                    )
                    Log.d(TAG, "Element language is: " + element.recognizedLanguage)
                    Log.d(TAG, "Element confidence is: " + element.confidence)
                    Log.d(TAG, "Element angle is: " + element.angle)
                    for (symbol in element.symbols) {
                        Log.d(TAG, "Symbol text is: " + symbol.text)
                        Log.d(TAG, "Symbol boundingbox is: " + symbol.boundingBox)
                        Log.d(
                            TAG,
                            "Symbol cornerpoint is: " + Arrays.toString(symbol.cornerPoints)
                        )
                        Log.d(TAG, "Symbol confidence is: " + symbol.confidence)
                        Log.d(TAG, "Symbol angle is: " + symbol.angle)
                    }
                }
            }
        }

        return detectedElements
    }

    private fun createPolygon(points: Array<Point>): Polygon {
        val coordinates = points.map { Coordinate(it.x.toDouble(), it.y.toDouble()) }.toTypedArray()
        val geometryFactory = GeometryFactory()
        return geometryFactory.createPolygon(coordinates + coordinates[0]) // Close the polygon
    }

    private fun calculateIoU(poly1: Polygon, poly2: Polygon): Double {
        val intersection = poly1.intersection(poly2)
        val union = poly1.union(poly2)
        return if (union.area == 0.0) 0.0 else intersection.area / union.area
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        if (lhs == rhs) return 0

        val m = lhs.length
        val n = rhs.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,        // deletion
                    dp[i][j - 1] + 1,        // insertion
                    dp[i - 1][j - 1] + cost  // substitution
                )
            }
        }

        return dp[m][n]
    }

    private fun loadGroundTruth(imageName: String): List<TotalTextGroundTruth> {
        val groundTruthPath =
            "${DatasetConstants.TOTAL_TEXT_GROUND_TRUTH.folderName}/poly_gt_$imageName.txt"

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
                "m" -> TotalTextOrientation.MULTI_ORIENTED
                else -> TotalTextOrientation.OTHER
            }

            // On convertit les valeurs en entier
            val x = xValues.map { list ->
                list.filter { it.isNotEmpty() }.map { if (it.isEmpty()) 0f else it.toFloat() }
            }
            val y = yValues.map { list ->
                list.filter { it.isNotEmpty() }.map { if (it.isEmpty()) 0f else it.toFloat() }
            }

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