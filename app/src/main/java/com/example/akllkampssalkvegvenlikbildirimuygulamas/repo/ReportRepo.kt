package com.example.campusguardian.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.campusguardian.db.DbProvider
import com.example.campusguardian.model.Report
import com.example.campusguardian.model.User

class ReportRepo(private val context: Context) {

    private val helper = DbProvider.get(context)

    fun createReport(
        creator: User,
        typeDb: String,
        title: String,
        description: String,
        lat: Double,
        lon: Double,
        photoUri: String?
    ): Long {
        val db = helper.writableDatabase
        val now = System.currentTimeMillis()

        val cv = ContentValues()
        cv.put("type", typeDb)
        cv.put("title", title.trim())
        cv.put("description", description.trim())
        cv.put("status", "OPEN")
        cv.put("lat", lat)
        cv.put("lon", lon)
        if (!photoUri.isNullOrBlank()) cv.put("photo_uri", photoUri)
        cv.put("created_by_user_id", creator.id)
        cv.put("unit", creator.unit) // rule: report.unit = creator.unit
        cv.put("created_at", now)
        cv.put("updated_at", now)

        return db.insert("reports", null, cv)
    }

    fun getById(reportId: Long): Report? {
        val db = helper.readableDatabase
        val c = db.query(
            "reports",
            null,
            "id = ?",
            arrayOf(reportId.toString()),
            null,
            null,
            null
        )
        c.use {
            if (!it.moveToFirst()) return null
            return cursorToReport(it)
        }
    }

    data class QueryParams(
        val typeDb: String? = null,
        val onlyOpen: Boolean = false,
        val onlyFollowedByUserId: Long? = null,
        val keyword: String? = null,
        val sortDesc: Boolean = true,
        val adminUnitOnly: String? = null, // if not null -> unit = value
        val statusDb: String? = null
    )

    fun listReports(params: QueryParams): List<Report> {
        val db = helper.readableDatabase
        val list = ArrayList<Report>()

        val whereParts = ArrayList<String>()
        val args = ArrayList<String>()

        params.typeDb?.let {
            whereParts.add("type = ?")
            args.add(it)
        }

        params.statusDb?.let {
            whereParts.add("status = ?")
            args.add(it)
        }

        if (params.onlyOpen) {
            whereParts.add("status = ?")
            args.add("OPEN")
        }

        params.adminUnitOnly?.let { unit ->
            whereParts.add("unit = ?")
            args.add(unit)
        }

        params.onlyFollowedByUserId?.let { uid ->
            whereParts.add("id IN (SELECT report_id FROM follows WHERE user_id = ?)")
            args.add(uid.toString())
        }

        val kw = params.keyword?.trim().orEmpty()
        if (kw.isNotEmpty()) {
            whereParts.add("(title LIKE ? OR description LIKE ?)")
            val like = "%$kw%"
            args.add(like)
            args.add(like)
        }

        val selection = if (whereParts.isEmpty()) null else whereParts.joinToString(" AND ")
        val orderBy = "created_at " + if (params.sortDesc) "DESC" else "ASC"

        val c = db.query(
            "reports",
            null,
            selection,
            if (args.isEmpty()) null else args.toTypedArray(),
            null,
            null,
            orderBy
        )

        c.use {
            while (it.moveToNext()) {
                list.add(cursorToReport(it))
            }
        }

        return list
    }

    fun updateStatus(reportId: Long, newStatusDb: String): Boolean {
        val db = helper.writableDatabase
        val cv = ContentValues()
        cv.put("status", newStatusDb)
        cv.put("updated_at", System.currentTimeMillis())

        val rows = db.update(
            "reports",
            cv,
            "id = ?",
            arrayOf(reportId.toString())
        )
        return rows > 0
    }

    fun updateDescription(reportId: Long, newDescription: String): Boolean {
        val db = helper.writableDatabase
        val cv = ContentValues()
        cv.put("description", newDescription.trim())
        cv.put("updated_at", System.currentTimeMillis())

        val rows = db.update(
            "reports",
            cv,
            "id = ?",
            arrayOf(reportId.toString())
        )
        return rows > 0
    }

    fun updatePhoto(reportId: Long, photoUri: String?): Boolean {
        val db = helper.writableDatabase
        val cv = ContentValues()
        if (photoUri.isNullOrBlank()) {
            cv.putNull("photo_uri")
        } else {
            cv.put("photo_uri", photoUri)
        }
        cv.put("updated_at", System.currentTimeMillis())

        val rows = db.update(
            "reports",
            cv,
            "id = ?",
            arrayOf(reportId.toString())
        )
        return rows > 0
    }

    private fun cursorToReport(c: Cursor): Report {
        return Report(
            id = c.getLong(c.getColumnIndexOrThrow("id")),
            type = c.getString(c.getColumnIndexOrThrow("type")),
            title = c.getString(c.getColumnIndexOrThrow("title")),
            description = c.getString(c.getColumnIndexOrThrow("description")),
            status = c.getString(c.getColumnIndexOrThrow("status")),
            lat = c.getDouble(c.getColumnIndexOrThrow("lat")),
            lon = c.getDouble(c.getColumnIndexOrThrow("lon")),
            photoUri = c.getString(c.getColumnIndexOrThrow("photo_uri")),
            createdByUserId = c.getLong(c.getColumnIndexOrThrow("created_by_user_id")),
            unit = c.getString(c.getColumnIndexOrThrow("unit")),
            createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
            updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at"))
        )
    }
}
