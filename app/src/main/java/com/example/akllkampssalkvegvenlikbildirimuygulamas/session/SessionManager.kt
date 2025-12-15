package com.example.campusguardian.session

import android.content.Context
import com.example.campusguardian.model.User
import com.example.campusguardian.repo.UserRepo

object SessionManager {
    private const val PREF = "session"
    private const val KEY_USER_ID = "user_id"

    @Volatile private var cachedUser: User? = null

    fun login(context: Context, user: User) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_USER_ID, user.id)
            .apply()
        cachedUser = user
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_USER_ID)
            .apply()
        cachedUser = null
    }

    fun getCurrentUserId(context: Context): Long? {
        val id = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong(KEY_USER_ID, -1L)
        return if (id > 0) id else null
    }

    fun getCurrentUser(context: Context): User? {
        cachedUser?.let { return it }

        val id = getCurrentUserId(context) ?: return null
        val user = UserRepo(context).getById(id)
        cachedUser = user
        return user
    }

    fun requireUser(context: Context): User {
        return getCurrentUser(context) ?: throw IllegalStateException("No active session user")
    }

    fun isAdmin(context: Context): Boolean {
        return getCurrentUser(context)?.role == "ADMIN"
    }
}
