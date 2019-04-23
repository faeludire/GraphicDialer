package com.nezspencer.callanalytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.util.*
import kotlin.collections.ArrayList

class AnalyticsDetailViewModel(private val dataList: MutableList<PhoneCall>) : ViewModel() {

    private val lineGraphData = MutableLiveData<Pair<BarDataSet, MutableList<String>>>()

    fun prepareDataForGraph(period: DataPeriod) {
        when (period) {
            DataPeriod.HOUR -> {
            }
            DataPeriod.DAY -> {
            }
            DataPeriod.WEEK -> {
            }
            DataPeriod.MONTH -> {
            }
        }

        val map = HashMap<String, PhoneData>()
        for (log in dataList) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = log.date
            var counter = 0
            val name = "Week ${calendar[Calendar.WEEK_OF_YEAR]}"
            if (map[name] == null) {
                counter++
                map[name] = PhoneData(name, log.number, counter)
            } else {
                counter = map[name]!!.count
                counter++
                map[name]!!.count = counter
            }
        }

        val parsedList = ArrayList(map.values)
        val labels = ArrayList(map.keys)
        val barEntries = mutableListOf<BarEntry>()
        for ((i, data) in parsedList.withIndex()) {
            barEntries.add(BarEntry(i.toFloat(), data.count.toFloat(), data))
        }
        val dataset = BarDataSet(barEntries, "")
        lineGraphData.postValue(Pair(dataset, labels))
    }

    fun getGraphData(): LiveData<Pair<BarDataSet, MutableList<String>>> = lineGraphData
}