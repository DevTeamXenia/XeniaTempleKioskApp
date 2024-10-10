package com.xenia.templekiosk.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.repository.LoginRepository
import com.xenia.templekiosk.databinding.ActivityLanguageBinding
import com.xenia.templekiosk.utils.SessionManager
import com.xenia.templekiosk.utils.common.CommonMethod.dismissLoader
import com.xenia.templekiosk.utils.common.CommonMethod.showLoader
import com.xenia.templekiosk.utils.common.CommonMethod.showSnackbar
import com.xenia.templekiosk.utils.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private val sharedPreferences: SharedPreferences by inject()
    private val loginRepository: LoginRepository by inject()
    private val sessionManager: SessionManager by inject()

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



        binding.cardEnglish.setOnClickListener { selectLanguage(Constants.LANGUAGE_ENGLISH) }
        binding.cardMalayalam.setOnClickListener { selectLanguage(Constants.LANGUAGE_MALAYALAM) }
        binding.cardTamil.setOnClickListener { selectLanguage(Constants.LANGUAGE_TAMIL) }
        binding.cardKannada.setOnClickListener { selectLanguage(Constants.LANGUAGE_KANNADA) }
        binding.cardTelugu.setOnClickListener { selectLanguage(Constants.LANGUAGE_TELUGU) }
        binding.cardHindi.setOnClickListener { selectLanguage(Constants.LANGUAGE_HINDI) }


        loadCompanyDetails()


    }

    private fun loadCompanyDetails() {
        showLoader(this@LanguageActivity,"loading settings...")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = loginRepository.getCompany(sessionManager.getCompanyId())
                if (response.status == "success") {
                    sessionManager.saveCompanyDetails(response.data)
                    dismissLoader()
                } else {
                    dismissLoader()
                    showSnackbar(
                        binding.root,
                        "unable to load settings! Please try again..."
                    )
                }
            } catch (e: Exception) {
                dismissLoader()
                showSnackbar(binding.root, "unable to load settings! Please try again...")
            }
        }
    }

    private fun selectLanguage(language: String) {
        sharedPreferences.edit().putString("SL", language).apply()
        startActivity(Intent(this, HomeActivity::class.java))
    }

}