package com.example.akllkampssalkvegvenlikbildirimuygulamas.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object TimeUtils {

    fun timeAgo(whenMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val diff = nowMillis - whenMillis
        val past = diff >= 0
        val d = abs(diff)

        val minute = 60_000L
        val hour = 60 * minute
        val day = 24 * hour

        val base = when {
            d < 10_000L -> "şimdi"
            d < minute -> "${d / 1000} sn"
            d < hour -> "${d / minute} dk"
            d < day -> "${d / hour} saat"
            else -> "${d / day} gün"
        }

        return if (base == "şimdi") base else if (past) "$base önce" else "$base sonra"
    }

    fun formatDateTime(millis: Long): String {
        val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))
        return fmt.format(Date(millis))
    }
}
