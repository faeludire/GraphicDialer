package com.nezspencer.callanalytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.nezspencer.callanalytics.databinding.DialogDateFilterBinding


class DateFilterDialog : DialogFragment() {

    private lateinit var binding: DialogDateFilterBinding
    private lateinit var filterList: MutableList<DateFilter>
    private lateinit var listener: DateFilterAdapter.DateFilterListener

    companion object {
        fun newInstance(
            list: MutableList<DateFilter>,
            filterListener: DateFilterAdapter.DateFilterListener
        ) = DateFilterDialog().apply {
            filterList = list
            listener = filterListener
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogDateFilterBinding.inflate(inflater, container, false)
        val adapter = DateFilterAdapter(filterList, listener)
        binding.rvDateFilter.adapter = adapter
        return binding.root
    }
}