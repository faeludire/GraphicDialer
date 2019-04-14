package com.nezspencer.callanalytics

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.ContentResolver
import android.provider.CallLog
import java.util.*
import kotlin.collections.HashMap


class AnalyticsHomeViewModel(private val contentResolver: ContentResolver) : ViewModel() {

    private val logsByTypeMapLivedata = MutableLiveData<HashMap<String, MutableList<PhoneCall>>>()

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

        if (mCallCursor.moveToFirst()) {

            do {

                val name = mCallCursor.getString(mCallCursor.getColumnIndex(CallLog.Calls.CACHED_NAME)) ?: "Anon"
                val number = mCallCursor.getString(mCallCursor.getColumnIndex(CallLog.Calls.NUMBER))
                val date = mCallCursor.getLong(mCallCursor.getColumnIndex(android.provider.CallLog.Calls.DATE))
                val type = mCallCursor.getInt(mCallCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE))
                val log = PhoneCall(name, number, date, type)
                when (type) {
                    CallLog.Calls.MISSED_TYPE -> {
                        missedList.add(log)
                    }
                    CallLog.Calls.OUTGOING_TYPE -> {
                        outgoingList.add(log)
                    }
                    CallLog.Calls.INCOMING_TYPE -> {
                        incomingList.add(log)
                    }
                    CallLog.Calls.REJECTED_TYPE -> {
                        rejectedList.add(log)
                    }
                }

            } while (mCallCursor.moveToNext())
            mCallCursor.close()

        }
        val hashMap = HashMap<String, MutableList<PhoneCall>>()
        hashMap[AnalyticsHomeFragment.missedLabel] = missedList
        hashMap[AnalyticsHomeFragment.incomingLabel] = incomingList
        hashMap[AnalyticsHomeFragment.outgoingLabel] = outgoingList
        hashMap[AnalyticsHomeFragment.rejectedLabel] = rejectedList
        logsByTypeMapLivedata.postValue(hashMap)
    }

    fun getLogsLiveData(): LiveData<HashMap<String, MutableList<PhoneCall>>> = logsByTypeMapLivedata
}