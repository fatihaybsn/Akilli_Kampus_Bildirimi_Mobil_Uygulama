package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.auth.LoginActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main.MapFragment
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main.ProfileFragment
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.report.CreateReportActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.utils.PermissionUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var isAdmin = false

    private val reqNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ignore */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = SessionManager.getCurrentUser(this)
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        isAdmin = (user.role == "ADMIN")

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.menu.clear()
        bottom.inflateMenu(if (isAdmin) R.menu.bottom_nav_admin else R.menu.bottom_nav_user)

        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> openFragment(FeedFragment.newInstance())
                R.id.nav_map -> openFragment(MapFragment.newInstance())
                R.id.nav_admin -> openFragment(AdminReportsFragment.newInstance())
                R.id.nav_profile -> openFragment(ProfileFragment.newInstance())
                else -> return@setOnItemSelectedListener false
            }
            true
        }

        if (savedInstanceState == null) {
            bottom.selectedItemId = R.id.nav_feed
        }

        val fab = findViewById<FloatingActionButton>(R.id.fabCreate)
        fab.setOnClickListener {
            startActivity(Intent(this, CreateReportActivity::class.java))
        }

        // Ask notification permission on Android 13+ (optional)
        if (PermissionUtils.needsNotificationPermission() &&
            !PermissionUtils.hasPostNotifications(this)
        ) {
            reqNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openFragment(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, f)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_top_menu, menu)
        val itemAdmin = menu.findItem(R.id.action_open_admin)
        itemAdmin.isVisible = isAdmin
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_admin -> {
                if (isAdmin) {
                    openFragment(AdminReportsFragment.newInstance())
                    Toast.makeText(this, "Admin panel açıldı.", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
