package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.report

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.Report
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportStatus
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.FollowRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.ReportRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.NotificationEngine
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.TimeUtils
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.UiMappings
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class ReportDetailActivity : AppCompatActivity() {

    private var reportId: Long = -1

    private lateinit var tvTitle: TextView
    private lateinit var tvType: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvUnit: TextView
    private lateinit var tvCreator: TextView
    private lateinit var tvDesc: TextView

    private lateinit var ivPhoto: ImageView
    private lateinit var map: MapView

    private lateinit var btnFollow: Button

    // Admin controls
    private lateinit var adminPanel: View
    private lateinit var spStatus: Spinner
    private lateinit var etDescEdit: EditText
    private lateinit var btnSaveStatus: Button
    private lateinit var btnSaveDesc: Button
    private lateinit var btnTerminate: Button

    private var report: Report? = null
    private var spinnerOptions: List<ReportStatus> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        reportId = intent.getLongExtra("report_id", -1)
        if (reportId <= 0) {
            finish()
            return
        }

        tvTitle = findViewById(R.id.tvTitle)
        tvType = findViewById(R.id.tvType)
        tvStatus = findViewById(R.id.tvStatus)
        tvTime = findViewById(R.id.tvTime)
        tvUnit = findViewById(R.id.tvUnit)
        tvCreator = findViewById(R.id.tvCreator)
        tvDesc = findViewById(R.id.tvDesc)

        ivPhoto = findViewById(R.id.ivPhoto)
        map = findViewById(R.id.mapView)

        btnFollow = findViewById(R.id.btnFollow)

        adminPanel = findViewById(R.id.adminPanel)
        spStatus = findViewById(R.id.spStatus)
        etDescEdit = findViewById(R.id.etDescEdit)
        btnSaveStatus = findViewById(R.id.btnSaveStatus)
        btnSaveDesc = findViewById(R.id.btnSaveDesc)
        btnTerminate = findViewById(R.id.btnTerminate)

        setupMap()

        val user = SessionManager.requireUser(this)
        adminPanel.visibility = if (user.role == "ADMIN") View.VISIBLE else View.GONE
        btnFollow.visibility = if (user.role == "USER") View.VISIBLE else View.GONE

        btnFollow.setOnClickListener { toggleFollow() }
        btnSaveStatus.setOnClickListener { saveStatus() }
        btnSaveDesc.setOnClickListener { saveDescription() }
        btnTerminate.setOnClickListener { confirmTerminate() }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        load()
    }

    override fun onPause() {
        map.onPause()
        super.onPause()
    }

    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(17.0)
    }

    private fun setStatusSpinnerOptions(currentStatusDb: String) {
        val current = ReportStatus.fromDb(currentStatusDb)

        spinnerOptions = when (current) {
            ReportStatus.OPEN -> listOf(ReportStatus.OPEN, ReportStatus.IN_PROGRESS)
            ReportStatus.IN_PROGRESS -> listOf(ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED)
            ReportStatus.RESOLVED -> listOf(ReportStatus.RESOLVED)
        }

        val labels = spinnerOptions.map { it.labelTr }
        val ad = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStatus.adapter = ad
        spStatus.setSelection(0)
    }

    private fun load() {
        val repo = ReportRepo(this)
        val r = repo.getById(reportId) ?: run {
            finish(); return
        }
        report = r

        tvTitle.text = r.title
        tvType.text = "Tür: ${UiMappings.typeLabel(r.type)}"
        tvStatus.text = "Durum: ${UiMappings.statusLabel(r.status)}"
        tvTime.text = "Zaman: ${TimeUtils.formatDateTime(r.createdAt)} (${TimeUtils.timeAgo(r.createdAt)})"
        tvUnit.text = "Birim: ${r.unit}"

        val creator = when {
            !r.createdByName.isNullOrBlank() && !r.createdByEmail.isNullOrBlank() -> "${r.createdByName} (${r.createdByEmail})"
            !r.createdByName.isNullOrBlank() -> r.createdByName
            !r.createdByEmail.isNullOrBlank() -> r.createdByEmail
            else -> "Kullanıcı #${r.createdByUserId}"
        }
        tvCreator.text = "Bildiren: $creator"

        tvDesc.text = r.description

        if (!r.photoUri.isNullOrBlank()) {
            ivPhoto.visibility = View.VISIBLE
            try {
                ivPhoto.setImageURI(android.net.Uri.parse(r.photoUri))
            } catch (_: Exception) {
                ivPhoto.visibility = View.GONE
            }
        } else {
            ivPhoto.visibility = View.GONE
        }

        // map marker
        map.overlays.clear()
        val p = GeoPoint(r.lat, r.lon)
        map.controller.setCenter(p)
        val m = Marker(map)
        m.position = p
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        m.title = r.title
        map.overlays.add(m)
        map.invalidate()

        val user = SessionManager.requireUser(this)
        if (user.role == "USER") {
            updateFollowButton()
        } else {
            setStatusSpinnerOptions(r.status)
            etDescEdit.setText(r.description)

            val current = ReportStatus.fromDb(r.status)
            val locked = (current == ReportStatus.RESOLVED)
            spStatus.isEnabled = !locked
            btnSaveStatus.isEnabled = !locked
            btnTerminate.isEnabled = !locked
        }
    }

    private fun updateFollowButton() {
        val user = SessionManager.requireUser(this)
        val followed = FollowRepo(this).isFollowed(user.id, reportId)
        btnFollow.text = if (followed) "Takibi Bırak" else "Takip Et"
    }

    private fun toggleFollow() {
        val user = SessionManager.requireUser(this)
        val repo = FollowRepo(this)
        val followed = repo.isFollowed(user.id, reportId)
        val ok = if (followed) repo.unfollow(user.id, reportId) else repo.follow(user.id, reportId)
        if (ok) {
            updateFollowButton()
            Toast.makeText(this, if (followed) "Takip bırakıldı." else "Takip edildi.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStatus() {
        val user = SessionManager.requireUser(this)
        if (user.role != "ADMIN") return

        val r = report ?: return

        val selectedLabel = spStatus.selectedItem?.toString().orEmpty()
        val selected = spinnerOptions.firstOrNull { it.labelTr == selectedLabel } ?: return
        val newStatus = selected.dbValue

        if (!UiMappings.isValidStatusTransition(r.status, newStatus)) {
            Toast.makeText(this, "Durum sırası geçersiz. Önce 'İnceleniyor', sonra 'Çözüldü'.", Toast.LENGTH_SHORT).show()
            return
        }

        val ok = ReportRepo(this).updateStatus(reportId, newStatus)
        if (ok) {
            val fresh = ReportRepo(this).getById(reportId)
            if (fresh != null) {
                NotificationEngine.handleStatusChange(this, fresh, newStatus)
            }
            Toast.makeText(this, "Durum güncellendi.", Toast.LENGTH_SHORT).show()
            load()
        } else {
            Toast.makeText(this, "Durum güncellenemedi.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDescription() {
        val user = SessionManager.requireUser(this)
        if (user.role != "ADMIN") return

        val desc = etDescEdit.text?.toString()?.trim().orEmpty()
        if (desc.length < 10) {
            Toast.makeText(this, "Açıklama en az 10 karakter.", Toast.LENGTH_SHORT).show()
            return
        }

        val ok = ReportRepo(this).updateDescription(reportId, desc)
        Toast.makeText(this, if (ok) "Açıklama güncellendi." else "Güncelleme başarısız.", Toast.LENGTH_SHORT).show()
        if (ok) load()
    }

    private fun confirmTerminate() {
        val user = SessionManager.requireUser(this)
        if (user.role != "ADMIN") return

        val input = EditText(this)
        input.hint = "Kısa admin notu (opsiyonel)"
        input.setText("Bildirim uygunsuz/yanlış olduğu için kapatıldı.")

        AlertDialog.Builder(this)
            .setTitle("Sonlandır")
            .setMessage("Bu bildirimi uygunsuz/yanlış olarak kapatmak istiyor musunuz? (Durum: Çözüldü)")
            .setView(input)
            .setPositiveButton("Evet") { _, _ ->
                val note = input.text?.toString()?.trim().orEmpty().ifEmpty {
                    "Bildirim uygunsuz/yanlış olduğu için kapatıldı."
                }

                val repo = ReportRepo(this)
                val ok = repo.closeAsInvalid(reportId, note)
                if (ok) {
                    val fresh = repo.getById(reportId)
                    if (fresh != null) {
                        NotificationEngine.handleStatusChange(this, fresh, "RESOLVED")
                    }
                    Toast.makeText(this, "Bildirim kapatıldı.", Toast.LENGTH_SHORT).show()
                    load()
                } else {
                    Toast.makeText(this, "İşlem başarısız.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}
