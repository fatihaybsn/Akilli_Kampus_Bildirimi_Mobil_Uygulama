package com.example.akllkampssalkvegvenlikbildirimuygulamas.repo

import android.content.ContentValues
import android.content.Context
import com.example.akllkampssalkvegvenlikbildirimuygulamas.db.DbProvider

class FollowRepo(private val context: Context) {

    private val helper = DbProvider.get(context)

    fun isFollowed(userId: Long, reportId: Long): Boolean {
        val db = helper.readableDatabase
        val c = db.query(
            "follows",
            arrayOf("user_id"),
            "user_id = ? AND report_id = ?",
            arrayOf(userId.toString(), reportId.toString()),
            null,
            null,
            null
        )
        c.use { return it.moveToFirst() }
    }

    fun follow(userId: Long, reportId: Long): Boolean {
        val db = helper.writableDatabase
        val cv = ContentValues()
        cv.put("user_id", userId)
        cv.put("report_id", reportId)
        cv.put("created_at", System.currentTimeMillis())

        val id = db.insertWithOnConflict(
            "follows",
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
        )
        return id != -1L
    }

    fun unfollow(userId: Long, reportId: Long): Boolean {
        val db = helper.writableDatabase
        val rows = db.delete(
            "follows",
            "user_id = ? AND report_id = ?",
            arrayOf(userId.toString(), reportId.toString())
        )
        return rows > 0
    }

    fun getFollowerUserIds(reportId: Long): List<Long> {
        val db = helper.readableDatabase
        val list = ArrayList<Long>()
        val c = db.query(
            "follows",
            arrayOf("user_id"),
            "report_id = ?",
            arrayOf(reportId.toString()),
            null,
            null,
            "created_at ASC"
        )
        c.use {
            while (it.moveToNext()) {
                list.add(it.getLong(it.getColumnIndexOrThrow("user_id")))
            }
        }
        return list
    }

    fun getFollowedReportIds(userId: Long): List<Long> {
        val db = helper.readableDatabase
        val list = ArrayList<Long>()
        val c = db.query(
            "follows",
            arrayOf("report_id"),
            "user_id = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "created_at DESC"
        )
        c.use {
            while (it.moveToNext()) {
                list.add(it.getLong(it.getColumnIndexOrThrow("report_id")))
            }
        }
        return list
    }
}
