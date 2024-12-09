package com.xenia.templekiosk.ui.adapter

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.Offering
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_ENGLISH
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_HINDI
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_KANNADA
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_MALAYALAM
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_TAMIL
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_TELUGU

class OfferingAdapter(
    private val offeringList: List<Offering>,
    private val sharedPreferences: SharedPreferences,
    private val itemClickListener: ItemClickListener,
    selectedItems: List<Offering> = emptyList()
) : RecyclerView.Adapter<OfferingAdapter.ViewHolder>() {

    private val selectedItemIds = mutableSetOf<String>()

    init {
        selectedItems.forEach { selectedItem ->
            selectedItemIds.add(selectedItem.offeringsId.toString())
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.txt_item_name)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.txt_item_price)

        @SuppressLint("SetTextI18n")
        fun bind(item: Offering) {
            itemNameTextView.text = item.offeringsName
            val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
            itemNameTextView.text = when (selectedLanguage) {
                LANGUAGE_ENGLISH -> item.offeringsName
                LANGUAGE_MALAYALAM -> item.offeringsNameMa
                LANGUAGE_TAMIL -> item.offeringsNameTa
                LANGUAGE_KANNADA -> item.offeringsNameKa
                LANGUAGE_TELUGU -> item.offeringsNameTe
                LANGUAGE_HINDI -> item.offeringsNameHi
                else -> item.offeringsName
            }

            itemPriceTextView.text = "₹ ${item.offeringsAmount}/-"

            if (selectedItemIds.contains(item.offeringsId.toString())) {
                itemView.setBackgroundResource(R.drawable.item_selected_bg)
            } else {
                itemView.setBackgroundResource(R.drawable.item_normal_bg)
            }


            itemView.setOnClickListener {
                if (selectedItemIds.contains(item.offeringsId.toString())) {
                    selectedItemIds.remove(item.offeringsId.toString())
                    itemClickListener.onItemRemoved(item)
                } else {
                    selectedItemIds.add(item.offeringsId.toString())
                    itemClickListener.onItemAdded(item)
                }

                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.vazhipadi_item_single_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(offeringList[position])
    }

    override fun getItemCount(): Int {
        return offeringList.size
    }

    interface ItemClickListener {
        fun onItemAdded(item: Offering)
        fun onItemRemoved(item: Offering)
    }
}
