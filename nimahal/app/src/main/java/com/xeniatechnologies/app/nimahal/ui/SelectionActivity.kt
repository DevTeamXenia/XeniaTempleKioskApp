package com.xeniatechnologies.app.nimahal.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xeniatechnologies.app.nimahal.R
import com.xeniatechnologies.app.nimahal.databinding.ActivitySelectionBinding
import com.xeniatechnologies.app.nimahal.utils.Constants
import com.xeniatechnologies.app.nimahal.utils.Methods.setLocale
import org.koin.android.ext.android.inject

class SelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectionBinding
    private val sharedPreferences: SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
        setLocale(this, selectedLanguage)

        initUi()
        intiListener()
    }

    private fun initUi() {
        binding.txtDonationFor.text = getString(R.string.donation_for)
        binding.txtSub.text = getString(R.string.subscription)
        binding.txtSwalathMajis.text = getString(R.string.swalath_majlis)
        binding.txtNikkah.text = getString(R.string.nikkah)
        binding.txtNabiDinam.text = getString(R.string.nabi_dinam)
        binding.txtBadhreengaludeDinam.text = getString(R.string.badhreengalude_dinam)
        binding.txtJeelaniDinam.text = getString(R.string.jeelani_dinam)
        binding.txtBacketPiriv.text = getString(R.string.backet_piriv)
    }

    private fun intiListener() {
        binding.cardSub.setOnClickListener { selectDonation(Constants.SUBSCRIPTION) }
        binding.cardSwalathMajis.setOnClickListener { selectDonation(Constants.SWALATHMAJLIS) }
        binding.cardNikkah.setOnClickListener { selectDonation(Constants.NIKKAH) }
        binding.cardNabiDinam.setOnClickListener { selectDonation(Constants.NABIDINAM) }
        binding.cardBadhreengaludeDinam.setOnClickListener { selectDonation(Constants.BADHREENGALUDEDINAM) }
        binding.cardJeelaniDinam.setOnClickListener { selectDonation(Constants.JEELANIDINAM) }
        binding.cardBacketPiriv.setOnClickListener { selectDonation(Constants.BACKETPIRIV) }
    }

    private fun selectDonation(donation: String) {
        val intent = Intent(this, DonationActivity::class.java)
        intent.putExtra("SD", donation)
        startActivity(intent)
    }

}