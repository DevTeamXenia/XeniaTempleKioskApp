package com.xenia.templekiosk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R

class CategoryAdapter(
    private val context: Context,
    private val fruits: Array<String>
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.vazhipadu_category_single_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fruit = fruits[position]
        holder.bind(fruit, position)
    }

    override fun getItemCount(): Int {
        return fruits.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fruitNameTextView: TextView = itemView.findViewById(R.id.category_name)

        fun bind(fruit: String, position: Int) {
            fruitNameTextView.text = fruit

            if (position == selectedItem) {
                itemView.setBackgroundResource(R.drawable.category_all_bg) // Set selected background
                fruitNameTextView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Set selected text color
            } else {
                itemView.setBackgroundResource(android.R.color.transparent) // Clear background
                fruitNameTextView.setTextColor(ContextCompat.getColor(context, R.color.black)) // Set default text color
            }

            itemView.setOnClickListener {
                val previousSelectedItem = selectedItem
                selectedItem = adapterPosition
                notifyItemChanged(previousSelectedItem)
                notifyItemChanged(selectedItem)
            }
        }
    }
}
