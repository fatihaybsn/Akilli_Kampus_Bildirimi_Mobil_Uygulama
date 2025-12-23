package com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akllkampssalkvegvenlikbildirimuygulamas.R
import com.example.akllkampssalkvegvenlikbildirimuygulamas.model.ReportType
import com.example.akllkampssalkvegvenlikbildirimuygulamas.repo.ReportRepo
import com.example.akllkampssalkvegvenlikbildirimuygulamas.session.SessionManager
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.adapters.ReportAdapter
import com.example.akllkampssalkvegvenlikbildirimuygulamas.ui.report.ReportDetailActivity

class FeedFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar

    private lateinit var spType: Spinner
    private lateinit var swOnlyOpen: SwitchCompat
    private lateinit var swOnlyFollowed: SwitchCompat
    private lateinit var swSortDesc: SwitchCompat
    private lateinit var swAdminUnitOnly: SwitchCompat
    private lateinit var etSearch: EditText

    private var adapter: ReportAdapter? = null

    companion object {
        fun newInstance() = FeedFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_feed, container, false)

        recycler = v.findViewById(R.id.recycler)
        progress = v.findViewById(R.id.progress)

        spType = v.findViewById(R.id.spType)
        swOnlyOpen = v.findViewById(R.id.swOnlyOpen)
        swOnlyFollowed = v.findViewById(R.id.swOnlyFollowed)
        swSortDesc = v.findViewById(R.id.swSortDesc)
        swAdminUnitOnly = v.findViewById(R.id.swAdminUnitOnly)
        etSearch = v.findViewById(R.id.etSearch)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        setupTypeSpinner()
        setupListeners()

        return v
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun setupTypeSpinner() {
        val items = ArrayList<String>()
        items.add("Tümü")
        for (t in ReportType.values()) items.add(t.labelTr)

        val ad = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spType.adapter = ad
        spType.setSelection(0)
    }

    private fun setupListeners() {
        val user = SessionManager.requireUser(requireContext())
        swAdminUnitOnly.visibility = if (user.role == "ADMIN") View.VISIBLE else View.GONE
        swAdminUnitOnly.isChecked = false

        val commonReload = CompoundButton.OnCheckedChangeListener { _, _ -> load() }
        swOnlyOpen.setOnCheckedChangeListener(commonReload)
        swOnlyFollowed.setOnCheckedChangeListener(commonReload)
        swSortDesc.setOnCheckedChangeListener(commonReload)
        swAdminUnitOnly.setOnCheckedChangeListener(commonReload)

        spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) = load()
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = load()
        })
    }

    private fun load() {
        progress.visibility = View.VISIBLE

        val user = SessionManager.requireUser(requireContext())
        val repo = ReportRepo(requireContext())

        val selectedType = spType.selectedItem?.toString().orEmpty()
        val typeDb = if (selectedType == "Tümü") null else ReportType.values().firstOrNull { it.labelTr == selectedType }?.dbValue

        val keyword = etSearch.text?.toString()?.trim().takeIf { !it.isNullOrBlank() }

        val params = ReportRepo.QueryParams(
            typeDb = typeDb,
            onlyOpen = swOnlyOpen.isChecked,
            onlyFollowedByUserId = if (swOnlyFollowed.isChecked) user.id else null,
            keyword = keyword,
            sortDesc = swSortDesc.isChecked,
            adminUnitOnly = if (user.role == "ADMIN" && swAdminUnitOnly.isChecked) user.unit else null
        )

        val list = repo.listReports(params)

        if (adapter == null) {
            adapter = ReportAdapter(list) { report ->
                val i = Intent(requireContext(), ReportDetailActivity::class.java)
                i.putExtra("report_id", report.id)
                startActivity(i)
            }
            recycler.adapter = adapter
        } else {
            adapter?.submit(list)
        }

        progress.visibility = View.GONE
    }
}
