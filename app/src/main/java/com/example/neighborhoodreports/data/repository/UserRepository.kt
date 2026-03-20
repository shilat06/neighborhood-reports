package com.example.neighborhoodreports.data.repository

import com.example.neighborhoodreports.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

/**
 * מנהל את המידע על המשתמשים (User Profile) במסד הנתונים Firestore.
 * מרכז את השמירה והשליפה של פרטי המשתמש.
 */
class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /**
     * יצירת מסמך למשתמש חדש במסד הנתונים
     */
    suspend fun createUser(user: User) {
        db.collection("users").document(user.uid).set(user).await()
    }

    /**
     * שליפת פרטי משתמש לפי מזהה (UID) מול השרת, עם גיבוי של Cache במקרה של ניתוק מהרשת
     */
    suspend fun getUser(uid: String): User? {
        // שלב 1: ניסיון שליפה מהשרת
        val snapshot =
                try {
                    db.collection("users").document(uid).get(Source.SERVER).await()
                } catch (e: Exception) {
                    // שלב 2: אם השרת לא נגיש (אין אינטרנט/SDK לא מוכן) — נשתמש ב-cache
                    db.collection("users").document(uid).get(Source.CACHE).await()
                }

        return snapshot.toObject(User::class.java)
    }

    /**
     * עדכון תמונת הפרופיל (avatar) של המשתמש
     */
    suspend fun updateAvatarUrl(uid: String, url: String) {
        db.collection("users").document(uid).update("avatarUrl", url).await()
    }

    /**
     * עדכון טוקן ה-FCM של המשתמש (לקבלת Push Notifications)
     */
    suspend fun updateFcmToken(uid: String, token: String) {
        db.collection("users").document(uid).update("fcmToken", token).await()
    }

    /**
     * בדיקה האם המשתמש כבר קיים במסד הנתונים
     */
    suspend fun isUserExists(uid: String): Boolean {
        return try {
            val doc = db.collection("users").document(uid).get().await()

            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * שליפת כל המשתמשים
     */
    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
