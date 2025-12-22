package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.auth

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.UserRepo

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val spUnit = findViewById<Spinner>(R.id.spUnit)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)

        ArrayAdapter.createFromResource(
            this,
            R.array.units_array,
            android.R.layout.simple_spinner_item
        ).also { ad ->
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spUnit.adapter = ad
        }

        btnRegister.setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val pass = etPassword.text?.toString().orEmpty()
            val unit = spUnit.selectedItem?.toString().orEmpty()

            if (name.length < 3) {
                toast("Ad-soyad en az 3 karakter olmalı.")
                return@setOnClickListener
            }
            if (!email.contains("@") || !email.contains(".")) {
                toast("Geçersiz e-posta.")
                return@setOnClickListener
            }
            if (pass.length < 6) {
                toast("Şifre en az 6 karakter olmalı.")
                return@setOnClickListener
            }
            if (unit.isBlank()) {
                toast("Unit seçimi zorunlu.")
                return@setOnClickListener
            }

            val (id, err) = UserRepo(this).register(name, email, pass, unit)
            if (id == null) {
                toast(err ?: "Kayıt başarısız.")
                return@setOnClickListener
            }

            toast("Kayıt başarılı. Giriş yapabilirsiniz.")
            finish()
        }

        tvBack.setOnClickListener { finish() }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
