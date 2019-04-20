package com.nezspencer.callanalytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AnalyticsDetailViewModelFactory(private val dataList: MutableList<PhoneCall>) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AnalyticsDetailViewModel(dataList) as T
    }
}