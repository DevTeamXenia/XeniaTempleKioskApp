package com.xeniatechnologies.app.nimahal.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xeniatechnologies.app.nimahal.databinding.ActivityLanguageBinding
import com.xeniatechnologies.app.nimahal.utils.Constants
import org.koin.android.ext.android.inject

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private val sharedPreferences: SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()

    }

    private fun setListener() {
           binding.cardDonationEn.setOnClickListener { selectLanguage(Constants.LANGUAGE_ENGLISH) }
           binding.cardDonationMl.setOnClickListener { selectLanguage(Constants.LANGUAGE_MALAYALAM) }
    }

    private fun selectLanguage(language: String) {
        sharedPreferences.edit().putString("SL", language).apply()
        startActivity(Intent(this, SelectionActivity::class.java))
    }
}