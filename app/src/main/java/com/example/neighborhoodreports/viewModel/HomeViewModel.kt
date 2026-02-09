package com.example.neighborhoodreports.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.neighborhoodreports.data.repository.CategoriesRepository
import com.example.neighborhoodreports.data.repository.ReportsRepository
import com.example.neighborhoodreports.model.Report
import com.example.neighborhoodreports.model.Category
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class HomeViewModel(
    private val reportsRepo: ReportsRepository = ReportsRepository(),     // ריפוזיטורי לדיווחים
    private val categoriesRepo: CategoriesRepository = CategoriesRepository() // ריפוזיטורי לקטגוריות
) : ViewModel() {

    private var allReports: List<Report> = emptyList() // כל הדיווחים מהשרת (לפני סינון)

    var reports by mutableStateOf<List<Report>>(emptyList()) // הדיווחים אחרי סינון
        private set

    var categories by mutableStateOf<List<Category>>(emptyList()) // רשימת קטגוריות
        private set

    var searchQuery by mutableStateOf("")            // טקסט חיפוש
    var selectedCategoryId by mutableStateOf<String?>(null) // קטגוריה נבחרת
    var sortDescending by mutableStateOf(true)       // כיוון מיון

    // שני מצבי טעינה נפרדים
    private var categoriesLoading = true
    private var reportsLoading = true

    // טעינה משולבת — true רק אם אחד מהם עדיין טוען
    val isLoading: Boolean
        get() = categoriesLoading || reportsLoading

    init {
        loadCategories() // טעינת קטגוריות
        loadReports()    // טעינת דיווחים
    }

    private fun loadCategories() {
        categoriesRepo.getCategories { list ->
            categories = list
            categoriesLoading = false // סיום טעינת קטגוריות
        }
    }

    private fun loadReports() {
        reportsRepo.getApprovedReports { list ->
            allReports = list
            applyFilters()            // הפעלת סינון על הנתונים
            reportsLoading = false    // סיום טעינת דיווחים
        }
    }

    // הפעלת כל הפילטרים: חיפוש, קטגוריה, מיון
    fun applyFilters() {
        var filtered = allReports

        // סינון לפי טקסט
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        // סינון לפי קטגוריה
        selectedCategoryId?.let { catId ->
            filtered = filtered.filter { it.categoryId == catId }
        }

        // מיון לפי תאריך
        filtered = if (sortDescending) {
            filtered.sortedByDescending { it.createdAt }
        } else {
            filtered.sortedBy { it.createdAt }
        }

        reports = filtered // עדכון הרשימה המוצגת
    }
}
