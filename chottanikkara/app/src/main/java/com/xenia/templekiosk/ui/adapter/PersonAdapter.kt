package com.xenia.templekiosk.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.CartItem
import com.xenia.templekiosk.data.network.model.PersonWithItems
import com.xenia.templekiosk.utils.SessionManager

class PersonAdapter(
    private val personList: List<PersonWithItems>,
    private val onItemRemoved: (CartItem) -> Unit,
    private val sessionManager: SessionManager
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vazhipadi_cart_single_row, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = personList[position]
        holder.bind(person, onItemRemoved, sessionManager)
    }

    override fun getItemCount(): Int = personList.size

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.txtName)
        private val starTextView: TextView = itemView.findViewById(R.id.txtStar)
        private val itemsRecyclerView: RecyclerView = itemView.findViewById(R.id.relPersonItem)

        fun bind(personWithItems: PersonWithItems, onItemRemoved: (CartItem) -> Unit, sessionManager: SessionManager) {
            nameTextView.text = personWithItems.personName
            starTextView.text = personWithItems.personStar

            val adapter = PersonItemAdapter(personWithItems.items.toMutableList(), onItemRemoved, sessionManager)
            itemsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            itemsRecyclerView.adapter = adapter
        }
    }
}
