package com.xenia.templekiosk.ui.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.xenia.templekiosk.R

class SummaryActivity : AppCompatActivity() {

    private lateinit var btnMore : Button
    private lateinit var btnPay : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        btnMore = findViewById(R.id.btn_more)
        btnPay = findViewById(R.id.btn_pay)
        btnMore.setOnClickListener {
            startActivity(Intent(applicationContext, VazhipaduActivity::class.java))
            finish()
        }

        btnPay.setOnClickListener {

        }

    }
}