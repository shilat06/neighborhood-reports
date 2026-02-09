package com.example.neighborhoodreports.data.repository

import com.example.neighborhoodreports.model.Category
import com.google.firebase.firestore.FirebaseFirestore

class CategoriesRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val categoriesRef = db.collection("categories")

    // טעינת כל הקטגוריות בזמן אמת
    fun getCategories(onResult: (List<Category>) -> Unit) {
//        categoriesRef.addSnapshotListener { snapshot, e ->
//            if (e != null || snapshot == null) {
//                onResult(emptyList())
//                return@addSnapshotListener
//            }
//            onResult(snapshot.toObjects(Category::class.java))
//        }
        categoriesRef.addSnapshotListener { snapshot, e ->
            println("🔥 getCategories called")

            if (e != null) {
                println("🔥 ERROR: ${e.message}")
                onResult(emptyList())
                return@addSnapshotListener
            }

            if (snapshot == null) {
                println("🔥 snapshot null")
                onResult(emptyList())
                return@addSnapshotListener
            }

            val list = snapshot.toObjects(Category::class.java)
            println("📌 categories loaded: ${list.size}")
            onResult(list)
        }


    }

    // הוספת קטגוריה חדשה
    fun addCategory(name: String, onComplete: (Boolean) -> Unit) {
        val doc = categoriesRef.document()
        val category = Category(id = doc.id, name = name)
        doc.set(category).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // מחיקת קטגוריה
    fun deleteCategory(id: String, onComplete: (Boolean) -> Unit) {
        categoriesRef.document(id).delete().addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }
}
