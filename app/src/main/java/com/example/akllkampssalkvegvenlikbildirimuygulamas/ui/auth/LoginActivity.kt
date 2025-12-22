package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.UserRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If already logged in, go to Main
        val current = SessionManager.getCurrentUser(this)
        if (current != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvReset = findViewById<TextView>(R.id.tvReset)

        btnLogin.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val pass = etPassword.text?.toString().orEmpty()

            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "E-posta ve şifre zorunlu.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = UserRepo(this).login(email, pass)
            if (user == null) {
                Toast.makeText(this, "Hatalı e-posta veya şifre.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            SessionManager.login(this, user)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvReset.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }
}
