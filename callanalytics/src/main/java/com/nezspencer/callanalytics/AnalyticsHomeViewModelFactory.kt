package com.nezspencer.callanalytics

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.ContentResolver

class AnalyticsHomeViewModelFactory(val contentResolver: ContentResolver) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AnalyticsHomeViewModel(contentResolver) as T
    }
}