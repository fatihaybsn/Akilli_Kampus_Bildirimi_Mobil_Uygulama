package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.profile

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportType
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.UserPrefsRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.CampusBounds
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.SettingsManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsContainer: LinearLayout
    private lateinit var swBounds: Switch
    private lateinit var etMinLat: EditText
    private lateinit var etMaxLat: EditText
    private lateinit var etMinLon: EditText
    private lateinit var etMaxLon: EditText
    private lateinit var btnSaveBounds: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = SessionManager.getCurrentUser(this) ?: run { finish(); return }
        setContentView(R.layout.activity_settings)

        prefsContainer = findViewById(R.id.prefsContainer)
        swBounds = findViewById(R.id.swBounds)
        etMinLat = findViewById(R.id.etMinLat)
        etMaxLat = findViewById(R.id.etMaxLat)
        etMinLon = findViewById(R.id.etMinLon)
        etMaxLon = findViewById(R.id.etMaxLon)
        btnSaveBounds = findViewById(R.id.btnSaveBounds)

        val repo = UserPrefsRepo(this)
        val currentPrefs = repo.getAllPrefs(user.id)

        prefsContainer.removeAllViews()
        for (t in ReportType.values()) {
            val row = layoutInflater.inflate(R.layout.row_pref_toggle, prefsContainer, false)
            val tv = row.findViewById<TextView>(R.id.tvLabel)
            val sw = row.findViewById<Switch>(R.id.swEnabled)

            tv.text = t.labelTr
            sw.isChecked = currentPrefs[t.dbValue] == true

            sw.setOnCheckedChangeListener { _, isChecked ->
                repo.setEnabled(user.id, t.dbValue, isChecked)
            }

            prefsContainer.addView(row)
        }

        // Bounds settings
        swBounds.isChecked = SettingsManager.isCampusBoundsValidationEnabled(this)
        swBounds.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setCampusBoundsValidationEnabled(this, isChecked)
        }

        val b = SettingsManager.getCampusBounds(this)
        etMinLat.setText(b.minLat.toString())
        etMaxLat.setText(b.maxLat.toString())
        etMinLon.setText(b.minLon.toString())
        etMaxLon.setText(b.maxLon.toString())

        btnSaveBounds.setOnClickListener {
            val minLat = etMinLat.text?.toString()?.toDoubleOrNull()
            val maxLat = etMaxLat.text?.toString()?.toDoubleOrNull()
            val minLon = etMinLon.text?.toString()?.toDoubleOrNull()
            val maxLon = etMaxLon.text?.toString()?.toDoubleOrNull()

            if (minLat == null || maxLat == null || minLon == null || maxLon == null) {
                toast("Bounds değerleri geçersiz.")
                return@setOnClickListener
            }
            if (minLat >= maxLat || minLon >= maxLon) {
                toast("Min değerler max değerlerden küçük olmalı.")
                return@setOnClickListener
            }

            SettingsManager.setCampusBounds(this, CampusBounds(minLat, maxLat, minLon, maxLon))
            toast("Kampüs sınırları kaydedildi.")
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
