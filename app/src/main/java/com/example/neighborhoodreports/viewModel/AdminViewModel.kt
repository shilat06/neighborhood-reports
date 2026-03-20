package com.example.neighborhoodreports.viewModel

import androidx.lifecycle.ViewModel
import com.example.neighborhoodreports.data.repository.CategoriesRepository
import com.example.neighborhoodreports.data.repository.ReportsRepository
import com.example.neighborhoodreports.model.Category
import com.example.neighborhoodreports.model.Report
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val PAGE_SIZE_REPORTS = 10L
private const val PAGE_SIZE_CATEGORIES = 15L

data class AdminUiState(
        val categories: List<Category> = emptyList(),
        val pendingReports: List<Report> = emptyList(),
        val isLoadingReports: Boolean = false,
        val isLoadingMoreReports: Boolean = false,
        val isLoadingMoreCategories: Boolean = false,
        val hasMoreReports: Boolean = true,
        val hasMoreCategories: Boolean = true,
)

/**
 * ה-ViewModel הראשי של מסך הניהול (אדמין).
 * אחראי לטעינת רשימת הדיווחים הממתינים לאישור והקטגוריות השונות (עם תמיכה בעימוד/Pagination),
 * ולביצוע פעולות ניהוליות כמו מחיקת/הוספת קטגוריות ואישור/דחיית דיווחים.
 */
class AdminViewModel(
        private val categoriesRepo: CategoriesRepository = CategoriesRepository(),
        private val reportsRepo: ReportsRepository = ReportsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    // Cursors for next-page fetches
    private var lastReportDoc: DocumentSnapshot? = null
    private var lastCategoryDoc: DocumentSnapshot? = null

    init {
        loadMorePendingReports()
        loadMoreCategories()
    }

    // ── דיווחים ממתינים לאישור (Pending Reports) ──────────────────────────────────────────
    fun loadMorePendingReports() {
        val state = _uiState.value
        if (state.isLoadingReports || state.isLoadingMoreReports || !state.hasMoreReports) return

        val isFirstPage = lastReportDoc == null
        android.util.Log.d("AdminViewModel", "loadMorePendingReports called. isFirstPage=$isFirstPage")
        _uiState.update {
            if (isFirstPage) it.copy(isLoadingReports = true)
            else it.copy(isLoadingMoreReports = true)
        }

        reportsRepo.getPendingReportsPaged(PAGE_SIZE_REPORTS, lastReportDoc) { newItems, cursor ->
            android.util.Log.d("AdminViewModel", "loadMorePendingReports callback: ${newItems.size} items")
            lastReportDoc = cursor
            _uiState.update { current ->
                current.copy(
                        pendingReports = if (isFirstPage) newItems else current.pendingReports + newItems,
                        isLoadingReports = false,
                        isLoadingMoreReports = false,
                        hasMoreReports = newItems.size.toLong() == PAGE_SIZE_REPORTS
                )
            }
        }
    }

    /** מנקה את רשימת הדיווחים הממתינים וטוען מחדש מהדף הראשון (יש לקרוא פונקציה זו אחרי אישור/דחייה). */
    fun refreshPendingReports() {
        lastReportDoc = null
        _uiState.update { it.copy(pendingReports = emptyList(), hasMoreReports = true) }
        loadMorePendingReports()
    }

    // ── קטגוריות (Categories) ───────────────────────────────────────────────
    fun loadMoreCategories() {
        val state = _uiState.value
        if (state.isLoadingMoreCategories || !state.hasMoreCategories) return

        val isFirstPage = lastCategoryDoc == null
        android.util.Log.d("AdminViewModel", "loadMoreCategories called. isFirstPage=$isFirstPage")
        _uiState.update { it.copy(isLoadingMoreCategories = true) }

        categoriesRepo.getCategoriesPaged(PAGE_SIZE_CATEGORIES, lastCategoryDoc) { newItems, cursor ->
            android.util.Log.d("AdminViewModel", "loadMoreCategories callback: ${newItems.size} items")
            lastCategoryDoc = cursor
            _uiState.update { current ->
                current.copy(
                        categories = if (isFirstPage) newItems else current.categories + newItems,
                        isLoadingMoreCategories = false,
                        hasMoreCategories = newItems.size.toLong() == PAGE_SIZE_CATEGORIES
                )
            }
        }
    }

    /** מנקה את רשימת הקטגוריות וטוען מחדש מהדף הראשון (מופעל אחרי הוספה/מחיקה). */
    private fun refreshCategories() {
        lastCategoryDoc = null
        _uiState.update { it.copy(categories = emptyList(), hasMoreCategories = true) }
        loadMoreCategories()
    }

    // ── Mutations ────────────────────────────────────────────────
    fun addCategory(name: String) {
        categoriesRepo.addCategory(name) { success -> if (success) refreshCategories() }
    }

    fun deleteCategory(id: String) {
        categoriesRepo.deleteCategory(id) { success -> if (success) refreshCategories() }
    }

    fun approveReport(id: String) {
        reportsRepo.updateStatus(id, "approved") { refreshPendingReports() }
    }

    fun rejectReport(id: String) {
        reportsRepo.updateStatus(id, "rejected") { refreshPendingReports() }
    }
}
