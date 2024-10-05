package com.xenia.templekiosk.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xenia.templekiosk.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgMelkavu.setOnClickListener { startDonationActivity("Melkavu") }
        binding.imgKeezhkavu.setOnClickListener { startDonationActivity("Keezhkavu") }
        binding.imgShiva.setOnClickListener { startDonationActivity("shiva") }
        binding.imgAyyappa.setOnClickListener { startDonationActivity("Ayyappa") }

        binding.leftHome.setOnClickListener {
            startActivity(Intent(applicationContext,LanguageActivity::class.java))
            finish()
        }
    }


    private fun startDonationActivity(key: String) {
        val intent = Intent(this, DonationActivity::class.java)
        intent.putExtra("KEY", key)
        startActivity(intent)
    }
}
