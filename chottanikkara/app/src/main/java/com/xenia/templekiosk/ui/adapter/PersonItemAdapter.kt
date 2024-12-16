package com.xenia.templekiosk.ui.adapter

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.OfferingItem
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_ENGLISH
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_HINDI
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_KANNADA
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_MALAYALAM
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_TAMIL
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_TELUGU


class PersonItemAdapter(
    private val itemList: MutableList<OfferingItem>,
    private val name: String,
    private val star: String,
    private val sharedPreferences: SharedPreferences,
    private val onItemDeleteListener: OnItemDeleteListener
) : RecyclerView.Adapter<PersonItemAdapter.ItemViewHolder>() {

    interface OnItemDeleteListener {
        fun onItemDelete(name: String, star: String, offeringId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.person_item_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemNameTextView: TextView = itemView.findViewById(R.id.txtItemName)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.txtItemPrice)
        private val itemDevathaTextView: TextView = itemView.findViewById(R.id.txtDevatha)
        private val itemDelete: ImageView = itemView.findViewById(R.id.txtItemDelete)

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(item: OfferingItem) {
            itemNameTextView.text = item.vaOfferingsName
            val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
            itemNameTextView.text = when (selectedLanguage) {
                LANGUAGE_ENGLISH -> item.vaOfferingsName
                LANGUAGE_MALAYALAM -> item.vaOfferingsNameMa
                LANGUAGE_TAMIL -> item.vaOfferingsNameTa
                LANGUAGE_KANNADA -> item.vaOfferingsNameKa
                LANGUAGE_TELUGU -> item.vaOfferingsNameTe
                LANGUAGE_HINDI -> item.vaOfferingsNameHi
                else -> item.vaOfferingsName
            }
            itemDevathaTextView.text = item.vaSubTempleName.replace("\n", " ")
            val formattedAmount = String.format("%.2f", item.vaOfferingsAmount)
            itemPriceTextView.text = "₹ $formattedAmount"

            itemDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val removedItem = itemList[position]
                    onItemDeleteListener.onItemDelete(name, star, removedItem.vaOfferingsId)
                    itemList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
    }

}
