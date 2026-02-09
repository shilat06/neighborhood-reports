package com.example.neighborhoodreports.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neighborhoodreports.viewModel.NewReportViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun NewReportScreen(
    viewModel: NewReportViewModel = viewModel(),
    onReportAdded: () -> Unit // ניווט חזרה למסך הבית אחרי הוספה
) {
    val categories = viewModel.categories
    val loading = viewModel.loading
    val success = viewModel.success
    val error = viewModel.error

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("הוספת דיווח חדש", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(20.dp))

        // כותרת
        TextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("כותרת") }
        )

        Spacer(Modifier.height(12.dp))

        // תיאור
        TextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("תיאור") },
            minLines = 3
        )

        Spacer(Modifier.height(12.dp))

        // בחירת קטגוריה
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                selectedCategoryId?.let { id ->
                    categories.find { it.id == id }?.name ?: "בחר קטגוריה"
                } ?: "בחר קטגוריה"
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.name) },
                    onClick = {
                        selectedCategoryId = cat.id
                        expanded = false
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // כפתור שליחה
        Button(
            onClick = {
                viewModel.addReport(
                    title = title,
                    description = description,
                    categoryId = selectedCategoryId
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("שלח דיווח")
        }

        Spacer(Modifier.height(20.dp))

        // טעינה
        if (loading) {
            CircularProgressIndicator()
        }

        // הצלחה
        if (success) {
            Text("הדיווח נשלח בהצלחה!")
            onReportAdded()
        }

        // שגיאה
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
