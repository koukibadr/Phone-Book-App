package com.bkapp.phonebook.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bkapp.phonebook.R
import com.bkapp.phonebook.data.model.Contact

class ContactListAdapter(
        private val listOfContact: List<Contact>
) : RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.contact_list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun getItemCount(): Int {
        return listOfContact.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.contactName.text = listOfContact[position].name
        holder.contactPhone.text = listOfContact[position].phoneNumber
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName: TextView = itemView.findViewById(R.id.contactName)
        val contactPhone: TextView = itemView.findViewById(R.id.contactPhone)
    }
}