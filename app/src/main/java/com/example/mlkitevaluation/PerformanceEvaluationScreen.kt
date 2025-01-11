package com.example.mlkitevaluation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun PerformanceEvaluationScreen(
    navController: NavController,
    viewModel: PerformanceViewModel = viewModel(factory = PerformanceViewModel.Factory)
) {
    val textBlocks by viewModel.textBlocks.collectAsState()
    val groundTruths by viewModel.groundTruths.collectAsState()

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
        Box(modifier = Modifier.padding(innerPadding)) {
            for (textBlock in textBlocks) {
                for (line in textBlock.lines) {
                    if (line.text == "PELA 4JAR") {
                        val points: MutableList<Pair<Float, Float>> = mutableListOf()
                        points.add(Pair(230f, 402f))
                        points.add(Pair(292f, 430f))
                        points.add(Pair(358f, 395f))
                        points.add(Pair(384f, 419f))
                        points.add(Pair(393f, 463f))
                        points.add(Pair(211f, 425f))
                        CustomPolygon(points, Color.Red)
                    }
                    for (element in line.elements) {
                        val points: MutableList<Pair<Float, Float>> = mutableListOf();
                        element.cornerPoints?.forEach {
                            points.add(Pair(it.x.toFloat(), it.y.toFloat()))
                        }
                        CustomPolygon(points)
                    }
                }
            }
            for (groundTruth in groundTruths) {
                val points: MutableList<Pair<Float, Float>> = mutableListOf()
                for (i in groundTruth.x[0].indices) {
                    points.add(Pair(groundTruth.x[0][i], groundTruth.y[0][i]))
                }
                CustomPolygon(points, Color.Red)
            }
        }

//        Column(modifier = Modifier.padding(innerPadding)) {
//            Text("Performance")
//            LazyColumn {
//                items(viewModel.imagesFilesNames.value) {
//                    Text(text = it)
//                }
//            }
//        }
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
