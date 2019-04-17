package com.nezspencer.callanalytics

data class DateFilter(
    val name: String,
    val startTime: Long,
    val endTime: Long = System.currentTimeMillis()
)