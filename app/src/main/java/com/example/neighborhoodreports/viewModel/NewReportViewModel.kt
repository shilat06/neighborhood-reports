package com.example.neighborhoodreports.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.neighborhoodreports.data.repository.CategoriesRepository
import com.example.neighborhoodreports.data.repository.ReportsRepository
import com.example.neighborhoodreports.model.Category
import com.example.neighborhoodreports.model.Report
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.Timestamp

class NewReportViewModel(
    private val reportsRepo: ReportsRepository = ReportsRepository(),
    private val categoriesRepo: CategoriesRepository = CategoriesRepository()
) : ViewModel() {

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var success by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadCategories()
    }

    private fun loadCategories() {
        categoriesRepo.getCategories { list ->
            categories = list
        }
    }

    fun addReport(title: String, description: String, categoryId: String?) {
        if (title.isBlank() || description.isBlank() || categoryId == null) {
            error = "נא למלא את כל השדות"
            return
        }

        loading = true
        error = null

        val report = Report(
            id = "",
            title = title,
            description = description,
            categoryId = categoryId,
            status = "pending",
            createdAt = Timestamp.now()
        )

        reportsRepo.addReport(report) { ok ->
            loading = false
            success = ok
            if (!ok) error = "שגיאה בשליחת הדיווח"
        }
    }
}
