package com.example.campusguardian.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.campusguardian.db.DbProvider
import com.example.campusguardian.model.Announcement

class AnnouncementRepo(private val context: Context) {

    private val helper = DbProvider.get(context)

    fun createAnnouncement(title: String, message: String, adminId: Long): Long {
        val db = helper.writableDatabase
        val now = System.currentTimeMillis()

        val cv = ContentValues()
        cv.put("title", title.trim())
        cv.put("message", message.trim())
        cv.put("created_at", now)
        cv.put("created_by_admin_id", adminId)
        cv.put("active", 1)

        return db.insert("announcements", null, cv)
    }

    fun listActive(): List<Announcement> {
        val db = helper.readableDatabase
        val list = ArrayList<Announcement>()
        val c = db.query(
            "announcements",
            null,
            "active = ?",
            arrayOf("1"),
            null,
            null,
            "created_at DESC"
        )
        c.use {
            while (it.moveToNext()) list.add(cursorToAnnouncement(it))
        }
        return list
    }

    private fun cursorToAnnouncement(c: Cursor): Announcement {
        return Announcement(
            id = c.getLong(c.getColumnIndexOrThrow("id")),
            title = c.getString(c.getColumnIndexOrThrow("title")),
            message = c.getString(c.getColumnIndexOrThrow("message")),
            createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
            createdByAdminId = c.getLong(c.getColumnIndexOrThrow("created_by_admin_id")),
            active = c.getInt(c.getColumnIndexOrThrow("active"))
        )
    }
}
