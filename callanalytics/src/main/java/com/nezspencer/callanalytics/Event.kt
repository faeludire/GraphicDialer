package com.nezspencer.callanalytics

open class Event<out T>(private val data: MutableList<PhoneCall>) {

    var isHandled = false
        private set

    fun getIfNotHandled(): MutableList<PhoneCall>? {
        return if (isHandled)
            null
        else {
            isHandled = true
            data
        }

    }

    fun getData() = data
}