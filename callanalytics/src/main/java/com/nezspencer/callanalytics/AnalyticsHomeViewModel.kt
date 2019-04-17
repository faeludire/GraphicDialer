package com.nezspencer.callanalytics

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.provider.CallLog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class AnalyticsHomeViewModel(private val contentResolver: ContentResolver) : ViewModel() {

    private val logsByTypeMapLivedata = MutableLiveData<HashMap<String,
            Pair<MutableList<PhoneCall>, MutableList<PhoneData>>>>()

    @SuppressLint("MissingPermission")
    fun getCallLogs(dateFilter: DateFilter) {

        val missedList = mutableListOf<PhoneCall>()
        val outgoingList = mutableListOf<PhoneCall>()
        val incomingList = mutableListOf<PhoneCall>()
        val rejectedList = mutableListOf<PhoneCall>()
        val missedGrouped = hashMapOf<String, PhoneData>()
        val outgoingGrouped = hashMapOf<String, PhoneData>()
        val incomingGrouped = hashMapOf<String, PhoneData>()
        val rejectedGrouped = hashMapOf<String, PhoneData>()
        val mCallCursor = queryForCallLogs(dateFilter)

        if (mCallCursor.moveToFirst()) {

            do {
                val number = mCallCursor.getString(mCallCursor.getColumnIndex(CallLog.Calls.NUMBER))
                val name = mCallCursor.getString(mCallCursor.getColumnIndex(CallLog.Calls.CACHED_NAME)) ?: number
                val date = mCallCursor.getLong(mCallCursor.getColumnIndex(android.provider.CallLog.Calls.DATE))
                val type = mCallCursor.getInt(mCallCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE))
                val log = PhoneCall(name, number, date, type)
                when (type) {
                    CallLog.Calls.MISSED_TYPE -> {
                        groupByPhoneNumber(log, missedGrouped)
                        missedList.add(log)
                    }
                    CallLog.Calls.OUTGOING_TYPE -> {
                        groupByPhoneNumber(log, outgoingGrouped)
                        outgoingList.add(log)
                    }
                    CallLog.Calls.INCOMING_TYPE -> {
                        groupByPhoneNumber(log, incomingGrouped)
                        incomingList.add(log)
                    }
                    CallLog.Calls.REJECTED_TYPE -> {
                        groupByPhoneNumber(log, rejectedGrouped)
                        rejectedList.add(log)
                    }
                }

            } while (mCallCursor.moveToNext())
            mCallCursor.close()

        }
        val hashMap = HashMap<String, Pair<MutableList<PhoneCall>, MutableList<PhoneData>>>()
        val missedData = ArrayList(missedGrouped.values)
        missedData.sortByDescending { it.count }
        val incomingData = ArrayList(outgoingGrouped.values)
        incomingData.sortByDescending { it.count }
        val outgoingData = ArrayList(outgoingGrouped.values)
        outgoingData.sortByDescending { it.count }
        val rejectedData = ArrayList(rejectedGrouped.values)
        rejectedData.sortByDescending { it.count }
        hashMap[AnalyticsHomeFragment.missedLabel] = Pair(missedList, missedData)
        hashMap[AnalyticsHomeFragment.incomingLabel] = Pair(incomingList, incomingData)
        hashMap[AnalyticsHomeFragment.outgoingLabel] = Pair(outgoingList, outgoingData)
        hashMap[AnalyticsHomeFragment.rejectedLabel] = Pair(rejectedList, rejectedData)
        logsByTypeMapLivedata.postValue(hashMap)
    }

    @SuppressLint("MissingPermission")
    private fun queryForCallLogs(dateFilter: DateFilter): Cursor {
        val strFields = arrayOf(
            android.provider.CallLog.Calls.CACHED_NAME,
            android.provider.CallLog.Calls.NUMBER,
            android.provider.CallLog.Calls.DATE,
            android.provider.CallLog.Calls.TYPE
        )

        val strOrder = android.provider.CallLog.Calls.DATE + " DESC"
        val selection = "${android.provider.CallLog.Calls.DATE} BETWEEN ? AND ?"

        return contentResolver.query(
            CallLog.Calls.CONTENT_URI, strFields, selection,
            arrayOf(dateFilter.startTime.toString(), dateFilter.endTime.toString()), strOrder
        )
    }

    fun getLogsLiveData(): LiveData<HashMap<String, Pair<MutableList<PhoneCall>,
            MutableList<PhoneData>>>> = logsByTypeMapLivedata

    private fun groupByPhoneNumber(log: PhoneCall, map: HashMap<String, PhoneData>) {
        var record = map[log.number]
        if (record == null)
            record = PhoneData(log.name, log.number, 1)
        else
            record.count += 1
        map[log.number] = record
    }
}