package com.example.campusguardian.model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: String, // USER | ADMIN
    val unit: String,
    val createdAt: Long
)

data class Report(
    val id: Long,
    val type: String,   // HEALTH | SECURITY | ENVIRONMENT | LOST_FOUND | TECHNICAL
    val title: String,
    val description: String,
    val status: String, // OPEN | IN_PROGRESS | RESOLVED | CLOSED_INVALID
    val lat: Double,
    val lon: Double,
    val photoUri: String?,
    val createdByUserId: Long,
    val unit: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class Announcement(
    val id: Long,
    val title: String,
    val message: String,
    val createdAt: Long,
    val createdByAdminId: Long,
    val active: Int // 0/1
)

data class NotificationLog(
    val id: Long,
    val userId: Long,
    val kind: String, // STATUS_CHANGE | EMERGENCY
    val refId: Long?,
    val title: String,
    val body: String,
    val createdAt: Long,
    val readAt: Long?
)

enum class ReportType(val dbValue: String, val labelTr: String) {
    HEALTH("HEALTH", "Sağlık"),
    SECURITY("SECURITY", "Güvenlik"),
    ENVIRONMENT("ENVIRONMENT", "Çevre"),
    LOST_FOUND("LOST_FOUND", "Kayıp / Buluntu"),
    TECHNICAL("TECHNICAL", "Teknik");

    companion object {
        fun fromDb(v: String): ReportType {
            return values().firstOrNull { it.dbValue == v } ?: TECHNICAL
        }
    }
}

enum class ReportStatus(val dbValue: String, val labelTr: String) {
    OPEN("OPEN", "Açık"),
    IN_PROGRESS("IN_PROGRESS", "İnceleniyor"),
    RESOLVED("RESOLVED", "Çözüldü"),
    CLOSED_INVALID("CLOSED_INVALID", "Sonlandırıldı");

    companion object {
        fun fromDb(v: String): ReportStatus {
            return values().firstOrNull { it.dbValue == v } ?: OPEN
        }
    }
}

enum class NotificationKind(val dbValue: String) {
    STATUS_CHANGE("STATUS_CHANGE"),
    EMERGENCY("EMERGENCY")
}