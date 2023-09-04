package com.xenia.templekiosk.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.xenia.templekiosk.R

class PrintPreviewActivity : AppCompatActivity() {
    private lateinit var fullPreview: LinearLayout
    private lateinit var donationPreview: LinearLayout
    private lateinit var vazhipaduPreview: LinearLayout
    private lateinit var btnHome : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_preview)

        fullPreview = findViewById(R.id.full_preview)
        donationPreview = findViewById(R.id.donation_preview)
        vazhipaduPreview = findViewById(R.id.vazhipadu_preview)
        btnHome = findViewById(R.id.btn_home)

        val receivedData = intent.getStringExtra("ScreenType")
        if (receivedData != null) {
            if (receivedData == "Donation") {
                donationPreview.visibility = View.VISIBLE
                vazhipaduPreview.visibility = View.GONE
            }
            else {
                donationPreview.visibility = View.GONE
                vazhipaduPreview.visibility = View.VISIBLE
            }
        }

        fullPreview.setOnClickListener {
            gotoNavigation()
        }

        btnHome.setOnClickListener {
            gotoNavigation()
        }
    }

    private fun gotoNavigation() {
        startActivity(Intent(applicationContext, LanguageActivity::class.java))
        finish()
    }


}