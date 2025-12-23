package com.example.akllkampssalkvegvenlikbildirimuygulamas.utils

import android.content.Context
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.NotificationHelper
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.NotificationKind
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.Report
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.AnnouncementRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.FollowRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.NotificationRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.UserPrefsRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.UserRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.session.SessionManager

object NotificationEngine {

    fun handleStatusChange(context: Context, report: Report, newStatusDb: String) {
        val followRepo = FollowRepo(context)
        val prefsRepo = UserPrefsRepo(context)
        val notifRepo = NotificationRepo(context)

        val followerIds = followRepo.getFollowerUserIds(report.id)
        if (followerIds.isEmpty()) return

        val title = "Bildirim durumu güncellendi"
        val body = "\"${report.title}\" → ${UiMappings.statusLabel(newStatusDb)}"

        val current = SessionManager.getCurrentUser(context)

        for (uid in followerIds) {
            val enabled = prefsRepo.isEnabled(uid, report.type)
            if (!enabled) continue

            notifRepo.insertLog(
                userId = uid,
                kind = NotificationKind.STATUS_CHANGE.dbValue,
                refId = report.id,
                title = title,
                body = body
            )

            if (current != null && current.id == uid) {
                NotificationHelper.showLocalNotification(context, title, body)
            }
        }
    }

    fun publishEmergency(context: Context, adminId: Long, title: String, message: String): Long {
        val annRepo = AnnouncementRepo(context)
        val userRepo = UserRepo(context)
        val notifRepo = NotificationRepo(context)

        val nowTitle = "Acil Duyuru: $title"
        val annId = annRepo.createAnnouncement(title = title, message = message, adminId = adminId)

        val userIds = userRepo.getAllUserIds()
        val current = SessionManager.getCurrentUser(context)

        for (uid in userIds) {
            notifRepo.insertLog(
                userId = uid,
                kind = NotificationKind.EMERGENCY.dbValue,
                refId = annId,
                title = nowTitle,
                body = message
            )

            if (current != null && current.id == uid) {
                NotificationHelper.showLocalNotification(context, nowTitle, message)
            }
        }

        return annId
    }
}
