package com.example.neighborhoodreports.data

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveUser(uid: String) {
        prefs.edit().putString("uid", uid).apply()
    }

    fun saveRole(role: String) {
        prefs.edit().putString("role", role).apply()
    }

    fun getUser(): String? = prefs.getString("uid", null)

    fun getRole(): String? = prefs.getString("role", null)

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun debugPrint() {
        println("Saved UID = ${prefs.getString("uid", null)}")
    }
}
