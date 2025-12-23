package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.ReportRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.map.ReportMarkerInfoWindow
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.report.ReportDetailActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.UiMappings
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var progress: ProgressBar

    companion object {
        fun newInstance() = MapFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_map, container, false)
        map = v.findViewById(R.id.mapView)
        progress = v.findViewById(R.id.progress)
        setupMap()
        return v
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        loadPins()
    }

    override fun onPause() {
        map.onPause()
        super.onPause()
    }

    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(16.0)

        // center roughly on seeded points
        map.controller.setCenter(GeoPoint(39.9207, 32.8540))
    }

    private fun loadPins() {
        progress.visibility = View.VISIBLE
        map.overlays.clear()

        val repo = ReportRepo(requireContext())
        val list = repo.listReports(ReportRepo.QueryParams(sortDesc = true))

        for (r in list) {
            val m = Marker(map)
            m.position = GeoPoint(r.lat, r.lon)
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            m.title = r.title

            val icon = ContextCompat.getDrawable(requireContext(), UiMappings.typeIconRes(r.type))
            if (icon != null) {
                icon.setTint(UiMappings.typeColor(r.type))
                m.icon = icon
            }

            val infoWindow = ReportMarkerInfoWindow(
                mapView = map,
                report = r,
                onDetail = {
                    val i = Intent(requireContext(), ReportDetailActivity::class.java)
                    i.putExtra("report_id", r.id)
                    startActivity(i)
                }
            )
            m.infoWindow = infoWindow

            m.setOnMarkerClickListener { marker, _ ->
                marker.showInfoWindow()
                true
            }

            map.overlays.add(m)
        }

        map.invalidate()
        progress.visibility = View.GONE
    }
}
