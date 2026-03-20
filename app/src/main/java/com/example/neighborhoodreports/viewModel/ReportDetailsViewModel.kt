package com.example.neighborhoodreports.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhoodreports.data.repository.CategoriesRepository
import com.example.neighborhoodreports.data.repository.NotificationRepository
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

import com.example.neighborhoodreports.data.repository.CommentsRepository
import com.example.neighborhoodreports.data.repository.AuthRepository
import com.example.neighborhoodreports.model.Comment
import com.google.firebase.firestore.ListenerRegistration
import android.util.Log

data class ReportDetailsUiState(
        val report: Report? = null,
        val author: User? = null,
        val category: Category? = null,
        val comments: List<Pair<Comment, User?>> = emptyList(),
        val allUsers: List<User> = emptyList(),
        val loading: Boolean = true,
        val error: String? = null,
        val deleted: Boolean = false,
        val currentUserId: String? = null
)

/**
 * מנהל את מסך פרטי הדיווח.
 * טוען את פרטי הדיווח המלאים, נתוני המשתמש שדיווח (Author), והקטגוריה.
 * תומך גם במחיקת דיווח (למשתמשים מורשים).
 */
class ReportDetailsViewModel(
        application: Application,
        private val reportsRepo: ReportsRepository = ReportsRepository(),
        private val userRepo: UserRepository = UserRepository(),
        private val categoriesRepo: CategoriesRepository = CategoriesRepository(),
        private val commentsRepo: CommentsRepository = CommentsRepository(),
        private val authRepo: AuthRepository = AuthRepository()
) : AndroidViewModel(application) {

    private val notificationRepo = NotificationRepository(application)

    private val _uiState = MutableStateFlow(ReportDetailsUiState())
    val uiState: StateFlow<ReportDetailsUiState> = _uiState.asStateFlow()

    // שמירת הרפרנס למאזין כדי שנוכל לבטל אותו כשהסקרין נסגר
    private var commentsListener: ListenerRegistration? = null

    fun loadReport(id: String) {
        _uiState.update { it.copy(loading = true, error = null) }

        reportsRepo.getReportById(id) { result ->
            if (result == null) {
                _uiState.update { it.copy(loading = false, error = "שגיאה בטעינת הדיווח") }
            } else {
                _uiState.update { it.copy(loading = false, report = result) }
                // Fetch the author profile in the background
                if (result.userId.isNotBlank()) {
                    viewModelScope.launch {
                        val user = userRepo.getUser(result.userId)
                        _uiState.update { it.copy(author = user) }
                    }
                }
                // Fetch the category in the background
                if (result.categoryId.isNotBlank()) {
                    categoriesRepo.getCategoryById(result.categoryId) { categoryObj ->
                        _uiState.update { it.copy(category = categoryObj) }
                    }
                }

                // Fetch all users for tagging
                viewModelScope.launch {
                    val users = userRepo.getAllUsers()
                    _uiState.update { it.copy(allUsers = users) }
                }

                // Start listening to comments in real-time
                listenToComments(result.id)
            }
        }
    }

    private fun listenToComments(reportId: String) {
        // ביטול מאזין קודם אם קיים (למשל כשנטען דיווח אחר)
        commentsListener?.remove()
        commentsListener = commentsRepo.getCommentsForReport(reportId) { commentsList ->
            viewModelScope.launch {
                // Fetch user profile for each commenter to show name/avatar
                val commentsWithUsers = commentsList.map { comment ->
                    val user = if (comment.userId.isNotBlank()) {
                        userRepo.getUser(comment.userId)
                    } else null
                    Pair(comment, user)
                }
                _uiState.update { it.copy(comments = commentsWithUsers) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ביטול ההרשמה לעדכונים בזמן-אמת כשה-ViewModel מתאפס (ניווט לאחור)
        commentsListener?.remove()
    }

    fun deleteReport(id: String) {
        _uiState.update { it.copy(loading = true, error = null) }
        reportsRepo.deleteReport(id) { success ->
            if (success) {
                _uiState.update { it.copy(loading = false, deleted = true) }
            } else {
                _uiState.update { it.copy(loading = false, error = "שגיאה במחיקת הדיווח") }
            }
        }
    }

    fun addComment(text: String, reportId: String) {
        val uid = authRepo.currentUserId() ?: return
        val comment = Comment(userId = uid, text = text)
        commentsRepo.addComment(reportId, comment) { success ->
            if (success) {
                // Send push notification in the background
                viewModelScope.launch {
                    val currentState = _uiState.value
                    val currentUserName = currentState.allUsers.find { it.uid == uid }?.name ?: "מישהו"
                    
                    // 1. Regex to check if anyone is tagged explicitly
                    val tagRegex = Regex("@([\\wא-ת\\-]+(?:\\s+[\\wא-ת\\-]+)?)")
                    val matches = tagRegex.findAll(text)
                    val allUsers = currentState.allUsers

                    var notifiedTaggedUser = false

                    for (match in matches) {
                        val usernameMatched = match.groupValues[1].trim()
                        val taggedUser = allUsers.find { it.name == usernameMatched }

                        Log.d("FCM_PUSH", "Tagged user match: $usernameMatched - Found in DB: ${taggedUser != null}")

                        if (taggedUser != null && taggedUser.uid != uid && !taggedUser.fcmToken.isNullOrEmpty()) {
                            notificationRepo.sendPushNotification(
                                targetToken = taggedUser.fcmToken,
                                title = "$currentUserName תייג/ה אותך בתגובה!",
                                body = text,
                                reportId = reportId
                            )
                            notifiedTaggedUser = true
                        } else if (taggedUser != null && taggedUser.fcmToken.isNullOrEmpty()) {
                            Log.e("FCM_PUSH", "Tagged user $usernameMatched does not have an FCM token.")
                        }
                    }

                    // 2. If no one was tagged, send notification to the original report author
                    if (!notifiedTaggedUser) {
                        val reportAuthor = currentState.author
                        Log.d("FCM_PUSH", "No valid tag found. Falling back to Report Author: ${reportAuthor?.name}")
                        if (reportAuthor != null && reportAuthor.uid != uid && !reportAuthor.fcmToken.isNullOrEmpty()) {
                            notificationRepo.sendPushNotification(
                                targetToken = reportAuthor.fcmToken,
                                title = "$currentUserName הגיב/ה על הדיווח שלך",
                                body = text,
                                reportId = reportId
                            )
                        } else if (reportAuthor != null && reportAuthor.fcmToken.isNullOrEmpty()) {
                            Log.e("FCM_PUSH", "Report Author ${reportAuthor.name} does not have an FCM token.")
                        } else if (reportAuthor?.uid == uid) {
                            Log.d("FCM_PUSH", "Author is commenting on their own post. No push sent.")
                        }
                    }
                }
            } else {
                _uiState.update { it.copy(error = "שגיאה בשליחת התגובה") }
            }
        }
    }

    fun deleteComment(reportId: String, commentId: String) {
        commentsRepo.deleteComment(reportId, commentId) { success ->
            if (!success) {
                _uiState.update { it.copy(error = "שגיאה במחיקת התגובה") }
            }
        }
    }
}
