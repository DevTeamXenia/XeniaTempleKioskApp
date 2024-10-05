package com.xenia.templekiosk.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xenia.templekiosk.R
import com.xenia.templekiosk.databinding.ActivityLanguageBinding

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Glide.with(this)
            .asGif()
            .load(R.drawable.bg_home)
            .apply(RequestOptions().fitCenter())
            .into(binding.imgBackground)

        binding.cardEn.setOnClickListener {
            startActivity(Intent(applicationContext,HomeActivity::class.java))
        }


    }


}