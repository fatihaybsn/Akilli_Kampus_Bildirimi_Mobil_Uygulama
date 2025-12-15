package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.campusguardian.model.Report
import com.example.campusguardian.utils.TimeUtils
import com.example.campusguardian.utils.UiMappings

class AdminReportAdapter(
    private var list: List<Report>,
    private val onOpenDetail: (Report) -> Unit,
    private val onQuickStatus: (Report, String) -> Unit,
    private val onTerminate: (Report) -> Unit
) : RecyclerView.Adapter<AdminReportAdapter.VH>() {

    fun submit(newList: List<Report>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_admin_report, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position], onOpenDetail, onQuickStatus, onTerminate)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivType = itemView.findViewById<ImageView>(R.id.ivType)
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        private val tvUnit = itemView.findViewById<TextView>(R.id.tvUnit)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        private val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)

        private val btnInProgress = itemView.findViewById<Button>(R.id.btnInProgress)
        private val btnResolved = itemView.findViewById<Button>(R.id.btnResolved)
        private val btnTerminate = itemView.findViewById<Button>(R.id.btnTerminate)

        fun bind(
            r: Report,
            onOpenDetail: (Report) -> Unit,
            onQuickStatus: (Report, String) -> Unit,
            onTerminate: (Report) -> Unit
        ) {
            ivType.setImageResource(UiMappings.typeIconRes(r.type))
            ivType.setColorFilter(UiMappings.typeColor(r.type))

            tvTitle.text = r.title
            tvUnit.text = "Unit: ${r.unit}"
            tvTime.text = TimeUtils.timeAgo(r.createdAt)
            tvStatus.text = UiMappings.statusLabel(r.status)
            tvStatus.setTextColor(UiMappings.statusColor(r.status))

            itemView.setOnClickListener { onOpenDetail(r) }

            btnInProgress.setOnClickListener { onQuickStatus(r, "IN_PROGRESS") }
            btnResolved.setOnClickListener { onQuickStatus(r, "RESOLVED") }
            btnTerminate.setOnClickListener { onTerminate(r) }
        }
    }
}
