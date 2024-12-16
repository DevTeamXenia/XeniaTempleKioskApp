package com.xenia.templekiosk.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.PersonWithItems

@Suppress("DEPRECATION")
class PersonAdapter(
    private var personList: List<PersonWithItems>,
    private var sharedPreferences: SharedPreferences,
    private val onItemDeleteListener: PersonItemAdapter.OnItemDeleteListener
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updatePersonList(updatedList: List<PersonWithItems>) {
        personList = updatedList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vazhipadi_cart_single_row, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = personList[position]
        holder.bind(person, sharedPreferences, onItemDeleteListener)
    }

    override fun getItemCount(): Int = personList.size

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.txtName)
        private val starTextView: TextView = itemView.findViewById(R.id.txtStar)
        private val itemsRecyclerView: RecyclerView = itemView.findViewById(R.id.relPersonItem)

        fun bind(
            personWithItems: PersonWithItems,
            sharedPreferences: SharedPreferences,
            onItemDeleteListener: PersonItemAdapter.OnItemDeleteListener
        ) {
            nameTextView.text = personWithItems.personName
            starTextView.text = personWithItems.personStarLa

            val adapter = PersonItemAdapter(
                personWithItems.items.toMutableList(),
                personWithItems.personName,
                personWithItems.personStar,
                sharedPreferences,
                onItemDeleteListener
            )

            itemsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            itemsRecyclerView.adapter = adapter
        }

    }
}
