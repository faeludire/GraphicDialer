package com.nezspencer.callanalytics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.nezspencer.callanalytics.databinding.AnalyticsHomeBinding
import java.util.*

class AnalyticsHomeFragment : Fragment(), DateFilterAdapter.DateFilterListener,
    ContactsRecyclerAdapter.ContactClickListener {

    private lateinit var viewModel: AnalyticsHomeViewModel
    private lateinit var activity: AppCompatActivity
    private lateinit var binding: AnalyticsHomeBinding
    private lateinit var contactsHashMap: HashMap<String, Pair<MutableList<PhoneCall>, MutableList<PhoneData>>>
    private lateinit var recyclerAdapter: ContactsRecyclerAdapter
    private lateinit var filterDialog: DateFilterDialog
    private var selectedSectorList = mutableListOf<PhoneCall>()
    private var containerID: Int = 0

    companion object {
        fun newInstance(containerId: Int) = AnalyticsHomeFragment().apply { containerID = containerId }
        const val RC_CALL_LOG = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_filter -> {
                filterDialog
                    .show(activity.supportFragmentManager, DateFilterDialog::javaClass.name)
            }
        }
        return super.onOptionsItemSelected(item)
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

        viewModel.getDetailListData().observe(this, Observer<Event<MutableList<PhoneCall>>> { event ->
            event.getIfNotHandled()?.let {
                openDetailScreen(it, DataPeriod.WEEK)
            }
        })

        filterDialog = DateFilterDialog.newInstance(setupDateFilterDates(), this@AnalyticsHomeFragment)
        recyclerAdapter = ContactsRecyclerAdapter(activity, listener = this)
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
        contactsHashMap[LogType.MISSED.label]?.let {
            if (it.first.size > 0) {
                missedEntry = PieEntry(it.first.size.toFloat(), LogType.MISSED.label, it)
                pieEntryList.add(missedEntry)
            }
        }

        contactsHashMap[LogType.RECEIVED.label]?.let {
            if (it.first.size > 0) {
                incomingEntry = PieEntry(it.first.size.toFloat(), LogType.RECEIVED.label, it)
                pieEntryList.add(incomingEntry)
            }
        }

        contactsHashMap[LogType.DIALED.label]?.let {
            if (it.first.size > 0) {
                outgoingEntry = PieEntry(it.first.size.toFloat(), LogType.DIALED.label, it)
                pieEntryList.add(outgoingEntry)
            }
        }

        contactsHashMap[LogType.REJECTED.label]?.let {
            if (it.first.size > 0) {
                rejectedEntry = PieEntry(it.first.size.toFloat(), LogType.REJECTED.label, it)
                pieEntryList.add(rejectedEntry)
            }
        }

        if (pieEntryList.size == 0) {
            //no call logs to analyse
            showNoDataView()
        } else
            showDataView()

        val pieDataset = LogsPieDataSet(pieEntryList, "Call-Log analysis")
        pieDataset.colors = mutableListOf(
            activity.resources.getColor(R.color.missed),
            activity.resources.getColor(R.color.received),
            activity.resources.getColor(R.color.outgoing),
            activity.resources.getColor(R.color.rejected)
        )
        val pieData = PieData(pieDataset)
        val legend = binding.pieCallLogs.legend
        legend.direction = Legend.LegendDirection.LEFT_TO_RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.isWordWrapEnabled = true
        legend.yOffset = 10f
        binding.pieCallLogs.description.isEnabled = false
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
                    selectedSectorList.apply {
                        clear()
                        addAll(data.first)
                    }

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

    override fun onContactClicked(data: PhoneData) {
        viewModel.getRecordsForContact(data.number, selectedSectorList)
    }

    private fun setupDateFilterDates(): MutableList<DateFilter> {
        var calendar = initCalender()
        val todayTime = calendar.timeInMillis
        calendar = initCalender()
        calendar.add(Calendar.DAY_OF_WEEK, -6)
        val day7Time = calendar.timeInMillis

        val thisMonthTime = initCalender().apply {
            set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
        }.timeInMillis

        val lastMonthCal = initCalender().apply {
            set(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
        }
        val lastMonthStart = lastMonthCal.timeInMillis
        val lastMonthEnd = lastMonthCal.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, getActualMaximum(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, getActualMaximum(Calendar.MINUTE))
            set(Calendar.SECOND, getActualMaximum(Calendar.SECOND))
        }.timeInMillis

        val thisYearDate = initCalender().apply {
            set(Calendar.WEEK_OF_YEAR, getActualMinimum(Calendar.WEEK_OF_YEAR))
            set(Calendar.MONTH, getActualMinimum(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
        }.timeInMillis

        return mutableListOf(
            DateFilter("Since today", todayTime),
            DateFilter("last 7 days", day7Time),
            DateFilter("this month", thisMonthTime),
            DateFilter("Last month", lastMonthStart, lastMonthEnd),
            DateFilter("This year", thisYearDate)
        )
    }

    private fun openDetailScreen(list: MutableList<PhoneCall>, period: DataPeriod) {
        val name = AnalyticsDetailFragment::javaClass.name
        activity.supportFragmentManager.beginTransaction()
            .replace(containerID, AnalyticsDetailFragment.newInstance(list, period), name)
            .addToBackStack(name).commit()
    }

    private fun initCalender() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
}