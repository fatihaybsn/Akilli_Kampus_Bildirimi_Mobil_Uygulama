package com.example.akllkampssalkvegvenlikbildirimuygulamas.utils

import android.graphics.Color
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportStatus
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportType

object UiMappings {

    fun typeLabel(typeDb: String): String = ReportType.fromDb(typeDb).labelTr
    fun statusLabel(statusDb: String): String = ReportStatus.fromDb(statusDb).labelTr

    fun responsibleUnit(typeDb: String): String = ReportType.fromDb(typeDb).responsibleUnit

    fun isValidStatusTransition(currentStatusDb: String, newStatusDb: String): Boolean {
        val current = ReportStatus.fromDb(currentStatusDb)
        val next = ReportStatus.fromDb(newStatusDb)

        // No-op always allowed
        if (current == next) return true

        return when (current) {
            ReportStatus.OPEN -> next == ReportStatus.IN_PROGRESS
            ReportStatus.IN_PROGRESS -> next == ReportStatus.RESOLVED
            ReportStatus.RESOLVED -> false
        }
    }

    fun typeIconRes(typeDb: String): Int {
        return when (ReportType.fromDb(typeDb)) {
            ReportType.HEALTH -> android.R.drawable.ic_menu_info_details
            ReportType.SECURITY -> android.R.drawable.ic_lock_lock
            ReportType.ENVIRONMENT -> android.R.drawable.ic_menu_compass
            ReportType.LOST_FOUND -> android.R.drawable.ic_menu_help
            ReportType.TECHNICAL -> android.R.drawable.ic_menu_manage
        }
    }

    fun statusIconRes(statusDb: String): Int {
        return when (ReportStatus.fromDb(statusDb)) {
            ReportStatus.OPEN -> android.R.drawable.presence_away
            ReportStatus.IN_PROGRESS -> android.R.drawable.presence_busy
            ReportStatus.RESOLVED -> android.R.drawable.presence_online
        }
    }

    fun typeColor(typeDb: String): Int {
        return when (ReportType.fromDb(typeDb)) {
            ReportType.HEALTH -> Color.parseColor("#D32F2F")
            ReportType.SECURITY -> Color.parseColor("#455A64")
            ReportType.ENVIRONMENT -> Color.parseColor("#2E7D32")
            ReportType.LOST_FOUND -> Color.parseColor("#6A1B9A")
            ReportType.TECHNICAL -> Color.parseColor("#EF6C00")
        }
    }

    fun statusColor(statusDb: String): Int {
        return when (ReportStatus.fromDb(statusDb)) {
            ReportStatus.OPEN -> Color.parseColor("#D32F2F")
            ReportStatus.IN_PROGRESS -> Color.parseColor("#F9A825")
            ReportStatus.RESOLVED -> Color.parseColor("#2E7D32")
        }
    }
}
