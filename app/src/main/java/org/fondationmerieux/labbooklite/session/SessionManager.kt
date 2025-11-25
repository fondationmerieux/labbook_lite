package org.fondationmerieux.labbooklite.session

import android.content.Context

object SessionManager {

    // Returns the current logged-in user id, or 0 if not set
    fun getCurrentUserId(context: Context): Int {
        val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
        return prefs.getInt("user_id", 0)
    }

    // Example: get current username if you need it later
    fun getCurrentUsername(context: Context): String? {
        val prefs = context.getSharedPreferences("LabBookPrefs", Context.MODE_PRIVATE)
        return prefs.getString("username", null)
    }
}
