package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.report

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportType
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.ReportRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.PermissionUtils
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.SettingsManager
import com.google.android.material.textfield.TextInputEditText
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay

class CreateReportActivity : AppCompatActivity() {

    private lateinit var spType: Spinner
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDesc: TextInputEditText
    private lateinit var tvLatLon: TextView
    private lateinit var btnPickPhoto: Button
    private lateinit var btnUseDeviceLoc: Button
    private lateinit var btnSubmit: Button

    private lateinit var map: MapView
    private lateinit var progress: ProgressBar

    private var selectedLat: Double? = null
    private var selectedLon: Double? = null
    private var selectedPhotoUri: String? = null

    private var marker: Marker? = null

    private val pickPhoto = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { /* ignore */ }

        selectedPhotoUri = uri.toString()
        btnPickPhoto.text = "Foto seçildi"
    }

    private val reqLocationPerm = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // after permission, try again
        setFromDeviceLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_report)

        spType = findViewById(R.id.spType)
        etTitle = findViewById(R.id.etTitle)
        etDesc = findViewById(R.id.etDesc)
        tvLatLon = findViewById(R.id.tvLatLon)
        btnPickPhoto = findViewById(R.id.btnPickPhoto)
        btnUseDeviceLoc = findViewById(R.id.btnUseDeviceLoc)
        btnSubmit = findViewById(R.id.btnSubmit)

        map = findViewById(R.id.mapView)
        progress = findViewById(R.id.progress)

        setupTypeSpinner()
        setupMap()

        btnPickPhoto.setOnClickListener {
            pickPhoto.launch(arrayOf("image/*"))
        }

        btnUseDeviceLoc.setOnClickListener {
            setFromDeviceLocation()
        }

        btnSubmit.setOnClickListener {
            submit()
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        map.onPause()
        super.onPause()
    }

    private fun setupTypeSpinner() {
        val items = ReportType.values().map { it.labelTr }
        val ad = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spType.adapter = ad
    }

    private fun setupMap() {
        progress.visibility = View.VISIBLE

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(16.0)

        val rot = RotationGestureOverlay(map)
        rot.isEnabled = true
        map.overlays.add(rot)

        // default center
        val center = GeoPoint(39.9207, 32.8540)
        map.controller.setCenter(center)
        setSelection(center.latitude, center.longitude, userInitiated = false)

        map.setOnTouchListener { _, event ->
            // Let map handle gestures normally; selection via long press is better but simplified:
            false
        }

        // Map üzerinde seçim: uzun basılan noktanın koordinatını al
        val eventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                // tek dokunuşla seçim yapmayacağız
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                if (p == null) return false
                setSelection(p.latitude, p.longitude, userInitiated = true)
                return true
            }
        }
        map.overlays.add(MapEventsOverlay(eventsReceiver))
        progress.visibility = View.GONE
    }

    private fun setSelection(lat: Double, lon: Double, userInitiated: Boolean) {
        selectedLat = lat
        selectedLon = lon
        tvLatLon.text = "Konum: %.6f, %.6f (Haritada seçmek için uzun bas)".format(lat, lon)

        if (marker == null) {
            marker = Marker(map).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Seçilen Konum"
                position = GeoPoint(lat, lon)
            }
            map.overlays.add(marker)
        } else {
            marker?.position = GeoPoint(lat, lon)
        }
        map.invalidate()

        if (userInitiated) maybeWarnBounds(lat, lon)
    }

    private fun maybeWarnBounds(lat: Double, lon: Double) {
        if (!SettingsManager.isCampusBoundsValidationEnabled(this)) return
        val bounds = SettingsManager.getCampusBounds(this)
        if (bounds.contains(lat, lon)) return

        AlertDialog.Builder(this)
            .setTitle("Kampüs dışı konum")
            .setMessage(
                "Seçilen konum kampüs sınırlarının dışında görünüyor.\n" +
                        "Devam etmek istiyor musunuz? (Hard-block yok)"
            )
            .setPositiveButton("Devam") { _, _ -> }
            .setNegativeButton("Vazgeç") { _, _ ->
                // revert to center-ish
                val c = GeoPoint(39.9207, 32.8540)
                map.controller.setCenter(c)
                setSelection(c.latitude, c.longitude, userInitiated = false)
            }
            .show()
    }

    private fun setFromDeviceLocation() {
        val fine = PermissionUtils.hasFineLocation(this)
        val coarse = PermissionUtils.hasCoarseLocation(this)
        if (!fine && !coarse) {
            reqLocationPerm.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            return
        }

        try {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            val loc: Location? =
                (if (fine) lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (loc == null) {
                Toast.makeText(this, "Cihaz konumu alınamadı (lastKnown null).", Toast.LENGTH_SHORT).show()
                return
            }

            val p = GeoPoint(loc.latitude, loc.longitude)
            map.controller.setCenter(p)
            setSelection(p.latitude, p.longitude, userInitiated = true)
        } catch (se: SecurityException) {
            Toast.makeText(this, "Konum izni yok.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Konum hatası: ${e.message ?: "bilinmiyor"}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submit() {
        val user = SessionManager.requireUser(this)

        val typeLabel = spType.selectedItem?.toString().orEmpty()
        val typeDb = ReportType.values().firstOrNull { it.labelTr == typeLabel }?.dbValue ?: "TECHNICAL"

        val title = etTitle.text?.toString()?.trim().orEmpty()
        val desc = etDesc.text?.toString()?.trim().orEmpty()

        if (title.length < 5) {
            toast("Başlık en az 5 karakter olmalı.")
            return
        }
        if (desc.length < 10) {
            toast("Açıklama en az 10 karakter olmalı.")
            return
        }
        val lat = selectedLat
        val lon = selectedLon
        if (lat == null || lon == null) {
            toast("Konum seçimi zorunlu.")
            return
        }

        val id = ReportRepo(this).createReport(
            creator = user,
            typeDb = typeDb,
            title = title,
            description = desc,
            lat = lat,
            lon = lon,
            photoUri = selectedPhotoUri
        )

        if (id <= 0) {
            toast("Kayıt başarısız.")
            return
        }

        toast("Bildirim oluşturuldu.")
        finish()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
