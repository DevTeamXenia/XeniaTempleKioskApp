package com.xenia.templekiosk.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.listeners.InactivityHandlerActivity
import com.xenia.templekiosk.databinding.ActivityHomeBinding
import com.xenia.templekiosk.ui.dialogue.CustomInactivityDialog
import com.xenia.templekiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.templekiosk.utils.InactivityHandler
import com.xenia.templekiosk.utils.common.CommonMethod.setLocale
import com.xenia.templekiosk.utils.common.Constants
import org.koin.android.ext.android.inject

class HomeActivity : AppCompatActivity(), CustomInactivityDialog.InactivityCallback,
    InactivityHandlerActivity {

    private val sharedPreferences: SharedPreferences by inject()
    private val customQRPopupDialog: CustomQRPopupDialogue by inject()
    private lateinit var binding: ActivityHomeBinding
    private lateinit var inactivityHandler: InactivityHandler
    private lateinit var inactivityDialog: CustomInactivityDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
        setLocale(this, selectedLanguage)
        initUI()

        inactivityDialog = CustomInactivityDialog(this)
        inactivityHandler = InactivityHandler(this, supportFragmentManager, inactivityDialog,customQRPopupDialog)

        binding.cardMelkavu.setOnClickListener {
            selectDevatha(getString(R.string.melkavu_devi))
        }
        binding.cardKeezhkavu.setOnClickListener {
            selectDevatha(getString(R.string.keezhkavu_devi))
        }
        binding.cardShiva.setOnClickListener {
            selectDevatha(getString(R.string.shiva))
        }
        binding.cardAyyappa.setOnClickListener {
            selectDevatha(getString(R.string.ayyappa))
        }


        binding.leftHome.setOnClickListener {
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
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
