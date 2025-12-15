package com.example.campusguardian.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.campusguardian.repo.NotificationRepo
import com.example.campusguardian.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.adapters.NotificationAdapter

class NotificationInboxActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var swUnread: Switch
    private lateinit var btnMarkAll: Button

    private var adapter: NotificationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = SessionManager.getCurrentUser(this) ?: run { finish(); return }
        setContentView(R.layout.activity_notification_inbox)

        recycler = findViewById(R.id.recycler)
        progress = findViewById(R.id.progress)
        swUnread = findViewById(R.id.swUnreadOnly)
        btnMarkAll = findViewById(R.id.btnMarkAllRead)

        recycler.layoutManager = LinearLayoutManager(this)

        swUnread.setOnCheckedChangeListener { _, _ -> load() }
        btnMarkAll.setOnClickListener {
            NotificationRepo(this).markAllRead(user.id)
            load()
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        val user = SessionManager.getCurrentUser(this) ?: return
        progress.visibility = View.VISIBLE

        val repo = NotificationRepo(this)
        val list = repo.listForUser(user.id, onlyUnread = swUnread.isChecked)

        if (adapter == null) {
            adapter = NotificationAdapter(list,
                onMarkRead = { log ->
                    repo.markRead(log.id)
                    load()
                }
            )
            recycler.adapter = adapter
        } else {
            adapter?.submit(list)
        }

        progress.visibility = View.GONE
    }
}
