package com.example.neighborhoodreports.data

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveUser(uid: String) {
        prefs.edit().putString("uid", uid).apply()
    }

    fun getUser(): String? {
        return prefs.getString("uid", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun debugPrint() {
        println("Saved UID = ${prefs.getString("uid", null)}")    }

}
