package com.nezspencer.callanalytics

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactsRecyclerAdapter(
    private val context: Context,
    private val contactList: MutableList<PhoneData> = mutableListOf()
) : RecyclerView.Adapter<ContactsRecyclerAdapter.Holder>() {


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): Holder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_contact, p0, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = contactList.size

    override fun onBindViewHolder(p0: Holder, p1: Int) {
        val item = contactList[p0.adapterPosition]
        p0.contactName.text = item.name
        p0.frequencyView.text = "${item.count} times"

    }

    fun swapList(list: MutableList<PhoneData>) {
        contactList.clear()
        contactList.addAll(list)
        notifyDataSetChanged()
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.iv_profile)
        val contactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        val frequencyView: TextView = itemView.findViewById(R.id.tv_frequency)
    }
}