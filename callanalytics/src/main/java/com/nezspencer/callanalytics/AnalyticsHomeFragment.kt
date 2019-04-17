package com.nezspencer.callanalytics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.nezspencer.callanalytics.databinding.AnalyticsHomeBinding
import java.util.*

class AnalyticsHomeFragment : Fragment(), DateFilterAdapter.DateFilterListener {

    private lateinit var viewModel: AnalyticsHomeViewModel
    private lateinit var activity: AppCompatActivity
    private lateinit var binding: AnalyticsHomeBinding
    private lateinit var contactsHashMap: HashMap<String, Pair<MutableList<PhoneCall>, MutableList<PhoneData>>>
    private lateinit var recyclerAdapter: ContactsRecyclerAdapter
    private lateinit var filterDialog: DateFilterDialog

    companion object {
        fun newInstance() = AnalyticsHomeFragment()
        const val missedLabel = "missed"
        const val incomingLabel = "incoming"
        const val outgoingLabel = "outgoing"
        const val rejectedLabel = "rejected"
        const val RC_CALL_LOG = 100
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AnalyticsHomeBinding.inflate(inflater)
        viewModel = ViewModelProviders.of(
            this,
            AnalyticsHomeViewModelFactory(activity.contentResolver)
        )[AnalyticsHomeViewModel::class.java]
        viewModel.getLogsLiveData().observe(this, Observer { map ->
            map?.let {
                contactsHashMap = it
                setupData()
            }
        })

        filterDialog = DateFilterDialog.newInstance(setupDateFilterDates(), this@AnalyticsHomeFragment)
        recyclerAdapter = ContactsRecyclerAdapter(activity)
        binding.rvLogs.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.rvLogs.adapter = recyclerAdapter
        binding.btnGrantPermission.setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), RC_CALL_LOG)
        }

        binding.btnFilter.setOnClickListener {
            filterDialog
                .show(activity.supportFragmentManager, DateFilterDialog::javaClass.name)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            activity.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        ) {
            val cal = initCalender()
            cal[Calendar.MONTH] = cal.getActualMinimum(Calendar.MONTH)
            cal[Calendar.DAY_OF_MONTH] = cal.getActualMinimum(Calendar.DAY_OF_MONTH)
            viewModel.getCallLogs(DateFilter("since this year", cal.timeInMillis))
        } else {
            //no permission granted yet
            showNoPermissionView()
        }
        return binding.root
    }

    private fun setupData() {
        var missedEntry: PieEntry
        var incomingEntry: PieEntry
        var outgoingEntry: PieEntry
        var rejectedEntry: PieEntry
        val pieEntryList = mutableListOf<PieEntry>()
        contactsHashMap[missedLabel]?.let {
            if (it.first.size > 0) {
                missedEntry = PieEntry(it.first.size.toFloat(), "Missed calls", it)
                pieEntryList.add(missedEntry)
            }
        }

        contactsHashMap[incomingLabel]?.let {
            if (it.first.size > 0) {
                incomingEntry = PieEntry(it.first.size.toFloat(), "Incoming calls", it)
                pieEntryList.add(incomingEntry)
            }
        }

        contactsHashMap[outgoingLabel]?.let {
            if (it.first.size > 0) {
                outgoingEntry = PieEntry(it.first.size.toFloat(), "Outgoing calls", it)
                pieEntryList.add(outgoingEntry)
            }
        }

        contactsHashMap[rejectedLabel]?.let {
            if (it.first.size > 0) {
                rejectedEntry = PieEntry(it.first.size.toFloat(), "Rejected calls", it)
                pieEntryList.add(rejectedEntry)
            }
        }

        if (pieEntryList.size == 0) {
            //no call logs to analyse
            showNoDataView()
        } else
            showDataView()

        val pieDataset = PieDataSet(pieEntryList, "Call-Log analysis")
        pieDataset.colors = mutableListOf(
            activity.resources.getColor(R.color.missed),
            activity.resources.getColor(R.color.incoming),
            activity.resources.getColor(R.color.outgoing),
            activity.resources.getColor(R.color.rejected)
        )
        val pieData = PieData(pieDataset)
        binding.pieCallLogs.data = pieData
        binding.pieCallLogs.setDrawSliceText(false)
        binding.pieCallLogs.invalidate()
        binding.pieCallLogs.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                recyclerAdapter.swapList(mutableListOf())
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val data = e.data as Pair<MutableList<PhoneCall>, MutableList<PhoneData>>
                    recyclerAdapter.swapList(data.second)
                }
            }
        })



    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is AppCompatActivity)
            activity = context
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_CALL_LOG && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val cal = initCalender()
            cal[Calendar.MONTH] = cal.getActualMinimum(Calendar.MONTH)
            cal[Calendar.DAY_OF_MONTH] = cal.getActualMinimum(Calendar.DAY_OF_MONTH)
            viewModel.getCallLogs(DateFilter("since this year", cal.timeInMillis))
        }
    }

    private fun showNoDataView() {
        binding.pieCallLogs.visibility = View.INVISIBLE
        binding.rvLogs.visibility = View.INVISIBLE
        binding.btnGrantPermission.visibility = View.INVISIBLE
        binding.tvPrompt.visibility = View.VISIBLE
        binding.tvPrompt.text = activity.getString(R.string.analytics_empty_prompt)
    }

    private fun showNoPermissionView() {
        binding.pieCallLogs.visibility = View.INVISIBLE
        binding.rvLogs.visibility = View.INVISIBLE
        binding.tvPrompt.visibility = View.VISIBLE
        binding.tvPrompt.text = activity.getString(R.string.analytics_permission_prompt)
        binding.btnGrantPermission.visibility = View.VISIBLE
    }

    private fun showDataView() {
        binding.pieCallLogs.visibility = View.VISIBLE
        binding.rvLogs.visibility = View.VISIBLE
        binding.tvPrompt.visibility = View.INVISIBLE
        binding.btnGrantPermission.visibility = View.INVISIBLE
    }

    override fun onFilterClicked(dateFilter: DateFilter) {
        binding.tvFilterResult.text = dateFilter.name
        filterDialog.dismiss()
        viewModel.getCallLogs(dateFilter)
    }

    private fun setupDateFilterDates(): MutableList<DateFilter> {
        var calendar = initCalender()
        val date1 = Date(calendar.timeInMillis)
        calendar = initCalender()
        calendar.add(Calendar.DAY_OF_WEEK, -6)
        val date2 = Date(calendar.timeInMillis)
        return mutableListOf(
            DateFilter("Since today", date1.time),
            DateFilter("last 7 days", date2.time)
        )
    }

    private fun initCalender() = Calendar.getInstance().apply {
        set(Calendar.HOUR, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
}