package com.nezspencer.callanalytics

import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class LogsPieDataSet(private val pieEntries: MutableList<PieEntry>, label: String) : PieDataSet(pieEntries, label) {

    override fun getEntryIndex(e: PieEntry?): Int {
        return pieEntries.indexOf(e)
    }

    override fun getColor(index: Int): Int {
        val entry = pieEntries[index]
        return when (entry.label) {
            LogType.MISSED.label -> {
                mColors[0]
            }
            LogType.RECEIVED.label -> {
                mColors[1]
            }
            LogType.DIALED.label -> {
                mColors[2]
            }
            LogType.REJECTED.label -> {
                mColors[3]
            }
            else -> {
                super.getColor(index)
            }
        }
    }
}