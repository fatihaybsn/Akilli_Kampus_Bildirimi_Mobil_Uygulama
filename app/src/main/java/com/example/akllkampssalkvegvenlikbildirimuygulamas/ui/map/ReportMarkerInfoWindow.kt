package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.map

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.campusguardian.model.Report
import com.example.campusguardian.utils.TimeUtils
import com.example.campusguardian.utils.UiMappings
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class ReportMarkerInfoWindow(
    mapView: MapView,
    private val report: Report,
    private val onDetail: () -> Unit
) : InfoWindow(R.layout.map_info_window, mapView) {

    override fun onOpen(item: Any?) {
        val tvType = mView.findViewById<TextView>(R.id.tvType)
        val tvTitle = mView.findViewById<TextView>(R.id.tvTitle)
        val tvTime = mView.findViewById<TextView>(R.id.tvTime)
        val btn = mView.findViewById<Button>(R.id.btnDetail)

        tvType.text = UiMappings.typeLabel(report.type)
        tvTitle.text = report.title
        tvTime.text = TimeUtils.timeAgo(report.createdAt)

        btn.setOnClickListener { onDetail() }
    }

    override fun onClose() {
        // no-op
    }
}
