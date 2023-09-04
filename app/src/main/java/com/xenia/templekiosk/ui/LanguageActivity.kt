package com.xenia.templekiosk.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import com.xenia.templekiosk.R

class LanguageActivity : AppCompatActivity() {

    private lateinit var cardEnglish : RelativeLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        cardEnglish = findViewById(R.id.card_en)

        cardEnglish.setOnClickListener {
            startActivity(Intent(applicationContext, HomeActivity::class.java))
        }


    }

    override fun onBackPressed() {
        finishAffinity()
    }
}