package com.xenia.churchkiosk.ui.screens


import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.xenia.churchkiosk.utils.SessionManager
import com.xenia.churchkiosk.utils.common.CommonMethod.dismissLoader
import com.xenia.churchkiosk.utils.common.CommonMethod.isInternetAvailable
import com.xenia.churchkiosk.utils.common.CommonMethod.showLoader
import com.xenia.churchkiosk.utils.common.CommonMethod.showSnackbar
import com.xenia.churchkiosk.utils.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import com.bumptech.glide.request.target.Target
import com.xenia.churchkiosk.R
import com.xenia.churchkiosk.data.repository.LoginRepository
import com.xenia.churchkiosk.data.repository.VazhipaduRepository
import com.xenia.churchkiosk.databinding.ActivityLanguageBinding
import com.xenia.churchkiosk.ui.dialogue.CustomInternetAvailabilityDialog


class LanguageActivity : AppCompatActivity(),
    CustomInternetAvailabilityDialog.InternetAvailabilityListener{

    private lateinit var binding: ActivityLanguageBinding
    private val sharedPreferences: SharedPreferences by inject()
    private val loginRepository: LoginRepository by inject()
    private val vazhipaduRepository: VazhipaduRepository by inject()
    private val sessionManager: SessionManager by inject()
    private val customInternetAvailabilityDialog: CustomInternetAvailabilityDialog by inject()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestOverlayPermission()
        setupLanguageButtons()
        lifecycleScope.launch {
            vazhipaduRepository.clearAllData()
        }
    }

    override fun onRestart() {
        lifecycleScope.launch {
            vazhipaduRepository.clearAllData()
        }
        super.onRestart()
    }


    private fun setupLanguageButtons() {
        binding.cardEnglish.setOnClickListener { selectLanguage(Constants.LANGUAGE_ENGLISH) }
        binding.cardMalayalam.setOnClickListener { selectLanguage(Constants.LANGUAGE_MALAYALAM) }
        binding.cardTamil.setOnClickListener { selectLanguage(Constants.LANGUAGE_TAMIL) }
        binding.cardKannada.setOnClickListener { selectLanguage(Constants.LANGUAGE_KANNADA) }
        binding.cardTelugu.setOnClickListener { selectLanguage(Constants.LANGUAGE_TELUGU) }
        binding.cardHindi.setOnClickListener { selectLanguage(Constants.LANGUAGE_HINDI) }
    }


    private fun loadCompanyDetails() {
        if (isInternetAvailable(this)) {
            showLoader(this@LanguageActivity, "Loading settings...")
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = loginRepository.getCompany(sessionManager.getCompanyId())
                    if (response.status == "success") {
                        sessionManager.saveCompanyDetails(response.data)
                        dismissLoader()
                    } else {
                        dismissLoader()
                        showSnackbar(binding.root, "Unable to load settings! Please try again...")
                    }
                } catch (e: Exception) {
                    dismissLoader()
                    showSnackbar(binding.root, "Unable to load settings! Please try again...")
                }
            }
        } else {
            customInternetAvailabilityDialog.show(supportFragmentManager, "CustomPopup")
        }
    }

    override fun onRetryClicked() {
        loadCompanyDetails()
    }

    override fun onDialogInactive() {
        TODO("Not yet implemented")
    }


    private fun selectLanguage(language: String) {
        sharedPreferences.edit().putString("SL", language).apply()
        startActivity(Intent(this, SelectionActivity::class.java))
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            vazhipaduRepository.clearAllData()
            loadCompanyDetails()
        }

    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }

    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* No action needed after permission request */ }

}