package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.Report
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.ReportRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.adapters.AdminReportAdapter
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.admin.PublishAnnouncementActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.report.ReportDetailActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.NotificationEngine
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.UiMappings

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

        swUnitOnly.isChecked = true // default ON
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
        if (user.role != "ADMIN") return

        progress.visibility = View.VISIBLE

        val repo = ReportRepo(requireContext())
        val list = repo.listReports(
            ReportRepo.QueryParams(
                sortDesc = true,
                includeCreatorInfo = true,
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
        if (!UiMappings.isValidStatusTransition(report.status, newStatus)) {
            Toast.makeText(requireContext(), "Durum sırası geçersiz.", Toast.LENGTH_SHORT).show()
            return
        }

        val repo = ReportRepo(requireContext())
        val ok = repo.updateStatus(report.id, newStatus)
        if (ok) {
            val fresh = repo.getById(report.id)
            if (fresh != null) {
                NotificationEngine.handleStatusChange(requireContext(), fresh, newStatus)
            }
            load()
        }
    }

    private fun confirmTerminate(report: Report) {
        val input = android.widget.EditText(requireContext())
        input.hint = "Kısa admin notu (opsiyonel)"
        input.setText("Bildirim uygunsuz/yanlış olduğu için kapatıldı.")

        AlertDialog.Builder(requireContext())
            .setTitle("Sonlandır")
            .setMessage("Bu bildirimi uygunsuz/yanlış olarak kapatmak istiyor musunuz? (Durum: Çözüldü)")
            .setView(input)
            .setPositiveButton("Evet") { _, _ ->
                val note = input.text?.toString()?.trim().orEmpty().ifEmpty {
                    "Bildirim uygunsuz/yanlış olduğu için kapatıldı."
                }

                val repo = ReportRepo(requireContext())
                val ok = repo.closeAsInvalid(report.id, note)
                if (ok) {
                    val fresh = repo.getById(report.id)
                    if (fresh != null) {
                        NotificationEngine.handleStatusChange(requireContext(), fresh, "RESOLVED")
                    }
                    load()
                } else {
                    Toast.makeText(requireContext(), "İşlem başarısız.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}
