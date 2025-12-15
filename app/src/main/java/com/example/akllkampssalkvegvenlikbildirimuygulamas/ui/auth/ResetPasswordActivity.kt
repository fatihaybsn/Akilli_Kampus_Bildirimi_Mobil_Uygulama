package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSend = findViewById<Button>(R.id.btnSend)

        btnSend.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            if (email.isBlank() || !email.contains("@")) {
                Toast.makeText(this, "Geçerli bir e-posta girin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // simulation
            startActivity(Intent(this, ResetSentActivity::class.java))
            finish()
        }
    }
}
