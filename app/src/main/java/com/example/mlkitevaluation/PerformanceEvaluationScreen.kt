package com.example.mlkitevaluation

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mlkitevaluation.vo.TotalTextResult

@Composable
fun PerformanceEvaluationScreen(
    navController: NavController,
    viewModel: PerformanceViewModel = viewModel(factory = PerformanceViewModel.Factory)
) {
    val totalNumberImages by viewModel.totalNumberImages.collectAsState()
    val currentNumberImagesProcess by viewModel.currentNumberImagesProcess.collectAsState()
    val totalTextHorizontalResult by viewModel.totalTextHorizontalResult.collectAsState()
    val totalTextCurvedResult by viewModel.totalTextCurvedResult.collectAsState()
    val totalTextMultiOrientedResult by viewModel.totalTextMultiOrientedResult.collectAsState()
    val imagesResultsByImagesNames by viewModel.imagesResultsByImagesNames.collectAsState()

    val TAG = "PerformanceEvaluationScreen"

    Scaffold(
        topBar = {
            TopBar(
                title = "Evaluation",
                canNavigateBack = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                "Dataset : Total-text",
                modifier = Modifier.size(32.dp)
            )
            Button(onClick = {
                Log.i(TAG, "Orientation : ${totalTextHorizontalResult.orientation}")
                Log.i(TAG, "Nombre de caractère en erreur : ${totalTextHorizontalResult.charDistanceInError}")
                Log.i(TAG, "Caractère totale : ${totalTextHorizontalResult.totalChars}")
                Log.i(TAG, "Nombre de mot en erreur : ${totalTextHorizontalResult.wordDistanceInError}")
                Log.i(TAG, "Mot totale : ${totalTextHorizontalResult.totalWords}")

                Log.i(TAG, "Orientation : ${totalTextMultiOrientedResult.orientation}")
                Log.i(TAG, "Nombre de caractère en erreur : ${totalTextMultiOrientedResult.charDistanceInError}")
                Log.i(TAG, "Caractère totale : ${totalTextMultiOrientedResult.totalChars}")
                Log.i(TAG, "Nombre de mot en erreur : ${totalTextMultiOrientedResult.wordDistanceInError}")
                Log.i(TAG, "Mot totale : ${totalTextMultiOrientedResult.totalWords}")

                Log.i(TAG, "Orientation : ${totalTextCurvedResult.orientation}")
                Log.i(TAG, "Nombre de caractère en erreur : ${totalTextCurvedResult.charDistanceInError}")
                Log.i(TAG, "Caractère totale : ${totalTextCurvedResult.totalChars}")
                Log.i(TAG, "Nombre de mot en erreur : ${totalTextCurvedResult.wordDistanceInError}")
                Log.i(TAG, "Mot totale : ${totalTextCurvedResult.totalWords}")
            }) {
                Text("Update")
            }
            if (totalNumberImages == 0) {
                Text("Chargement des fichiers du dataset...")
            } else {
                Text("$currentNumberImagesProcess / $totalNumberImages")
                Column {
                    ShowResult(totalTextHorizontalResult)
                    ShowResult(totalTextMultiOrientedResult)
                    ShowResult(totalTextCurvedResult)
                }
//                if (currentNumberImagesProcess < totalNumberImages) {
//                    AnimatedCircularProgressBar(currentNumberImagesProcess.toFloat() / totalNumberImages)
//                } else {
//                    Column {
//                        ShowResult(totalTextHorizontalResult)
//                        ShowResult(totalTextMultiOrientedResult)
//                        ShowResult(totalTextCurvedResult)
//                    }
//                }
            }
        }
    }
}

@Composable
fun ShowResult(totalTextResult: TotalTextResult) {
    Box(modifier = Modifier.padding(10.dp)) {
        Column {
            Text("Orientation : ${totalTextResult.orientation.orientationValue}")
            Text("Nombre de caractère en erreur : ${totalTextResult.charDistanceInError}")
            Text("Caractère totale : ${totalTextResult.totalChars}")
            Text("Nombre de mot en erreur : ${totalTextResult.wordDistanceInError}")
            Text("Mot totale : ${totalTextResult.totalWords}")
        }
    }
}

@Composable
fun CustomPolygon(
    points: List<Pair<Float, Float>>,
    color: Color = Color.Blue,
    strokeWidth: Float = 2f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        if (points.isNotEmpty()) {
            path.moveTo(points[0].first, points[0].second)
            for (i in 1 until points.size) {
                path.lineTo(points[i].first, points[i].second)
            }
            path.close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun AnimatedCircularProgressBar(targetProgress: Float) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(targetProgress) {
        progress.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    CircularProgressIndicator(
        progress = { progress.value },
        modifier = Modifier.size(64.dp),
        color = Color.Blue,
    )
}

