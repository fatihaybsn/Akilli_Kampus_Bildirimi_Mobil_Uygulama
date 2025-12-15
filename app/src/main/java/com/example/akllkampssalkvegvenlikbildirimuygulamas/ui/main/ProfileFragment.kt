package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.campusguardian.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.auth.LoginActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.profile.FollowedReportsActivity
import com.example.campusguardian.ui.profile.NotificationInboxActivity
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.profile.SettingsActivity

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)

        val tvName = v.findViewById<TextView>(R.id.tvName)
        val tvEmail = v.findViewById<TextView>(R.id.tvEmail)
        val tvRole = v.findViewById<TextView>(R.id.tvRole)
        val tvUnit = v.findViewById<TextView>(R.id.tvUnit)

        val btnSettings = v.findViewById<Button>(R.id.btnSettings)
        val btnFollowed = v.findViewById<Button>(R.id.btnFollowed)
        val btnInbox = v.findViewById<Button>(R.id.btnInbox)
        val btnLogout = v.findViewById<Button>(R.id.btnLogout)

        val user = SessionManager.requireUser(requireContext())
        tvName.text = user.name
        tvEmail.text = user.email
        tvRole.text = "Role: ${user.role}"
        tvUnit.text = "Unit: ${user.unit}"

        btnSettings.setOnClickListener { startActivity(Intent(requireContext(), SettingsActivity::class.java)) }
        btnFollowed.setOnClickListener { startActivity(Intent(requireContext(), FollowedReportsActivity::class.java)) }
        btnInbox.setOnClickListener { startActivity(Intent(requireContext(), NotificationInboxActivity::class.java)) }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Çıkış Yap")
                .setMessage("Oturumu kapatmak istiyor musunuz?")
                .setPositiveButton("Evet") { _, _ ->
                    SessionManager.logout(requireContext())
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        return v
    }
}
