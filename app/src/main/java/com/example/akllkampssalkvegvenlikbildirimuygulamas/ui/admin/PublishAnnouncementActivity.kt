package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.admin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.campusguardian.session.SessionManager
import com.example.campusguardian.utils.NotificationEngine

class PublishAnnouncementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = SessionManager.getCurrentUser(this)
        if (user == null || user.role != "ADMIN") {
            finish()
            return
        }

        setContentView(R.layout.activity_publish_announcement)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnPublish = findViewById<Button>(R.id.btnPublish)

        btnPublish.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val msg = etMessage.text?.toString()?.trim().orEmpty()

            if (title.length < 3) {
                Toast.makeText(this, "Başlık en az 3 karakter.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (msg.length < 10) {
                Toast.makeText(this, "Mesaj en az 10 karakter.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            NotificationEngine.publishEmergency(
                context = this,
                adminId = user.id,
                title = title,
                message = msg
            )

            Toast.makeText(this, "Acil duyuru yayınlandı.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
