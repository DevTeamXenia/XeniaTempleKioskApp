package com.xenia.templekiosk.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xenia.templekiosk.R
import com.xenia.templekiosk.databinding.ActivityHomeBinding
import com.xenia.templekiosk.utils.common.CommonMethod.setLocale
import com.xenia.templekiosk.utils.common.Constants
import org.koin.android.ext.android.inject



class HomeActivity : AppCompatActivity() {
    private val sharedPreferences: SharedPreferences by inject()
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
        setLocale(this, selectedLanguage)
        initUI()

        binding.cardMelkavu.setOnClickListener { selectDevatha(Constants.MELVAKUBHAGAVATI) }
        binding.cardKeezhkavu.setOnClickListener { selectDevatha(Constants.KEEZHKAVUBHAGAVATI) }
        binding.cardShiva.setOnClickListener { selectDevatha(Constants.SHIVA) }
        binding.cardAyyappa.setOnClickListener { selectDevatha(Constants.AYYAPPA) }

        binding.leftHome.setOnClickListener {
            startActivity(Intent(applicationContext,LanguageActivity::class.java))
            finish()
        }
    }


    private fun initUI() {
        binding.txtHome.text = getString(R.string.home)
        binding.txtKanika.text = getString(R.string.kanika)
        binding.txtMelkavu.text = getString(R.string.melkavu_devi)
        binding.txtKeezhkavu.text = getString(R.string.keezhkavu_devi)
        binding.txtShiva.text = getString(R.string.shiva)
        binding.txtAyyappa.text = getString(R.string.ayyappa)
    }

    private fun selectDevatha(devatha: String) {
        val intent = Intent(this, DonationActivity::class.java)
        intent.putExtra("SD", devatha)
        startActivity(intent)
    }


}