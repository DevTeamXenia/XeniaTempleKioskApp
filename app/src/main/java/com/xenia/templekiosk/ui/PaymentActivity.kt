package com.xenia.templekiosk.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.xenia.templekiosk.R
import com.xenia.templekiosk.common.Screen

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val receivedData = intent.getStringExtra("ScreenType")
        if (receivedData != null) {
            Handler().postDelayed({
                val intent = Intent(applicationContext, PrintPreviewActivity::class.java)
                intent.putExtra("ScreenType",receivedData)
                startActivity(intent)
            }, 3000)
        }


    }
}