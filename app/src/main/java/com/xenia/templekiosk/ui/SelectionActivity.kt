package com.xenia.templekiosk.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import com.xenia.templekiosk.R

class SelectionActivity : AppCompatActivity() {

    private lateinit var cardDonation : CardView
    private lateinit var cardVazhipadu : CardView
    private lateinit var cardLanguage : RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        cardDonation = findViewById(R.id.card_donation)
        cardVazhipadu = findViewById(R.id.card_Vazhipadu)
        cardLanguage = findViewById(R.id.home_left_language)


        cardDonation.setOnClickListener {
            startActivity(Intent(applicationContext, DonationActivity::class.java))
        }

        cardVazhipadu.setOnClickListener {
            startActivity(Intent(applicationContext, VazhipaduActivity::class.java))
        }

        cardLanguage.setOnClickListener {
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
        }

    }
}