package com.example.akllkampssalkvegvenlikbildirimuygulamas.db

object DbSchema {
    const val DB_NAME = "campus_guardian.db"
    const val DB_VERSION = 1

    // --- CREATE TABLE SQL (ŞEMA: birebir) ---
    val CREATE_USERS = """
        CREATE TABLE users(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            email TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            role TEXT NOT NULL,
            unit TEXT NOT NULL,
            created_at INTEGER NOT NULL
        );
    """.trimIndent()

    val CREATE_REPORTS = """
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
    """.trimIndent()

    val CREATE_FOLLOWS = """
        CREATE TABLE follows(
            user_id INTEGER NOT NULL,
            report_id INTEGER NOT NULL,
            created_at INTEGER NOT NULL,
            PRIMARY KEY(user_id, report_id)
        );
    """.trimIndent()

    val CREATE_USER_PREFS = """
        CREATE TABLE user_prefs(
            user_id INTEGER NOT NULL,
            type TEXT NOT NULL,
            enabled INTEGER NOT NULL,
            PRIMARY KEY(user_id, type)
        );
    """.trimIndent()

    val CREATE_ANNOUNCEMENTS = """
        CREATE TABLE announcements(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            message TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            created_by_admin_id INTEGER NOT NULL,
            active INTEGER NOT NULL
        );
    """.trimIndent()

    val CREATE_NOTIFICATION_LOG = """
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
    """.trimIndent()
}
