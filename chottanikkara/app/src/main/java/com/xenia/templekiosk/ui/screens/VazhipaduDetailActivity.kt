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


    }
}