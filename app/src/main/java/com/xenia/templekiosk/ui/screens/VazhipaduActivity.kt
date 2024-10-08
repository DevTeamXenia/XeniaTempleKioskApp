package com.xenia.templekiosk.ui.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R
import com.xenia.templekiosk.ui.adapter.CategoryAdapter
import com.xenia.templekiosk.ui.adapter.ItemAdapter
import com.xenia.templekiosk.data.listeners.ItemClickListener

class VazhipaduActivity : AppCompatActivity(), ItemClickListener {


    private lateinit var btnSummary : Button
    private lateinit var btnPay : Button
    private lateinit var itemArray: Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vazhipadu)


        btnSummary = findViewById(R.id.btn_summary)
        btnPay = findViewById(R.id.btn_pay)

        val resources = this.resources
        val catArray = resources.getStringArray(R.array.vazhipaduCategory)
         itemArray = resources.getStringArray(R.array.vazhipaduItem)

        val recyclerViewForCategory = findViewById<RecyclerView>(R.id.category_recyclerView)
        val recyclerViewForItem = findViewById<RecyclerView>(R.id.item_recyclerView)
        val layoutManager = LinearLayoutManager(this)




        recyclerViewForCategory.layoutManager = layoutManager
        val categoryAdapter = CategoryAdapter(this, catArray)
        recyclerViewForCategory.adapter = categoryAdapter

        val layoutManager2 = GridLayoutManager(this, 3)
        recyclerViewForItem.layoutManager = layoutManager2

        val itemAdapter = ItemAdapter(this, itemArray,this)
        recyclerViewForItem.adapter = itemAdapter


        btnSummary.setOnClickListener {
            startActivity(Intent(applicationContext, SummaryActivity::class.java))
        }

        btnPay.setOnClickListener {

        }
    }

    override fun onItemClick(position: Int) {
        val selectedItem = itemArray[position]
        val intent = Intent(applicationContext, VazhipaduDetailActivity::class.java)
        intent.putExtra("ItemName", selectedItem)
        startActivity(intent)

    }
}