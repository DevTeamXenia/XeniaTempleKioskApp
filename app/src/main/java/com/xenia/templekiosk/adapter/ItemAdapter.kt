package com.xenia.templekiosk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.callBack.ItemClickListener

class ItemAdapter(
    private val context: Context,
    private val items: Array<String>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private var selectedItem = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.vazhipadi_item_single_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.txt_item_name)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.txt_item_price)

        fun bind(item: String, position: Int) {
            itemNameTextView.text = item

            if (position == selectedItem) {
                itemView.setBackgroundResource(R.drawable.item_selected_bg)
            } else {
                itemView.setBackgroundResource(R.drawable.item_normal_bg)
            }

            itemView.setOnClickListener {
                val previousSelectedItem = selectedItem
                selectedItem = adapterPosition
                notifyItemChanged(previousSelectedItem)
                notifyItemChanged(selectedItem)
                itemClickListener.onItemClick(adapterPosition)
            }

        }
    }
}
