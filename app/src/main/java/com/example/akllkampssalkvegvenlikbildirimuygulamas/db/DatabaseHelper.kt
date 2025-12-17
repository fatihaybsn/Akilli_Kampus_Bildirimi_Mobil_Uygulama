package com.example.akllkampssalkvegvenlikbildirimuygulamas.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportStatus
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportType
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.HashUtils

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "campus_guardian.db"
        const val DB_VERSION = 1

        private const val CREATE_USERS = """
            CREATE TABLE users(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL,
              email TEXT NOT NULL UNIQUE,
              password_hash TEXT NOT NULL,
              role TEXT NOT NULL,
              unit TEXT NOT NULL,
              created_at INTEGER NOT NULL
            );
        """

        private const val CREATE_REPORTS = """
            CREATE TABLE reports(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              type TEXT NOT NULL,
              title TEXT NOT NULL,
              description TEXT NOT NULL,
              status TEXT NOT NULL,
              lat REAL NOT NULL,
              lon REAL NOT NULL,
              photo_uri TEXT,
              created_by_user_id INTEGER NOT NULL,
              unit TEXT NOT NULL,
              created_at INTEGER NOT NULL,
              updated_at INTEGER NOT NULL
            );
        """

        private const val CREATE_FOLLOWS = """
            CREATE TABLE follows(
              user_id INTEGER NOT NULL,
              report_id INTEGER NOT NULL,
              created_at INTEGER NOT NULL,
              PRIMARY KEY(user_id, report_id)
            );
        """

        private const val CREATE_USER_PREFS = """
            CREATE TABLE user_prefs(
              user_id INTEGER NOT NULL,
              type TEXT NOT NULL,
              enabled INTEGER NOT NULL,
              PRIMARY KEY(user_id, type)
            );
        """

        private const val CREATE_ANNOUNCEMENTS = """
            CREATE TABLE announcements(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              title TEXT NOT NULL,
              message TEXT NOT NULL,
              created_at INTEGER NOT NULL,
              created_by_admin_id INTEGER NOT NULL,
              active INTEGER NOT NULL
            );
        """

        private const val CREATE_NOTIFICATION_LOG = """
            CREATE TABLE notification_log(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              user_id INTEGER NOT NULL,
              kind TEXT NOT NULL,
              ref_id INTEGER,
              title TEXT NOT NULL,
              body TEXT NOT NULL,
              created_at INTEGER NOT NULL,
              read_at INTEGER
            );
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USERS)
        db.execSQL(CREATE_REPORTS)
        db.execSQL(CREATE_FOLLOWS)
        db.execSQL(CREATE_USER_PREFS)
        db.execSQL(CREATE_ANNOUNCEMENTS)
        db.execSQL(CREATE_NOTIFICATION_LOG)

        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Course project strategy: destructive migration
        db.execSQL("DROP TABLE IF EXISTS notification_log")
        db.execSQL("DROP TABLE IF EXISTS announcements")
        db.execSQL("DROP TABLE IF EXISTS user_prefs")
        db.execSQL("DROP TABLE IF EXISTS follows")
        db.execSQL("DROP TABLE IF EXISTS reports")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    private fun seedData(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            val now = System.currentTimeMillis()

            val adminId = insertUser(
                db,
                name = "Admin Güvenlik",
                email = "admin@campus.edu",
                passwordPlain = "Admin123",
                role = "ADMIN",
                unit = "Güvenlik",
                createdAt = now - 10_000
            )

            val user1Id = insertUser(
                db,
                name = "Ali Öğrenci",
                email = "ali@student.edu",
                passwordPlain = "User123",
                role = "USER",
                unit = "Sağlık",
                createdAt = now - 20_000
            )

            val user2Id = insertUser(
                db,
                name = "Ayşe Öğrenci",
                email = "ayse@student.edu",
                passwordPlain = "User123",
                role = "USER",
                unit = "Teknik",
                createdAt = now - 30_000
            )

            val user3Id = insertUser(
                db,
                name = "Mehmet Öğrenci",
                email = "mehmet@student.edu",
                passwordPlain = "User123",
                role = "USER",
                unit = "Çevre",
                createdAt = now - 40_000
            )

            seedPrefs(db, adminId)
            seedPrefs(db, user1Id)
            seedPrefs(db, user2Id)
            seedPrefs(db, user3Id)

            // Sample reports (5-10)
            val r1 = insertReport(
                db,
                type = ReportType.SECURITY,
                title = "Şüpheli kişi bildirimi",
                description = "Kütüphane çevresinde şüpheli hareketler.",
                status = ReportStatus.OPEN,
                lat = 39.9207,
                lon = 32.8540,
                photoUri = null,
                createdByUserId = user1Id,
                unit = ReportType.SECURITY.responsibleUnit,
                createdAt = now - 2 * 60 * 60 * 1000L
            )

            val r2 = insertReport(
                db,
                type = ReportType.HEALTH,
                title = "Yaralanma / ilk yardım ihtiyacı",
                description = "Spor salonu girişinde düşme sonucu yaralanma.",
                status = ReportStatus.IN_PROGRESS,
                lat = 39.9212,
                lon = 32.8532,
                photoUri = null,
                createdByUserId = user2Id,
                unit = ReportType.HEALTH.responsibleUnit,
                createdAt = now - 5 * 60 * 60 * 1000L
            )

            val r3 = insertReport(
                db,
                type = ReportType.ENVIRONMENT,
                title = "Çöp taşması",
                description = "Yemekhane arkasındaki çöp kutuları taşmış.",
                status = ReportStatus.RESOLVED,
                lat = 39.9199,
                lon = 32.8551,
                photoUri = null,
                createdByUserId = user3Id,
                unit = ReportType.ENVIRONMENT.responsibleUnit,
                createdAt = now - 26 * 60 * 60 * 1000L
            )

            val r4 = insertReport(
                db,
                type = ReportType.TECHNICAL,
                title = "Aydınlatma arızası",
                description = "B blok koridor ışıkları yanmıyor.",
                status = ReportStatus.OPEN,
                lat = 39.9202,
                lon = 32.8521,
                photoUri = null,
                createdByUserId = user2Id,
                unit = ReportType.TECHNICAL.responsibleUnit,
                createdAt = now - 40 * 60 * 1000L
            )

            val r5 = insertReport(
                db,
                type = ReportType.LOST_FOUND,
                title = "Kayıp öğrenci kartı",
                description = "Kampüs içi öğrenci kartı bulunursa güvenliğe teslim edilsin.",
                status = ReportStatus.RESOLVED,
                lat = 39.9220,
                lon = 32.8559,
                photoUri = null,
                createdByUserId = user1Id,
                unit = ReportType.LOST_FOUND.responsibleUnit,
                createdAt = now - 3 * 24 * 60 * 60 * 1000L
            )

            // Seed follows for testing notification flow
            insertFollow(db, user1Id, r4, now - 5_000)
            insertFollow(db, user2Id, r1, now - 6_000)
            insertFollow(db, user3Id, r1, now - 7_000)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun insertUser(
        db: SQLiteDatabase,
        name: String,
        email: String,
        passwordPlain: String,
        role: String,
        unit: String,
        createdAt: Long
    ): Long {
        val cv = ContentValues()
        cv.put("name", name)
        cv.put("email", email)
        cv.put("password_hash", HashUtils.sha256(passwordPlain))
        cv.put("role", role)
        cv.put("unit", unit)
        cv.put("created_at", createdAt)
        return db.insert("users", null, cv)
    }

    private fun seedPrefs(db: SQLiteDatabase, userId: Long) {
        for (t in ReportType.values()) {
            val cv = ContentValues()
            cv.put("user_id", userId)
            cv.put("type", t.dbValue)
            cv.put("enabled", 1)
            db.insert("user_prefs", null, cv)
        }
    }

    private fun insertReport(
        db: SQLiteDatabase,
        type: ReportType,
        title: String,
        description: String,
        status: ReportStatus,
        lat: Double,
        lon: Double,
        photoUri: String?,
        createdByUserId: Long,
        unit: String,
        createdAt: Long
    ): Long {
        val cv = ContentValues()
        cv.put("type", type.dbValue)
        cv.put("title", title)
        cv.put("description", description)
        cv.put("status", status.dbValue)
        cv.put("lat", lat)
        cv.put("lon", lon)
        if (photoUri != null) cv.put("photo_uri", photoUri)
        cv.put("created_by_user_id", createdByUserId)
        cv.put("unit", unit)
        cv.put("created_at", createdAt)
        cv.put("updated_at", createdAt)
        return db.insert("reports", null, cv)
    }

    private fun insertFollow(db: SQLiteDatabase, userId: Long, reportId: Long, createdAt: Long) {
        val cv = ContentValues()
        cv.put("user_id", userId)
        cv.put("report_id", reportId)
        cv.put("created_at", createdAt)
        db.insert("follows", null, cv)
    }
}
