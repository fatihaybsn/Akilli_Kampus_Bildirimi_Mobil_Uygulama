package com.example.akllkampssalkvegvenlikbildirimuygulamas.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CampusDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DbSchema.DB_NAME,
    null,
    DbSchema.DB_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        // 1) Tabloları oluştur
        db.execSQL(DbSchema.CREATE_USERS)
        db.execSQL(DbSchema.CREATE_REPORTS)
        db.execSQL(DbSchema.CREATE_FOLLOWS)
        db.execSQL(DbSchema.CREATE_USER_PREFS)
        db.execSQL(DbSchema.CREATE_ANNOUNCEMENTS)
        db.execSQL(DbSchema.CREATE_NOTIFICATION_LOG)

        // 2) Seed (Bölüm 1’deki seed kodunu buraya çağırıyorsun)
        // seed(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Dönem projesi için en basit yaklaşım:
        // Şema değiştirirsen DB_VERSION artır, burada drop+create yapabilirsin.
        // db.execSQL("DROP TABLE IF EXISTS notification_log")
        // ...
        // onCreate(db)
    }
}
