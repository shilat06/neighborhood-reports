package com.example.neighborhoodreports.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhoodreports.data.repository.CategoriesRepository
import com.example.neighborhoodreports.data.repository.ReportsRepository
import com.example.neighborhoodreports.data.repository.UserRepository
import com.example.neighborhoodreports.model.Category
import com.example.neighborhoodreports.model.Report
import com.example.neighborhoodreports.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.google.firebase.firestore.DocumentSnapshot

private const val PAGE_SIZE = 3L

data class HomeUiState(
        val reports: List<Report> = emptyList(),
        val categories: List<Category> = emptyList(),
        val userMap: Map<String, User> = emptyMap(),
        val searchQuery: String = "",
        val selectedCategoryId: String? = null,
        val sortDescending: Boolean = true,
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true
)

/**
 * מנהל את המצב (State) של מסך הבית.
 * מטפל בטעינת הקטגוריות והדיווחים המאושרים (עם Pagination), הפעלת סינונים מקומיים (חיפוש מקומי),
 * והבאת פרטי המשתמשים שחיברו את הדיווחים.
 */
class HomeViewModel(
        private val reportsRepo: ReportsRepository = ReportsRepository(),
        private val categoriesRepo: CategoriesRepository = CategoriesRepository(),
        private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private var loadedReports: List<Report> = emptyList()
    private var lastReportDoc: DocumentSnapshot? = null

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadMoreReports()
    }

    private fun loadCategories() {
        categoriesRepo.getCategories { list ->
            _uiState.update { it.copy(categories = list) }
        }
    }

    fun loadMoreReports() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return

        val isFirstPage = lastReportDoc == null
        _uiState.update {
            if (isFirstPage) it.copy(isLoading = true) else it.copy(isLoadingMore = true)
        }

        reportsRepo.getApprovedReportsPaged(
                pageSize = PAGE_SIZE,
                categoryId = state.selectedCategoryId,
                sortDescending = state.sortDescending,
                lastDoc = lastReportDoc
        ) { newItems, cursor ->
            lastReportDoc = cursor
            loadedReports = if (isFirstPage) newItems else loadedReports + newItems

            fetchAuthors(newItems.map { it.userId }.distinct())

            _uiState.update {
                it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = newItems.size.toLong() == PAGE_SIZE
                )
            }
            applyLocalFilters()
        }
    }

    fun refreshReports() {
        lastReportDoc = null
        loadedReports = emptyList()
        _uiState.update { it.copy(reports = emptyList(), hasMore = true) }
        loadMoreReports()
    }

    private fun fetchAuthors(userIds: List<String>) {
        if (userIds.isEmpty()) return
        viewModelScope.launch {
            val map = _uiState.value.userMap.toMutableMap()
            userIds.forEach { uid ->
                if (uid.isNotBlank() && !map.containsKey(uid)) {
                    val user = userRepo.getUser(uid)
                    if (user != null) map[uid] = user
                }
            }
            _uiState.update { it.copy(userMap = map) }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyLocalFilters() // Search is done locally on loaded pages
    }

    fun updateSelectedCategory(categoryId: String?) {
        if (_uiState.value.selectedCategoryId == categoryId) return
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        refreshReports() // Requires new server fetch
    }

    fun toggleSort() {
        _uiState.update { it.copy(sortDescending = !it.sortDescending) }
        refreshReports() // Requires new server fetch
    }

    private fun applyLocalFilters() {
        val state = _uiState.value
        var filtered = loadedReports

        if (state.searchQuery.isNotBlank()) {
            filtered =
                    filtered.filter {
                        it.title.contains(state.searchQuery, ignoreCase = true) ||
                                it.description.contains(state.searchQuery, ignoreCase = true)
                    }
        }

        _uiState.update { it.copy(reports = filtered) }
    }
}
