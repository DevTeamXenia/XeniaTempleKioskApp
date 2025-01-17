package com.xenia.churchkiosk.ui.adapter

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xenia.churchkiosk.R
import com.xenia.churchkiosk.data.network.model.Offering
import com.xenia.churchkiosk.utils.common.Constants.LANGUAGE_ENGLISH
import com.xenia.churchkiosk.utils.common.Constants.LANGUAGE_HINDI
import com.xenia.churchkiosk.utils.common.Constants.LANGUAGE_KANNADA
import com.xenia.churchkiosk.utils.common.Constants.LANGUAGE_MALAYALAM
import com.xenia.churchkiosk.utils.common.Constants.LANGUAGE_TAMIL
import com.xenia.churchkiosk.utils.common.Constants.LANGUAGE_TELUGU

class OfferingAdapter(
    private var offeringList: List<Offering>,
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

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(item: Offering) {
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

            val amountDouble = item.offeringsAmount
            val formattedAmount = String.format("%.2f", amountDouble)
            itemPriceTextView.text = "₹ $formattedAmount/-"

            if (selectedItemIds.contains(item.offeringsId.toString())) {
                itemView.setBackgroundResource(R.drawable.item_selected_bg)
            } else {
                itemView.setBackgroundResource(R.drawable.item_normal_bg)
            }


            itemView.setOnClickListener {
                if (selectedItemIds.contains(item.offeringsId.toString())) {
                    selectedItemIds.remove(item.offeringsId.toString())
                    itemClickListener.onItemRemoved(item, selectedItemIds)
                } else {
                    selectedItemIds.add(item.offeringsId.toString())
                    itemClickListener.onItemAdded(item, selectedItemIds)
                }
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


    fun updateBackgroundForItem(offeringsId: String) {
        val position = offeringList.indexOfFirst { it.offeringsId.toString() == offeringsId }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newOfferingList: List<Offering>, newSelectedItems: List<Offering> = emptyList()) {
        offeringList = newOfferingList
        selectedItemIds.clear()
        newSelectedItems.forEach { selectedItem ->
            selectedItemIds.add(selectedItem.offeringsId.toString())
        }
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onItemAdded(item: Offering, selectedItemIds: MutableSet<String>)
        fun onItemRemoved(item: Offering, selectedItemIds: MutableSet<String>)
    }
}
