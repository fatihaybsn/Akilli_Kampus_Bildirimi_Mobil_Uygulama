package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.auth

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R

class ResetSentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_sent)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}
