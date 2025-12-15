package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.campusguardian.model.Report
import com.example.campusguardian.repo.ReportRepo
import com.example.campusguardian.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.adapters.AdminReportAdapter
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.admin.PublishAnnouncementActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.report.ReportDetailActivity
import com.example.campusguardian.utils.NotificationEngine

class AdminReportsFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var swUnitOnly: SwitchCompat
    private lateinit var btnEmergency: Button

    private var adapter: AdminReportAdapter? = null

    companion object {
        fun newInstance() = AdminReportsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_admin_reports, container, false)

        recycler = v.findViewById(R.id.recycler)
        progress = v.findViewById(R.id.progress)
        swUnitOnly = v.findViewById(R.id.swUnitOnly)
        btnEmergency = v.findViewById(R.id.btnEmergency)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        swUnitOnly.isChecked = true // default ON as required
        swUnitOnly.setOnCheckedChangeListener { _, _ -> load() }

        btnEmergency.setOnClickListener {
            startActivity(Intent(requireContext(), PublishAnnouncementActivity::class.java))
        }

        return v
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        val user = SessionManager.requireUser(requireContext())
        if (user.role != "ADMIN") {
            // Hard guard
            return
        }

        progress.visibility = View.VISIBLE

        val repo = ReportRepo(requireContext())
        val list = repo.listReports(
            ReportRepo.QueryParams(
                sortDesc = true,
                adminUnitOnly = if (swUnitOnly.isChecked) user.unit else null
            )
        )

        if (adapter == null) {
            adapter = AdminReportAdapter(
                list = list,
                onOpenDetail = { report ->
                    val i = Intent(requireContext(), ReportDetailActivity::class.java)
                    i.putExtra("report_id", report.id)
                    startActivity(i)
                },
                onQuickStatus = { report, newStatus ->
                    doQuickStatusUpdate(report, newStatus)
                },
                onTerminate = { report ->
                    confirmTerminate(report)
                }
            )
            recycler.adapter = adapter
        } else {
            adapter?.submit(list)
        }

        progress.visibility = View.GONE
    }

    private fun doQuickStatusUpdate(report: Report, newStatus: String) {
        val repo = ReportRepo(requireContext())
        val ok = repo.updateStatus(report.id, newStatus)
        if (ok) {
            // Fetch fresh report for accurate title/type/unit
            val fresh = repo.getById(report.id)
            if (fresh != null) {
                NotificationEngine.handleStatusChange(requireContext(), fresh, newStatus)
            }
            load()
        }
    }

    private fun confirmTerminate(report: Report) {
        AlertDialog.Builder(requireContext())
            .setTitle("Sonlandır")
            .setMessage("Bu bildirimi uygunsuz/yanlış olarak sonlandırmak istiyor musunuz?")
            .setPositiveButton("Evet") { _, _ ->
                doQuickStatusUpdate(report, "CLOSED_INVALID")
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}
