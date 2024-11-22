package com.xeniatechnologies.app.templekiosktirupati

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xeniatechnologies.app.templekiosktirupati.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {


    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnPay.setOnClickListener{
            startActivity(Intent(applicationContext,QRActivity::class.java))
        }

    }
}