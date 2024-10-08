package com.xenia.templekiosk.ui.screens

import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xenia.templekiosk.R
import com.xenia.templekiosk.databinding.ActivityHomeBinding
import org.koin.android.ext.android.inject
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private val sharedPreferences: SharedPreferences by inject()
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val selectedLanguage = sharedPreferences.getString("selected_language", "en")
        setLocale(selectedLanguage)


        displayHomeString()
    }

    private fun setLocale(languageCode: String?) {
        val locale = Locale(languageCode ?: "en")
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun displayHomeString() {
        binding.txtHome.text = getString(R.string.home)
        binding.txtKanika.text = getString(R.string.kanika)
        binding.txtMelkavu.text = getString(R.string.melkavu_devi)
        binding.txtKeezhkavu.text = getString(R.string.keezhkavu_devi)
        binding.txtShiva.text = getString(R.string.shiva)
        binding.txtAyyappa.text = getString(R.string.ayyappa)
    }
}

