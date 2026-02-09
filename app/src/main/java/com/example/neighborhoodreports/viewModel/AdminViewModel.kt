package com.example.neighborhoodreports.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.neighborhoodreports.data.repository.CategoriesRepository
import com.example.neighborhoodreports.data.repository.ReportsRepository
import com.example.neighborhoodreports.model.Category
import com.example.neighborhoodreports.model.Report
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class AdminViewModel(
    private val categoriesRepo: CategoriesRepository = CategoriesRepository(),
    private val reportsRepo: ReportsRepository = ReportsRepository()
) : ViewModel() {

    // רשימת קטגוריות
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    // דיווחים שממתינים לאישור
    var pendingReports by mutableStateOf<List<Report>>(emptyList())
        private set

    // טעינה
    var isLoading by mutableStateOf(true)
        private set

    init {
        loadCategories()
        loadPendingReports()
    }

    // טעינת קטגוריות בזמן אמת
    private fun loadCategories() {
        categoriesRepo.getCategories { list ->
            categories = list
        }
    }

    // טעינת דיווחים שממתינים לאישור
    private fun loadPendingReports() {
        reportsRepo.getPendingReports { list ->
            pendingReports = list
            isLoading = false
        }
    }

    // הוספת קטגוריה
    fun addCategory(name: String) {
        categoriesRepo.addCategory(name) { success ->
            // אפשר להוסיף הודעה למשתמש אם תרצי
        }
    }

    // מחיקת קטגוריה
    fun deleteCategory(id: String) {
        categoriesRepo.deleteCategory(id) { success ->
            // אפשר להוסיף הודעה למשתמש אם תרצי
        }
    }

    // אישור דיווח
    fun approveReport(id: String) {
        reportsRepo.updateStatus(id, "approved") { }
    }

    // דחיית דיווח
    fun rejectReport(id: String) {
        reportsRepo.updateStatus(id, "rejected") { }
    }
}
