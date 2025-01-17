package com.xenia.churchkiosk.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.xenia.churchkiosk.data.repository.LoginRepository
import com.xenia.churchkiosk.databinding.ActivityLoginBinding
import com.xenia.churchkiosk.utils.SessionManager
import com.xenia.churchkiosk.utils.common.CommonMethod.dismissLoader
import com.xenia.churchkiosk.utils.common.CommonMethod.showLoader
import com.xenia.churchkiosk.utils.common.CommonMethod.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val sessionManager: SessionManager by inject()
    private val loginRepository: LoginRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestOverlayPermission()

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
            finish()
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnLogin.setOnClickListener {
            val userId = binding.edtUserId.text.toString()
            val password = binding.edtPassword.text.toString()
            if (validateAndLogin(userId, password)) {
                performLogin(userId, password)
            }
        }
    }

    private fun validateAndLogin(userId: String, password: String): Boolean {
        binding.apply {
            when {
                userId.isEmpty() -> {
                    edtUserId.error = "User ID cannot be empty"
                    edtUserId.requestFocus()
                    return false
                }
                password.isEmpty() -> {
                    edtPassword.error = "Password cannot be empty"
                    edtPassword.requestFocus()
                    return false
                }
                else -> {
                    edtUserId.error = null
                    edtPassword.error = null
                }
            }
        }
        return true
    }

    private fun performLogin(userId: String, password: String) {
            showLoader(this@LoginActivity, "Logging in...")

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = loginRepository.login(userId,password)
                    sessionManager.clearSession()
                    sessionManager.saveUserDetails(response.userId,response.userName,response.companyId)
                    startActivity(Intent(applicationContext,LanguageActivity::class.java))
                    finish()
                    dismissLoader()
                } catch (e: Exception) {
                    dismissLoader()
                    showSnackbar(binding.root, "Something went wrong! Please try again")
                }
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