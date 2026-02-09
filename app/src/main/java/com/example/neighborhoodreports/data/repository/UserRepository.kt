package com.example.neighborhoodreports.data.repository

import com.example.neighborhoodreports.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createUser(user: User) {
        db.collection("users")
            .document(user.uid)
            .set(user)
            .await()
    }

    suspend fun getUser(uid: String): User? {
        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.toObject(User::class.java)
    }

    suspend fun isUserExists(uid: String): Boolean {
        return try {
            val doc = db
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
