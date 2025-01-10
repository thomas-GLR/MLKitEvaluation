package com.example.mlkitevaluation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun PerformanceEvaluationScreen(
    navController: NavController,
    viewModel: PerformanceViewModel = viewModel(factory = PerformanceViewModel.Factory)
) {
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
            Text("Performance")
            LazyColumn {
                items(viewModel.imagesFilesNames.value) {
                    Text(text = it)
                }
            }
        }
    }
}