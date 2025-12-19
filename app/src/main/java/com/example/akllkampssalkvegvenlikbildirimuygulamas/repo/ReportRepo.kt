package com.example.akllkampssalkvegvenlikbildirimuygulamas.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.akllkampssalkvegvenlikbildirimuygulamas.db.DbProvider
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.Report
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.User
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.UiMappings

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

        // Route by report type (responsible unit), not by reporter's unit
        cv.put("unit", UiMappings.responsibleUnit(typeDb))

        cv.put("created_at", now)
        cv.put("updated_at", now)

        return db.insert("reports", null, cv)
    }

    fun getById(reportId: Long): Report? {
        val db = helper.readableDatabase
        val sql = """
            SELECT r.*,
                   u.name AS created_by_name,
                   u.email AS created_by_email
            FROM reports r
            LEFT JOIN users u ON u.id = r.created_by_user_id
            WHERE r.id = ?
        """.trimIndent()

        val c = db.rawQuery(sql, arrayOf(reportId.toString()))
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
        val statusDb: String? = null,
        val includeCreatorInfo: Boolean = false
    )

    fun listReports(params: QueryParams): List<Report> {
        val db = helper.readableDatabase
        val list = ArrayList<Report>()

        val whereParts = ArrayList<String>()
        val args = ArrayList<String>()

        // Use alias when joined
        val p = if (params.includeCreatorInfo) "r." else ""

        params.typeDb?.let {
            whereParts.add("${p}type = ?")
            args.add(it)
        }

        params.statusDb?.let {
            whereParts.add("${p}status = ?")
            args.add(it)
        }

        if (params.onlyOpen) {
            whereParts.add("${p}status = ?")
            args.add("OPEN")
        }

        params.adminUnitOnly?.let { unit ->
            whereParts.add("${p}unit = ?")
            args.add(unit)
        }

        params.onlyFollowedByUserId?.let { uid ->
            whereParts.add("${p}id IN (SELECT report_id FROM follows WHERE user_id = ?)")
            args.add(uid.toString())
        }

        val kw = params.keyword?.trim().orEmpty()
        if (kw.isNotEmpty()) {
            whereParts.add("(${p}title LIKE ? OR ${p}description LIKE ?)")
            val like = "%$kw%"
            args.add(like)
            args.add(like)
        }

        val selection = if (whereParts.isEmpty()) "" else ("WHERE " + whereParts.joinToString(" AND "))
        val orderBy = "${p}created_at " + if (params.sortDesc) "DESC" else "ASC"

        val cursor: Cursor = if (params.includeCreatorInfo) {
            val sql = """
                SELECT r.*,
                       u.name AS created_by_name,
                       u.email AS created_by_email
                FROM reports r
                LEFT JOIN users u ON u.id = r.created_by_user_id
                $selection
                ORDER BY $orderBy
            """.trimIndent()
            db.rawQuery(sql, if (args.isEmpty()) null else args.toTypedArray())
        } else {
            db.query(
                "reports",
                null,
                if (whereParts.isEmpty()) null else whereParts.joinToString(" AND "),
                if (args.isEmpty()) null else args.toTypedArray(),
                null,
                null,
                orderBy
            )
        }

        cursor.use {
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

    fun closeAsInvalid(reportId: Long, adminNote: String): Boolean {
        val current = getById(reportId) ?: return false
        val noteBlock = adminNote.trim()

        val alreadyHasNote = current.description.contains("[Admin Notu]")
        val newDesc = if (alreadyHasNote) current.description else {
            current.description.trim() + "\n\n[Admin Notu] $noteBlock"
        }

        val okDesc = updateDescription(reportId, newDesc)
        val okStatus = updateStatus(reportId, "RESOLVED")
        return okDesc && okStatus
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
        fun optString(col: String): String? {
            val idx = c.getColumnIndex(col)
            return if (idx >= 0) c.getString(idx) else null
        }

        return Report(
            id = c.getLong(c.getColumnIndexOrThrow("id")),
            type = c.getString(c.getColumnIndexOrThrow("type")),
            title = c.getString(c.getColumnIndexOrThrow("title")),
            description = c.getString(c.getColumnIndexOrThrow("description")),
            status = c.getString(c.getColumnIndexOrThrow("status")),
            lat = c.getDouble(c.getColumnIndexOrThrow("lat")),
            lon = c.getDouble(c.getColumnIndexOrThrow("lon")),
            photoUri = optString("photo_uri"),
            createdByUserId = c.getLong(c.getColumnIndexOrThrow("created_by_user_id")),
            unit = c.getString(c.getColumnIndexOrThrow("unit")),
            createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
            updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at")),
            createdByName = optString("created_by_name"),
            createdByEmail = optString("created_by_email")
        )
    }
}
