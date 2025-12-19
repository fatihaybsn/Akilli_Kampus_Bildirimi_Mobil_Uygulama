package com.example.akllkampssalkvegvenlikbildirimuygulamas.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.akllkampssalkvegvenlikbildirimuygulamas.db.DbProvider
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.NotificationLog

class NotificationRepo(private val context: Context) {

    private val helper = DbProvider.get(context)

    fun insertLog(
        userId: Long,
        kind: String,
        refId: Long?,
        title: String,
        body: String
    ): Long {
        val db = helper.writableDatabase
        val cv = ContentValues()
        cv.put("user_id", userId)
        cv.put("kind", kind)
        if (refId != null) cv.put("ref_id", refId) else cv.putNull("ref_id")
        cv.put("title", title)
        cv.put("body", body)
        cv.put("created_at", System.currentTimeMillis())
        cv.putNull("read_at")
        return db.insert("notification_log", null, cv)
    }

    fun listForUser(userId: Long, onlyUnread: Boolean): List<NotificationLog> {
        val db = helper.readableDatabase
        val list = ArrayList<NotificationLog>()

        val whereParts = ArrayList<String>()
        val args = ArrayList<String>()

        whereParts.add("user_id = ?")
        args.add(userId.toString())

        if (onlyUnread) {
            whereParts.add("read_at IS NULL")
        }

        val selection = whereParts.joinToString(" AND ")

        val c = db.query(
            "notification_log",
            null,
            selection,
            args.toTypedArray(),
            null,
            null,
            "created_at DESC"
        )

        c.use {
            while (it.moveToNext()) list.add(cursorToLog(it))
        }

        return list
    }

    fun countUnread(userId: Long): Int {
        val db = helper.readableDatabase
        val c = db.rawQuery(
            "SELECT COUNT(*) FROM notification_log WHERE user_id = ? AND read_at IS NULL",
            arrayOf(userId.toString())
        )
        c.use {
            if (!it.moveToFirst()) return 0
            return it.getInt(0)
        }
    }

    fun markRead(logId: Long): Boolean {
        val db = helper.writableDatabase
        val cv = ContentValues()
        cv.put("read_at", System.currentTimeMillis())

        val rows = db.update(
            "notification_log",
            cv,
            "id = ?",
            arrayOf(logId.toString())
        )
        return rows > 0
    }

    fun markAllRead(userId: Long): Int {
        val db = helper.writableDatabase
        val cv = ContentValues()
        cv.put("read_at", System.currentTimeMillis())

        return db.update(
            "notification_log",
            cv,
            "user_id = ? AND read_at IS NULL",
            arrayOf(userId.toString())
        )
    }

    private fun cursorToLog(c: Cursor): NotificationLog {
        val refIdx = c.getColumnIndexOrThrow("ref_id")
        val readIdx = c.getColumnIndexOrThrow("read_at")

        val refId = if (c.isNull(refIdx)) null else c.getLong(refIdx)
        val readAt = if (c.isNull(readIdx)) null else c.getLong(readIdx)

        return NotificationLog(
            id = c.getLong(c.getColumnIndexOrThrow("id")),
            userId = c.getLong(c.getColumnIndexOrThrow("user_id")),
            kind = c.getString(c.getColumnIndexOrThrow("kind")),
            refId = refId,
            title = c.getString(c.getColumnIndexOrThrow("title")),
            body = c.getString(c.getColumnIndexOrThrow("body")),
            createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
            readAt = readAt
        )
    }
}
