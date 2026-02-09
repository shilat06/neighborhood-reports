package com.example.neighborhoodreports.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AppUser(
    val uid: String = "",
    val email: String = "",
    val role: String = "user"
)

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createUser(user: AppUser) {
        db.collection("users")
            .document(user.uid)
            .set(user)
            .await()
    }

    suspend fun getUser(uid: String): AppUser? {
        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.toObject(AppUser::class.java)
    }

    suspend fun isUserExists(uid: String): Boolean {
        return try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

}
