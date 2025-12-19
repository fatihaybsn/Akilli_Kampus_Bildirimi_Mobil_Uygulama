package com.example.akllkampssalkvegvenlikbildirimuygulamas.repo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import com.example.akllkampssalkvegvenlikbildirimuygulamas.db.DbProvider
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportType
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.User
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.HashUtils

class UserRepo(private val context: Context) {

    private val helper = DbProvider.get(context)

    fun login(email: String, passwordPlain: String): User? {
        val db = helper.readableDatabase
        val columns = arrayOf("id", "name", "email", "password_hash", "role", "unit", "created_at")

        val c = db.query(
            "users",
            columns,
            "email = ?",
            arrayOf(email.trim()),
            null,
            null,
            null
        )

        c.use {
            if (!it.moveToFirst()) return null
            val storedHash = it.getString(it.getColumnIndexOrThrow("password_hash"))
            val givenHash = HashUtils.sha256(passwordPlain)
            if (storedHash != givenHash) return null
            return cursorToUser(it)
        }
    }

    fun register(name: String, email: String, passwordPlain: String, unit: String): Pair<Long?, String?> {
        val db = helper.writableDatabase
        db.beginTransaction()
        return try {
            val now = System.currentTimeMillis()

            val cv = ContentValues()
            cv.put("name", name.trim())
            cv.put("email", email.trim())
            cv.put("password_hash", HashUtils.sha256(passwordPlain))
            cv.put("role", "USER")
            cv.put("unit", unit.trim())
            cv.put("created_at", now)

            val userId = db.insert("users", null, cv)
            if (userId <= 0) {
                Pair(null, "Kayıt başarısız.")
            } else {
                // create default prefs: all enabled
                for (t in ReportType.values()) {
                    val p = ContentValues()
                    p.put("user_id", userId)
                    p.put("type", t.dbValue)
                    p.put("enabled", 1)
                    db.insertWithOnConflict("user_prefs", null, p, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
                }
                db.setTransactionSuccessful()
                Pair(userId, null)
            }
        } catch (e: SQLiteConstraintException) {
            Pair(null, "Bu e-posta zaten kayıtlı.")
        } catch (e: Exception) {
            Pair(null, "Kayıt başarısız: ${e.message ?: "hata"}")
        } finally {
            db.endTransaction()
        }
    }

    fun getById(userId: Long): User? {
        val db = helper.readableDatabase
        val c = db.query(
            "users",
            arrayOf("id", "name", "email", "role", "unit", "created_at"),
            "id = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )
        c.use {
            if (!it.moveToFirst()) return null
            return cursorToUser(it)
        }
    }

    fun getAllUserIds(): List<Long> {
        val db = helper.readableDatabase
        val list = ArrayList<Long>()
        val c = db.query("users", arrayOf("id"), null, null, null, null, "id ASC")
        c.use {
            while (it.moveToNext()) {
                list.add(it.getLong(it.getColumnIndexOrThrow("id")))
            }
        }
        return list
    }

    fun getByEmail(email: String): User? {
        val db = helper.readableDatabase
        val c = db.query(
            "users",
            arrayOf("id", "name", "email", "role", "unit", "created_at"),
            "email = ?",
            arrayOf(email.trim()),
            null,
            null,
            null
        )
        c.use {
            if (!it.moveToFirst()) return null
            return cursorToUser(it)
        }
    }

    private fun cursorToUser(c: Cursor): User {
        return User(
            id = c.getLong(c.getColumnIndexOrThrow("id")),
            name = c.getString(c.getColumnIndexOrThrow("name")),
            email = c.getString(c.getColumnIndexOrThrow("email")),
            role = c.getString(c.getColumnIndexOrThrow("role")),
            unit = c.getString(c.getColumnIndexOrThrow("unit")),
            createdAt = c.getLong(c.getColumnIndexOrThrow("created_at"))
        )
    }
}
