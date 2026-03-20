package com.example.neighborhoodreports.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhoodreports.data.repository.CategoriesRepository
import com.example.neighborhoodreports.data.repository.ReportsRepository
import com.example.neighborhoodreports.data.repository.StorageRepository
import com.example.neighborhoodreports.data.repository.LocationRepository
import com.example.neighborhoodreports.model.Category
import com.example.neighborhoodreports.model.Report
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewReportUiState(
        val title: String = "",
        val description: String = "",
        val selectedCategoryId: String? = null,
        val imageUri: Uri? = null,
        val originalImageUrl: String? = null,
        val editingReportId: String? = null,
        val categories: List<Category> = emptyList(),
        val latitude: Double? = null,
        val longitude: Double? = null,
        val address: String? = null,
        val loading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
)

/**
 * מנהל את מסך יצירת/עריכת דיווח. אחראי לאמת את הקלט (כותרת, תיאור, תמונה וקטגוריה) ולהעלות את
 * הדיווח, כולל קידוד התמונה ל-base64 במידת הצורך.
 */
class NewReportViewModel(application: Application) : AndroidViewModel(application) {

    private val reportsRepo = ReportsRepository()
    private val categoriesRepo = CategoriesRepository()
    private val storageRepo = StorageRepository(application)
    private val locationRepo = LocationRepository()

    private val _uiState = MutableStateFlow(NewReportUiState())
    val uiState: StateFlow<NewReportUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        categoriesRepo.getCategories { list -> _uiState.update { it.copy(categories = list) } }
    }

    fun loadReportForEdit(id: String) {
        _uiState.update { it.copy(loading = true, error = null) }
        reportsRepo.getReportById(id) { report ->
            if (report != null) {
                _uiState.update { state ->
                    state.copy(
                            title = report.title,
                            description = report.description,
                            selectedCategoryId = report.categoryId,
                            originalImageUrl = report.imageUrl,
                            editingReportId = report.id,
                            latitude = if (report.latitude != 0.0) report.latitude else null,
                            longitude = if (report.longitude != 0.0) report.longitude else null,
                            address = report.address,
                            loading = false
                    )
                }
            } else {
                _uiState.update { it.copy(loading = false, error = "שגיאה בטעינת הדיווח") }
            }
        }
    }

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun updateSelectedCategory(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateImageUri(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun updateLocation(lat: Double, lng: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lng, address = "מחפש כתובת...") }
        viewModelScope.launch {
            val address = locationRepo.getAddressFromLocation(lat, lng)
            _uiState.update { it.copy(address = address ?: "כתובת לא ידועה") }
        }
    }

    fun addReport() {
        val currentState = _uiState.value

        if (currentState.title.isBlank() ||
                        currentState.description.isBlank() ||
                        currentState.selectedCategoryId == null
        ) {
            _uiState.update { it.copy(error = "נא למלא את כל השדות") }
            return
        }

        _uiState.update { it.copy(loading = true, error = null) }

        viewModelScope.launch {
            var imageUrl = currentState.originalImageUrl ?: ""

            // Encode the selected image to base64 if present
            if (currentState.imageUri != null) {
                val base64 = storageRepo.uploadImage(currentState.imageUri, "")
                if (base64 != null) {
                    imageUrl = base64
                } else {
                    _uiState.update { it.copy(loading = false, error = "שגיאה בקידוד התמונה") }
                    return@launch
                }
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if (currentState.editingReportId != null) {
                // UPDATE flow
                reportsRepo.getReportById(currentState.editingReportId) { existingReport ->
                    if (existingReport != null) {
                        val updatedReport =
                                existingReport.copy(
                                        title = currentState.title,
                                        description = currentState.description,
                                        categoryId = currentState.selectedCategoryId,
                                        imageUrl = imageUrl,
                                        latitude = currentState.latitude ?: 0.0,
                                        longitude = currentState.longitude ?: 0.0,
                                        address = currentState.address ?: ""
                                )
                        reportsRepo.updateReport(updatedReport) { ok ->
                            _uiState.update { state ->
                                state.copy(
                                        loading = false,
                                        success = ok,
                                        error = if (!ok) "שגיאה בשמירת השינויים" else null
                                )
                            }
                        }
                    } else {
                        _uiState.update { it.copy(loading = false, error = "הדיווח לא נמצא") }
                    }
                }
            } else {
                // ADD flow
                val report =
                        Report(
                                id = "",
                                title = currentState.title,
                                description = currentState.description,
                                categoryId = currentState.selectedCategoryId,
                                imageUrl = imageUrl,
                                status = "pending",
                                userId = uid,
                                latitude = currentState.latitude ?: 0.0,
                                longitude = currentState.longitude ?: 0.0,
                                address = currentState.address ?: "",
                                createdAt = Timestamp.now()
                        )

                reportsRepo.addReport(report) { ok ->
                    _uiState.update { state ->
                        state.copy(
                                loading = false,
                                success = ok,
                                error = if (!ok) "שגיאה בשליחת הדיווח" else null
                        )
                    }
                }
            }
        }
    }
}
