package com.nezspencer.callanalytics

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.nezspencer.callanalytics.databinding.AnalyticsDetailBinding

class AnalyticsDetailFragment : Fragment() {
    private lateinit var dataList: MutableList<PhoneCall>
    private lateinit var binding: AnalyticsDetailBinding
    private lateinit var viewModel: AnalyticsDetailViewModel
    private lateinit var hostActivity: AppCompatActivity
    private var dataPeriod: DataPeriod = DataPeriod.WEEK

    companion object {
        fun newInstance(list: MutableList<PhoneCall>, period: DataPeriod) = AnalyticsDetailFragment().apply {
            dataList = list
            dataPeriod = period

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AnalyticsDetailBinding.inflate(inflater)
        viewModel = ViewModelProviders.of(
            this,
            AnalyticsDetailViewModelFactory(dataList)
        )[AnalyticsDetailViewModel::class.java]
        viewModel.getGraphData().observe(this, Observer<Pair<BarDataSet, MutableList<String>>> { pair ->
            pair?.let {
                setupData(it)
            }
        })
        viewModel.prepareDataForGraph(DataPeriod.WEEK)
        return binding.root
    }

    private fun setupData(lineDataPair: Pair<BarDataSet, MutableList<String>>) {

        val xAxis = binding.chartDetail.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val xAxisFormatter = object : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return lineDataPair.second[value.toInt()]
            }
        }

        xAxis.granularity = 1f
        xAxis.valueFormatter = xAxisFormatter

        val dataSet = lineDataPair.first
        dataSet.color = ContextCompat.getColor(hostActivity, R.color.toolbar_color)
        dataSet.valueTextColor = ContextCompat.getColor(hostActivity, R.color.analytics_accent)
        binding.chartDetail.data = BarData(dataSet)
        binding.chartDetail.animateX(1000)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is AppCompatActivity)
            hostActivity = context
    }


}