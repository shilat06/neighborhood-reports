package com.example.neighborhoodreports.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neighborhoodreports.ui.components.ReportCard
import com.example.neighborhoodreports.viewModel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController, viewModel: HomeViewModel = viewModel()
) {
    val reports = viewModel.reports
    val categories = viewModel.categories
    val loading = viewModel.isLoading

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("דיווחי שכונה", style = MaterialTheme.typography.titleLarge) })
        }) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {

            Spacer(Modifier.height(16.dp))

            // שדה חיפוש מעוצב
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = {
                    viewModel.searchQuery = it
                    viewModel.applyFilters()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("חיפוש דיווחים") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // בחירת קטגוריה
            var expanded by rememberSaveable { mutableStateOf(false) }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = viewModel.selectedCategoryId?.let { id ->
                        categories.find { it.id == id }?.name ?: "בחר קטגוריה"
                    } ?: "בחר קטגוריה")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "הצג הכול",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                        },

                        onClick = {
                            viewModel.selectedCategoryId = null
                            viewModel.applyFilters()
                            expanded = false
                        })

                    categories.forEach { cat ->
                        DropdownMenuItem(text = {
                            Text(
                                cat.name, modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                        }, onClick = {
                            viewModel.selectedCategoryId = cat.id
                            viewModel.applyFilters()
                            expanded = false
                        })
                    }
                }
            }


            Spacer(Modifier.height(12.dp))

            // כפתור יצירת דיווח חדש
            Button(
                onClick = { navController.navigate("newReport") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("יצירת דיווח חדש", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            // כפתור מיון
            OutlinedButton(
                onClick = {
                    viewModel.sortDescending = !viewModel.sortDescending
                    viewModel.applyFilters()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    if (viewModel.sortDescending) "מיין מהחדש לישן"
                    else "מיין מהישן לחדש"
                )
            }

            Spacer(Modifier.height(16.dp))

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text("אין דיווחים להצגה", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    ReportCard(report = report) {
                        navController.navigate("details/${report.id}")
                    }
                }
            }
        }
    }
}
