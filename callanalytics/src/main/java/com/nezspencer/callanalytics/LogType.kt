package com.nezspencer.callanalytics

enum class LogType(val label: String) {
    MISSED("Missed calls"),
    RECEIVED("Received calls"),
    DIALED("Outgoing calls"),
    REJECTED("Rejected calls")
}