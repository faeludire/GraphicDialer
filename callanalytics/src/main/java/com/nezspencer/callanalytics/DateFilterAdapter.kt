package com.nezspencer.callanalytics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nezspencer.callanalytics.databinding.ItemDateFilterBinding

class DateFilterAdapter(
    private val filters: MutableList<DateFilter>,
    private val listener: DateFilterListener
) : RecyclerView.Adapter<DateFilterAdapter.Holder>() {
    private var selectedPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            ItemDateFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = filters.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val filter = filters[position]
        holder.itemBinding.rbDate.text = filter.name
        if (selectedPosition == position)
            holder.itemBinding.rbDate.isChecked = true
        holder.itemBinding.rbDate.setOnCheckedChangeListener { _, _ ->
            if (selectedPosition != position) {
                selectedPosition = position
                notifyDataSetChanged()
                listener.onFilterClicked(filter)
            }
        }
    }

    interface DateFilterListener {
        fun onFilterClicked(dateFilter: DateFilter)
    }

    inner class Holder(val itemBinding: ItemDateFilterBinding) : RecyclerView.ViewHolder(itemBinding.root)
}