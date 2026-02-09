package com.example.neighborhoodreports.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neighborhoodreports.viewModel.ReportDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailsScreen(
    navController: NavController,
    reportId: String,
    viewModel: ReportDetailsViewModel = viewModel ()
) {
    // שליפת הדיווח לפי ID
    val report = viewModel.report
    val loading = viewModel.loading
    val error = viewModel.error

    LaunchedEffect (reportId) {
        viewModel.loadReport(reportId)
    }

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("פרטי דיווח") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "חזרה")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // מצב טעינה
            if (loading) {
                CircularProgressIndicator()
                return@Column
            }

            // שגיאה
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                return@Column
            }

            // אם אין דיווח (לא נמצא)
            if (report == null) {
                Text("הדיווח לא נמצא")
                return@Column
            }

            // כותרת הדיווח
            Text(
                text = report.title,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(12.dp))

            // קטגוריה כ־Chip
            AssistChip(
                onClick = {},
                label = { Text(report.categoryId) }
            )

            Spacer(Modifier.height(20.dp))

            // תיאור מלא
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(20.dp))

            // תאריך יצירה
            Text(
                text = "נוצר בתאריך: ${report.createdAt?.toDate().toString()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
