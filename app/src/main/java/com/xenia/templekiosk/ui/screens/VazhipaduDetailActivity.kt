package com.xenia.templekiosk.ui.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.xenia.templekiosk.R


class VazhipaduDetailActivity : AppCompatActivity() {

    private lateinit var txtItemName : TextView
    private lateinit var btnPay : Button
    private lateinit var btnSummary : Button
    private lateinit var textViewList: MutableList<TextView>
    private var selectedTextView: TextView? = null
    private val normalBackgroundResource = R.drawable.textview_board
    private val selectedBackgroundResource = R.drawable.textview_selected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vazhipadu_detail)


        initUI()

        for (textView in textViewList) {
            textView.setOnClickListener {
                handleTextViewSelection(textView)
            }
        }

        val receivedData = intent.getStringExtra("ItemName")
        if (receivedData != null) {

            txtItemName.text = receivedData
        }

        btnPay.setOnClickListener {
        }

        btnSummary.setOnClickListener {
            startActivity(Intent(applicationContext, VazhipaduActivity::class.java))
        }

    }

    private fun handleTextViewSelection(textView: TextView) {
        selectedTextView?.setBackgroundResource(normalBackgroundResource)
        selectedTextView = textView
        selectedTextView?.setBackgroundResource(selectedBackgroundResource)
    }

    private fun initUI() {
        txtItemName = findViewById(R.id.txt_item_name)
        btnPay = findViewById(R.id.btn_pay)
        btnSummary = findViewById(R.id.btn_summary)
        textViewList = mutableListOf()
        textViewList.add(findViewById(R.id.Ashwini))
        textViewList.add(findViewById(R.id.Bharani))
        textViewList.add(findViewById(R.id.Krittika))
        textViewList.add(findViewById(R.id.Rohini))
        textViewList.add(findViewById(R.id.Mrigashirsha))
        textViewList.add(findViewById(R.id.Ardra))
        textViewList.add(findViewById(R.id.Punarvasu))
        textViewList.add(findViewById(R.id.Pushya))
        textViewList.add(findViewById(R.id.Ashlesha))
        textViewList.add(findViewById(R.id.Magha))
        textViewList.add(findViewById(R.id.PurvaPhalguni))
        textViewList.add(findViewById(R.id.UttaraPhalguni))
        textViewList.add(findViewById(R.id.Hasta))
        textViewList.add(findViewById(R.id.Chitra))
        textViewList.add(findViewById(R.id.Swati))
        textViewList.add(findViewById(R.id.Vishaka))
        textViewList.add(findViewById(R.id.Anuradha))
        textViewList.add(findViewById(R.id.Jyeshta))
        textViewList.add(findViewById(R.id.Moola))
        textViewList.add(findViewById(R.id.PurvaAshadha))
        textViewList.add(findViewById(R.id.UttaraAshada))
        textViewList.add(findViewById(R.id.Shravana))
        textViewList.add(findViewById(R.id.Dhanistha))
        textViewList.add(findViewById(R.id.Shatabhisaa))
        textViewList.add(findViewById(R.id.PurvaBhadrapada))
        textViewList.add(findViewById(R.id.UttaraBhadrapada))
        textViewList.add(findViewById(R.id.Revati))

    }
}