package com.nezspencer.callanalytics

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.ContentResolver
import android.provider.CallLog
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class AnalyticsHomeViewModel(private val contentResolver: ContentResolver) : ViewModel() {

    private val logsByTypeMapLivedata = MutableLiveData<HashMap<String,
            Pair<MutableList<PhoneCall>, MutableList<PhoneData>>>>()

    @SuppressLint("MissingPermission")
    fun getCallLogs() {
        val strFields = arrayOf(
            android.provider.CallLog.Calls.CACHED_NAME,
            android.provider.CallLog.Calls.NUMBER,
            android.provider.CallLog.Calls.DATE,
            android.provider.CallLog.Calls.TYPE
        )
        val calendar = Calendar.getInstance().apply {
            this.add(Calendar.MONTH, -1)
        }
        val strOrder = android.provider.CallLog.Calls.DATE + " DESC"
        val selection = "${android.provider.CallLog.Calls.DATE} <= ${calendar.timeInMillis}"

        val mCallCursor =
            contentResolver.query(
                android.provider.CallLog.Calls.CONTENT_URI, strFields, null,
                null, strOrder
            )
        val missedList = mutableListOf<PhoneCall>()
        val outgoingList = mutableListOf<PhoneCall>()
        val incomingList = mutableListOf<PhoneCall>()
        val rejectedList = mutableListOf<PhoneCall>()
        val missedGrouped = hashMapOf<String, PhoneData>()
        val outgoingGrouped = hashMapOf<String, PhoneData>()
        val incomingGrouped = hashMapOf<String, PhoneData>()
        val rejectedGrouped = hashMapOf<String, PhoneData>()


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
        hashMap[AnalyticsHomeFragment.missedLabel] = Pair(missedList, ArrayList(missedGrouped.values))
        hashMap[AnalyticsHomeFragment.incomingLabel] = Pair(incomingList, ArrayList(outgoingGrouped.values))
        hashMap[AnalyticsHomeFragment.outgoingLabel] = Pair(outgoingList, ArrayList(outgoingGrouped.values))
        hashMap[AnalyticsHomeFragment.rejectedLabel] = Pair(rejectedList, ArrayList(rejectedGrouped.values))
        logsByTypeMapLivedata.postValue(hashMap)
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