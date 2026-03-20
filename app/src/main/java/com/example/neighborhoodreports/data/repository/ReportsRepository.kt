package com.example.neighborhoodreports.data.repository

import com.example.neighborhoodreports.model.Report
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * מחלקה לניהול דיווחים במסד הנתונים.
 * מטפלת בשליפה, יצירה, עדכון ומחיקה של דיווחים מול Firebase Firestore.
 */
class ReportsRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val reportsRef = db.collection("reports")

    /**
     * טעינת דיווחים מאושרים למשתמש רגיל
     */
    fun getApprovedReports(onResult: (List<Report>) -> Unit) {
        reportsRef.whereEqualTo("status", "approved").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                println("error while fetching reports: ${e?.message}")
                onResult(emptyList())
                return@addSnapshotListener
            }
            val reports = snapshot.toObjects(Report::class.java).sortedByDescending { it.createdAt }
            onResult(reports)
        }
    }

    /**
     * טעינת דיווחים מאושרים עם תמיכה בעימוד (Cursor-based Pagination)
     */
    fun getApprovedReportsPaged(
            pageSize: Long,
            categoryId: String?,
            sortDescending: Boolean,
            lastDoc: DocumentSnapshot?,
            onResult: (List<Report>, DocumentSnapshot?) -> Unit
    ) {
        var query = reportsRef.whereEqualTo("status", "approved")
        
        if (categoryId != null) {
            query = query.whereEqualTo("categoryId", categoryId)
        }

        val direction = if (sortDescending) com.google.firebase.firestore.Query.Direction.DESCENDING else com.google.firebase.firestore.Query.Direction.ASCENDING
        query = query.orderBy("createdAt", direction).limit(pageSize)

        if (lastDoc != null) {
            query = query.startAfter(lastDoc)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.toObjects(Report::class.java)
                val cursor = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
                onResult(items, cursor)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ReportsRepo", "Error fetching approved reports paged", e)
                onResult(emptyList(), lastDoc)
            }
    }

    /**
     * טעינת דיווחים שממתינים לאישור (רק למנהל)
     */
    fun getPendingReports(onResult: (List<Report>) -> Unit) {
        reportsRef.whereEqualTo("status", "pending").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                println("error while fetching pending reports: ${e?.message}")
                onResult(emptyList())
                return@addSnapshotListener
            }
            val reports = snapshot.toObjects(Report::class.java).sortedByDescending { it.createdAt }
            onResult(reports)
        }
    }

    /**
     * טעינת דיווחים ממתינים תוך שימוש בעימוד (cursor-based)
     */
    fun getPendingReportsPaged(
            pageSize: Long,
            lastDoc: DocumentSnapshot?,
            onResult: (List<Report>, DocumentSnapshot?) -> Unit
    ) {
        var query = reportsRef
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(pageSize)
        if (lastDoc != null) query = query.startAfter(lastDoc)

        query.get()
                .addOnSuccessListener { snapshot ->
                    val items = snapshot.toObjects(Report::class.java)
                    val cursor = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
                    android.util.Log.d("ReportsRepo", "Fetched ${items.size} pending reports")
                    onResult(items, cursor)
                }
                .addOnFailureListener { e -> 
                    android.util.Log.e("ReportsRepo", "Error fetching pending reports", e)
                    onResult(emptyList(), lastDoc) 
                }
    }

    /**
     * הוספת דיווח חדש למערכת
     */
    fun addReport(report: Report, onComplete: (Boolean) -> Unit) {
        val doc = reportsRef.document()
        val newReport = report.copy(id = doc.id)
        doc.set(newReport).addOnCompleteListener { onComplete(it.isSuccessful) }
    }
    
    /**
     * עדכון פרטי דיווח קיים
     */
    fun updateReport(report: Report, onComplete: (Boolean) -> Unit) {
        reportsRef.document(report.id).set(report).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    /**
     * עדכון סטטוס של דיווח (רלוונטי למסך ניהול)
     */
    fun updateStatus(id: String, status: String, onComplete: (Boolean) -> Unit) {
        reportsRef.document(id).update("status", status).addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    /**
     * מחיקת דיווח מהמערכת
     */
    fun deleteReport(id: String, onComplete: (Boolean) -> Unit) {
        reportsRef.document(id).delete().addOnCompleteListener { onComplete(it.isSuccessful) }
    }
    /**
     * שליפת דיווח ספציפי לפי מזהה (ID)
     */
    fun getReportById(id: String, onResult: (Report?) -> Unit) {
        reportsRef
                .document(id)
                .get()
                .addOnSuccessListener { snap -> onResult(snap.toObject(Report::class.java)) }
                .addOnFailureListener { onResult(null) }
    }
}
