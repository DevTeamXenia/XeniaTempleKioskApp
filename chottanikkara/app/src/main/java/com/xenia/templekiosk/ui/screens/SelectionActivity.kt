package com.xenia.templekiosk.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.listeners.InactivityHandlerActivity
import com.xenia.templekiosk.databinding.ActivitySelectionBinding
import com.xenia.templekiosk.ui.dialogue.CustomInactivityDialog
import com.xenia.templekiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.templekiosk.utils.InactivityHandler
import com.xenia.templekiosk.utils.common.CommonMethod.setLocale
import org.koin.android.ext.android.inject

class SelectionActivity : AppCompatActivity(),CustomInactivityDialog.InactivityCallback,
    InactivityHandlerActivity {

    private val sharedPreferences: SharedPreferences by inject()
    private val customQRPopupDialog: CustomQRPopupDialogue by inject()
    private lateinit var binding: ActivitySelectionBinding

    private lateinit var inactivityHandler: InactivityHandler
    private lateinit var inactivityDialog: CustomInactivityDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
        setLocale(this, selectedLanguage)

        initUI()

        inactivityDialog = CustomInactivityDialog(this)
        inactivityHandler = InactivityHandler(this, supportFragmentManager, inactivityDialog,customQRPopupDialog)


        binding.cardDonation.setOnClickListener {
            startActivity(Intent(applicationContext,HomeActivity::class.java))
        }

        binding.cardVazhipadi?.setOnClickListener {
            startActivity(Intent(applicationContext,VazhipaduActivity::class.java))
        }

        binding.leftHome.setOnClickListener {
            startActivity(Intent(applicationContext,LanguageActivity::class.java))
            finish()
        }

    }

    private fun initUI() {
        binding.txtHome.text = getString(R.string.home)
        binding.txtVazhipadu?.text = getString(R.string.vazhipadu)
        binding.txtDonation?.text = getString(R.string.donation)
    }

    override fun resetInactivityTimer() {
        inactivityHandler.resetTimer()
    }

    override fun onResume() {
        super.onResume()
        inactivityHandler.resumeInactivityCheck()
    }

    override fun onPause() {
        super.onPause()
        inactivityHandler.pauseInactivityCheck()
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityHandler.cleanup()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        inactivityHandler.resetTimer()
        return super.dispatchTouchEvent(ev)
    }

}