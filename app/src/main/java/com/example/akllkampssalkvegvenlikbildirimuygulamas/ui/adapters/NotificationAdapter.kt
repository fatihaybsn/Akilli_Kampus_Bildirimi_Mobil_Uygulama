package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.campusguardian.model.NotificationLog
import com.example.campusguardian.utils.TimeUtils

class NotificationAdapter(
    private var list: List<NotificationLog>,
    private val onMarkRead: (NotificationLog) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.VH>() {

    fun submit(newList: List<NotificationLog>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_notification, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position], onMarkRead)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        private val tvBody = itemView.findViewById<TextView>(R.id.tvBody)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        private val tvState = itemView.findViewById<TextView>(R.id.tvState)
        private val btnRead = itemView.findViewById<Button>(R.id.btnMarkRead)

        fun bind(n: NotificationLog, onMarkRead: (NotificationLog) -> Unit) {
            tvTitle.text = n.title
            tvBody.text = n.body
            tvTime.text = TimeUtils.timeAgo(n.createdAt)

            val unread = (n.readAt == null)
            tvState.text = if (unread) "Okunmadı" else "Okundu"
            btnRead.visibility = if (unread) View.VISIBLE else View.GONE

            btnRead.setOnClickListener { onMarkRead(n) }
        }
    }
}
