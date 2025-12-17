package com.example.akllkampssalkvegvenlikbildirimuygulamas.db

import android.content.Context

object DbProvider {
    @Volatile private var helper: DatabaseHelper? = null

    fun get(context: Context): DatabaseHelper {
        return helper ?: synchronized(this) {
            helper ?: DatabaseHelper(context.applicationContext).also { helper = it }
        }
    }
}
