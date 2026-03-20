package com.example.neighborhoodreports.data.repository

import com.example.neighborhoodreports.model.Category
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * מחלקה לניהול קטגוריות הדיווחים מול המסד נתונים.
 * מטפלת בשליפת רשימת הקטגוריות, יצירת קטגוריה חדשה ומחיקה.
 */
class CategoriesRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val categoriesRef = db.collection("categories")

    /**
     * טעינת כל הקטגוריות בזמן אמת (חיבור ישיר למסד הנתונים)
     */
    fun getCategories(onResult: (List<Category>) -> Unit) {
        //        categoriesRef.addSnapshotListener { snapshot, e ->
        //            if (e != null || snapshot == null) {
        //                onResult(emptyList())
        //                return@addSnapshotListener
        //            }
        //            onResult(snapshot.toObjects(Category::class.java))
        //        }
        categoriesRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("error while fetching categories: ${e.message}")
                onResult(emptyList())
                return@addSnapshotListener
            }

            if (snapshot == null) {
                println("snapshot null")
                onResult(emptyList())
                return@addSnapshotListener
            }

            val list = snapshot.toObjects(Category::class.java)
            println("categories loaded: ${list.size}")
            onResult(list)
        }
    }
    /**
     * טעינת קטגוריות עם מנגנון עימוד (cursor-based)
     */
    fun getCategoriesPaged(
            pageSize: Long,
            lastDoc: DocumentSnapshot?,
            onResult: (List<Category>, DocumentSnapshot?) -> Unit
    ) {
        var query = categoriesRef.limit(pageSize)
        if (lastDoc != null) query = query.startAfter(lastDoc)

        query.get()
                .addOnSuccessListener { snapshot ->
                    val items = snapshot.toObjects(Category::class.java)
                    val cursor = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
                    android.util.Log.d("CategoriesRepo", "Fetched ${items.size} categories")
                    onResult(items, cursor)
                }
                .addOnFailureListener { e -> 
                    android.util.Log.e("CategoriesRepo", "Error fetching categories", e)
                    onResult(emptyList(), lastDoc) 
                }
    }

    /**
     * יצירת קטגוריה חדשה
     */
    fun addCategory(name: String, onComplete: (Boolean) -> Unit) {
        val doc = categoriesRef.document()
        val category = Category(id = doc.id, name = name)
        doc.set(category).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    /**
     * מחיקת קטגוריה קיימת
     */
    fun deleteCategory(id: String, onComplete: (Boolean) -> Unit) {
        categoriesRef.document(id).delete().addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    /**
     * שליפת קטגוריה על פי ID
     */
    fun getCategoryById(id: String, onResult: (Category?) -> Unit) {
        categoriesRef
                .document(id)
                .get()
                .addOnSuccessListener { snap -> onResult(snap.toObject(Category::class.java)) }
                .addOnFailureListener { onResult(null) }
    }
}
