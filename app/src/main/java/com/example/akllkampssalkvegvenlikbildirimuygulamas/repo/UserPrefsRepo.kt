package com.example.akllkampssalkvegvenlikbildirimuygulamas.repo

import android.content.ContentValues
import android.content.Context
import com.example.akllkampssalkvegvenlikbildirimuygulamas.db.DbProvider
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportType

class UserPrefsRepo(private val context: Context) {

    private val helper = DbProvider.get(context)

    fun isEnabled(userId: Long, typeDb: String): Boolean {
        val db = helper.readableDatabase
        val c = db.query(
            "user_prefs",
            arrayOf("enabled"),
            "user_id = ? AND type = ?",
            arrayOf(userId.toString(), typeDb),
            null,
            null,
            null
        )
        c.use {
            if (!it.moveToFirst()) return true // default: enabled
            return it.getInt(it.getColumnIndexOrThrow("enabled")) == 1
        }
    }

    fun getAllPrefs(userId: Long): Map<String, Boolean> {
        val db = helper.readableDatabase
        val map = LinkedHashMap<String, Boolean>()
        // ensure consistent ordering by ReportType enum order
        for (t in ReportType.values()) {
            map[t.dbValue] = isEnabled(userId, t.dbValue)
        }
        return map
    }

    fun setEnabled(userId: Long, typeDb: String, enabled: Boolean): Boolean {
        val db = helper.writableDatabase
        val cv = ContentValues()
        cv.put("enabled", if (enabled) 1 else 0)

        val updated = db.update(
            "user_prefs",
            cv,
            "user_id = ? AND type = ?",
            arrayOf(userId.toString(), typeDb)
        )

        if (updated > 0) return true

        // if row doesn't exist, insert
        val ins = ContentValues()
        ins.put("user_id", userId)
        ins.put("type", typeDb)
        ins.put("enabled", if (enabled) 1 else 0)
        val id = db.insertWithOnConflict(
            "user_prefs",
            null,
            ins,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        return id > 0
    }
}
