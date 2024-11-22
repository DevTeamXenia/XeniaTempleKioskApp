package com.xenia.templekiosk.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R

class NakshatraAdapter(
    private val nakshatras: Array<String>,
    private val onNakshatraSelected: (String) -> Unit
) : RecyclerView.Adapter<NakshatraAdapter.NakshatraViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NakshatraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_stars, parent, false)
        return NakshatraViewHolder(view)
    }

    override fun onBindViewHolder(holder: NakshatraViewHolder, position: Int) {
        holder.nakshatraName.text = nakshatras[holder.adapterPosition]

        if (selectedPosition == holder.adapterPosition) {
            holder.nakshatraName.setBackgroundResource(R.drawable.textview_selected)
        } else {
            holder.nakshatraName.setBackgroundResource(R.drawable.textview_board)
        }

        holder.nakshatraName.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            // Notify about the selection
            onNakshatraSelected(nakshatras[selectedPosition])

            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int {
        return nakshatras.size
    }

    class NakshatraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nakshatraName: TextView = itemView.findViewById(R.id.nakshatra_name)
    }
}
