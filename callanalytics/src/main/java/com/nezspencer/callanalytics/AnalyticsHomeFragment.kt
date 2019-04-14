package com.nezspencer.callanalytics

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class AnalyticsHomeFragment : Fragment() {

    private lateinit var viewModel: AnalyticsHomeViewModel
    private lateinit var activity: AppCompatActivity
    private lateinit var piechart: PieChart

    companion object {
        fun newInstance() = AnalyticsHomeFragment()
        const val missedLabel = "missed"
        const val incomingLabel = "incoming"
        const val outgoingLabel = "outgoing"
        const val rejectedLabel = "rejected"
        const val RC_CALL_LOG = 100
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(
            this,
            AnalyticsHomeViewModelFactory(activity.contentResolver)
        )[AnalyticsHomeViewModel::class.java]
        viewModel.getLogsLiveData().observe(this, Observer { map ->
            map?.let {
                setupData(it)
            }
        })
        val view = inflater.inflate(R.layout.analytics_home, container, false)
        piechart = view.findViewById(R.id.pie_call_logs)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            activity.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.getCallLogs()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), RC_CALL_LOG)
        }
        return view
    }

    private fun setupData(map: HashMap<String, MutableList<PhoneCall>>) {
        var missedEntry: PieEntry
        var incomingEntry: PieEntry
        var outgoingEntry: PieEntry
        var rejectedEntry: PieEntry
        val pieEntryList = mutableListOf<PieEntry>()
        map[missedLabel]?.let {
            if (it.size > 0) {
                missedEntry = PieEntry(it.size.toFloat(), "Missed calls", it)
                pieEntryList.add(missedEntry)
            }
        }

        map[incomingLabel]?.let {
            if (it.size > 0) {
                incomingEntry = PieEntry(it.size.toFloat(), "Incoming calls", it)
                pieEntryList.add(incomingEntry)
            }
        }

        map[outgoingLabel]?.let {
            if (it.size > 0) {
                outgoingEntry = PieEntry(it.size.toFloat(), "Outgoing calls", it)
                pieEntryList.add(outgoingEntry)
            }
        }

        map[rejectedLabel]?.let {
            if (it.size > 0) {
                rejectedEntry = PieEntry(it.size.toFloat(), "Rejected calls", it)
                pieEntryList.add(rejectedEntry)
            }
        }

        val pieDataset = PieDataSet(pieEntryList, "Call-Log analysis")
        pieDataset.colors = mutableListOf(
            activity.resources.getColor(R.color.missed),
            activity.resources.getColor(R.color.incoming),
            activity.resources.getColor(R.color.outgoing),
            activity.resources.getColor(R.color.rejected)
        )
        val pieData = PieData(pieDataset)
        piechart.data = pieData
        piechart.setDrawSliceText(false)
        piechart.invalidate()


    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is AppCompatActivity)
            activity = context
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_CALL_LOG && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            viewModel.getCallLogs()
    }
}