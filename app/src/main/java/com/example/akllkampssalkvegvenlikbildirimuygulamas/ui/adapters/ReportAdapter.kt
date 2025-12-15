package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.campusguardian.model.Report
import com.example.campusguardian.utils.TimeUtils
import com.example.campusguardian.utils.UiMappings

class ReportAdapter(
    private var list: List<Report>,
    private val onClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportAdapter.VH>() {

    fun submit(newList: List<Report>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_report, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = list[position]
        holder.bind(r, onClick)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivType = itemView.findViewById<ImageView>(R.id.ivType)
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        private val tvDesc = itemView.findViewById<TextView>(R.id.tvDesc)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        private val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)

        fun bind(r: Report, onClick: (Report) -> Unit) {
            val icon = UiMappings.typeIconRes(r.type)
            ivType.setImageResource(icon)
            ivType.setColorFilter(UiMappings.typeColor(r.type))

            tvTitle.text = r.title
            tvDesc.text = if (r.description.length > 80) r.description.take(80) + "…" else r.description
            tvTime.text = TimeUtils.timeAgo(r.createdAt)
            tvStatus.text = UiMappings.statusLabel(r.status)
            tvStatus.setTextColor(UiMappings.statusColor(r.status))

            itemView.setOnClickListener { onClick(r) }
        }
    }
}
