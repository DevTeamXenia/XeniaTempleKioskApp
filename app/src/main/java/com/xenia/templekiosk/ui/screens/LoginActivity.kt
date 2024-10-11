package com.xenia.templekiosk.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xenia.templekiosk.data.repository.LoginRepository
import com.xenia.templekiosk.databinding.ActivityLoginBinding
import com.xenia.templekiosk.utils.SessionManager
import com.xenia.templekiosk.utils.common.CommonMethod.dismissLoader
import com.xenia.templekiosk.utils.common.CommonMethod.showLoader
import com.xenia.templekiosk.utils.common.CommonMethod.showSnackbar
import com.xenia.templekiosk.utils.common.ScreenOrientationHelper
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
        if (!ScreenOrientationHelper.checkScreenOnAppOpen(this)) {
            return
        }

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
            finish()
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ScreenOrientationHelper.setScreenOrientation(applicationContext)

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


}