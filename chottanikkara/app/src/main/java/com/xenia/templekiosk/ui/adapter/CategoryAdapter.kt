package com.xenia.templekiosk.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.Category
import com.xenia.templekiosk.utils.SessionManager
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_ENGLISH
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_HINDI
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_KANNADA
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_MALAYALAM
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_TAMIL
import com.xenia.templekiosk.utils.common.Constants.LANGUAGE_TELUGU

class CategoryAdapter(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val onCategoryClickListener: OnCategoryClickListener
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var categories: List<Category> = listOf()
    private var selectedItemPosition = 0

    interface OnCategoryClickListener {
        fun onCategoryClick(category: Category)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.vazhipadu_category_single_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, position)
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: Category, position: Int) {

            val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
            categoryNameTextView.text = when (selectedLanguage) {
                LANGUAGE_ENGLISH -> category.categoryName
                LANGUAGE_MALAYALAM -> category.categoryNameMa
                LANGUAGE_TAMIL -> category.categoryNameTa
                LANGUAGE_KANNADA -> category.categoryNameKa
                LANGUAGE_TELUGU -> category.categoryNameTe
                LANGUAGE_HINDI -> category.categoryNameHi
                else -> category.categoryName
            }

            val isSelected = position == selectedItemPosition
            itemView.setBackgroundResource(
                if (isSelected) R.drawable.category_all_bg
                else android.R.color.transparent
            )

            categoryNameTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isSelected) R.color.white else R.color.black
                )
            )

            itemView.setOnClickListener {
                val previousSelectedItem = selectedItemPosition
                selectedItemPosition = adapterPosition

                notifyItemChanged(previousSelectedItem)
                notifyItemChanged(selectedItemPosition)

                onCategoryClickListener.onCategoryClick(category)
            }
        }
    }
}
