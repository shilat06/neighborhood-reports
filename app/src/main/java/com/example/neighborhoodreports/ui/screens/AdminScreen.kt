package com.example.neighborhoodreports.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.neighborhoodreports.viewModel.AdminViewModel

@Composable
fun AdminScreen(viewModel: AdminViewModel = AdminViewModel()) {

    val categories = viewModel.categories
    val pendingReports = viewModel.pendingReports
    val loading = viewModel.isLoading

    var newCategory by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // כותרת ראשית
        Text(
            text = "מסך ניהול",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        // -----------------------------
        // ניהול קטגוריות
        // -----------------------------
        Text(
            text = "ניהול קטגוריות",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newCategory,
                onValueChange = { newCategory = it },
                modifier = Modifier.weight(1f),
                label = { Text("שם קטגוריה") }
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    if (newCategory.isNotBlank()) {
                        viewModel.addCategory(newCategory)
                        newCategory = ""
                    }
                }
            ) {
                Text("הוסף")
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(categories) { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(cat.name)
                    TextButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                        Text("מחק")
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // -----------------------------
        // דיווחים ממתינים לאישור
        // -----------------------------
        Text(
            text = "דיווחים ממתינים לאישור",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(pendingReports) { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // כותרת הדיווח
                            Text(
                                text = report.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(Modifier.height(4.dp))

                            // תיאור קצר
                            Text(
                                text = report.description,
                                maxLines = 3
                            )

                            Spacer(Modifier.height(12.dp))

                            // כפתורי אישור/דחייה
                            Row {
                                Button(
                                    onClick = { viewModel.approveReport(report.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("אשר")
                                }

                                Spacer(Modifier.width(8.dp))

                                Button(
                                    onClick = { viewModel.rejectReport(report.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("דחה")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
