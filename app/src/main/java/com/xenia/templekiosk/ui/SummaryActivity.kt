package com.xenia.templekiosk.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.common.DialogUtils
import com.xenia.templekiosk.data.common.Screen

class SummaryActivity : AppCompatActivity() {

    private lateinit var btnMore : Button
    private lateinit var btnPay : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        btnMore = findViewById(R.id.btn_more)
        btnPay = findViewById(R.id.btn_pay)
        btnMore.setOnClickListener {
            startActivity(Intent(applicationContext,VazhipaduActivity::class.java))
            finish()
        }

        btnPay.setOnClickListener {
            DialogUtils.showQRPayPopup(this,"maheshmohan7319@okaxis","100",Screen.VazhipaduAreaScreen )
        }

    }
}