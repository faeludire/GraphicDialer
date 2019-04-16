package com.nezspencer.callanalytics

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AnalyticsHomeViewModelFactory(val contentResolver: ContentResolver) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AnalyticsHomeViewModel(contentResolver) as T
    }
}