package com.xenia.templekiosk.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.xenia.templekiosk.R
import com.xenia.templekiosk.databinding.ActivityLanguageBinding
import com.xenia.templekiosk.utils.common.LanguageConstants
import org.koin.android.ext.android.inject

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private val sharedPreferences: SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Glide.with(this)
            .asGif()
            .load(R.drawable.bg_home)
            .apply(RequestOptions()
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(binding.imgBackground)



        binding.cardEnglish.setOnClickListener { selectLanguage(LanguageConstants.LANGUAGE_ENGLISH) }
        binding.cardMalayalam.setOnClickListener { selectLanguage(LanguageConstants.LANGUAGE_MALAYALAM) }
        binding.cardTamil.setOnClickListener { selectLanguage(LanguageConstants.LANGUAGE_TAMIL) }
        binding.cardKannada.setOnClickListener { selectLanguage(LanguageConstants.LANGUAGE_KANNADA) }
        binding.cardTelugu.setOnClickListener { selectLanguage(LanguageConstants.LANGUAGE_TELUGU) }
        binding.cardHindi.setOnClickListener { selectLanguage(LanguageConstants.LANGUAGE_HINDI) }


    }

    private fun selectLanguage(language: String) {
        sharedPreferences.edit().putString("SL", language).apply()
        startActivity(Intent(this, HomeActivity::class.java))
    }

}