package com.example.mlkitevaluation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

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
        if (currentNumberImagesProcess < totalNumberImages) {

        } else {
            Column(modifier = Modifier.padding(innerPadding)) {

            }
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
