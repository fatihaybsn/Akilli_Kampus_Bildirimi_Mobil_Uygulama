package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.ReportRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.adapters.FollowedReportAdapter
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.report.ReportDetailActivity

class FollowedReportsActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private var adapter: FollowedReportAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = SessionManager.getCurrentUser(this) ?: run { finish(); return }
        setContentView(R.layout.activity_followed_reports)

        recycler = findViewById(R.id.recycler)
        progress = findViewById(R.id.progress)

        recycler.layoutManager = LinearLayoutManager(this)

        load(user.id)
    }

    private fun load(userId: Long) {
        progress.visibility = View.VISIBLE

        val list = ReportRepo(this).listReports(
            ReportRepo.QueryParams(
                onlyFollowedByUserId = userId,
                sortDesc = true
            )
        )

        if (adapter == null) {
            adapter = FollowedReportAdapter(list) { report ->
                val i = Intent(this, ReportDetailActivity::class.java)
                i.putExtra("report_id", report.id)
                startActivity(i)
            }
            recycler.adapter = adapter
        } else {
            adapter?.submit(list)
        }

        progress.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        val user = SessionManager.getCurrentUser(this) ?: return
        load(user.id)
    }
}
