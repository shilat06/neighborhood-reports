package com.example.neighborhoodreports.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.neighborhoodreports.data.repository.ReportsRepository
import com.example.neighborhoodreports.model.Report
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class ReportDetailsViewModel(
    private val reportsRepo: ReportsRepository = ReportsRepository()
) : ViewModel() {

    var report by mutableStateOf<Report?>(null)
        private set

    var loading by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // שליפת דיווח לפי ID
    fun loadReport(id: String) {
        loading = true
        error = null

        reportsRepo.getReportById(id) { result ->
            loading = false
            if (result == null) {
                error = "שגיאה בטעינת הדיווח"
            } else {
                report = result
            }
        }
    }
}
