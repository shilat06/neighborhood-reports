package com.example.neighborhoodreports.data.repository

import com.example.neighborhoodreports.model.Report
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReportsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val reportsRef = db.collection("reports")

    // טעינת דיווחים מאושרים למשתמש רגיל
    fun getApprovedReports(onResult: (List<Report>) -> Unit) {

        reportsRef
            .whereEqualTo("status", "approved")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    println("🔥 ERROR reports: ${e?.message}")
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                else{
                    println("🔥nullll ERROR reports: ${e} ${snapshot.documents } ")

                }

                onResult(snapshot.toObjects(Report::class.java))
            }
    }

    // טעינת דיווחים שממתינים לאישור (Admin)
    fun getPendingReports(onResult: (List<Report>) -> Unit) {
        reportsRef
            .whereEqualTo("status", "pending")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                onResult(snapshot.toObjects(Report::class.java))
            }
    }

    // הוספת דיווח חדש
    fun addReport(report: Report, onComplete: (Boolean) -> Unit) {
        val doc = reportsRef.document()
        val newReport = report.copy(id = doc.id)
        doc.set(newReport).addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    // עדכון סטטוס (Admin)
    fun updateStatus(id: String, status: String, onComplete: (Boolean) -> Unit) {
        reportsRef.document(id)
            .update("status", status)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // מחיקת דיווח
    fun deleteReport(id: String, onComplete: (Boolean) -> Unit) {
        reportsRef.document(id)
            .delete()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
    //שליפת דיווח לפי ID
    fun getReportById(id: String, onResult: (Report?) -> Unit) {
        reportsRef.document(id)
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.toObject(Report::class.java))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

}
