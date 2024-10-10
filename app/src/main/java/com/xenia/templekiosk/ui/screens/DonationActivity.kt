package com.xenia.templekiosk.ui.screens


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.xenia.templekiosk.R

import com.xenia.templekiosk.data.network.model.PaymentRequest
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.databinding.ActivityDonationBinding
import com.xenia.templekiosk.ui.adapter.NakshatraAdapter
import com.xenia.templekiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.templekiosk.ui.dialogue.CustomWarningPopupDialog
import com.xenia.templekiosk.utils.SessionManager
import com.xenia.templekiosk.utils.common.CommonMethod.dismissLoader
import com.xenia.templekiosk.utils.common.CommonMethod.generateNumericTransactionReferenceID
import com.xenia.templekiosk.utils.common.CommonMethod.showLoader


import com.xenia.templekiosk.utils.common.CommonMethod.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject


class DonationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonationBinding
    private val sessionManager: SessionManager by inject()
    private val paymentRepository: PaymentRepository by inject()
    private val customPopupDialog: CustomWarningPopupDialog by inject()
    private val customQRPopupDialog: CustomQRPopupDialogue by inject()
    private var selectedNakshatra: String? = null
    private var selectDevatha: String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectDevatha = intent.getStringExtra("SD")

        initUI()

        binding.btnHome.setOnClickListener {
            customPopupDialog.show(supportFragmentManager, "warning_dialog")
        }

        binding.btnPay.setOnClickListener {
            genaratePayment()
        }

    }


    private fun initUI() {
        val layoutManager = GridLayoutManager(applicationContext, 4)
        binding.listStar.layoutManager = layoutManager

        val translatedNakshatras = resources.getStringArray(R.array.nakshatras)

        val englishNakshatras = arrayOf(
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu",
            "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta",
            "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshta", "Mula", "Purva Ashadha",
            "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
            "Uttara Bhadrapada", "Revati"
        )

        val adapter = NakshatraAdapter(translatedNakshatras) { selectedTranslatedNakshatra ->
            val index = translatedNakshatras.indexOf(selectedTranslatedNakshatra)

            selectedNakshatra = englishNakshatras[index]
        }
        binding.listStar.adapter = adapter
        binding.editTxtDonation.requestFocus()

    }


    private fun genaratePayment() {
        val donationAmount = binding.editTxtDonation.text.toString().toDoubleOrNull()
        when {
            donationAmount == null || donationAmount <= 0 -> {
                showSnackbar(binding.root, "Please enter a valid amount greater than 0!")
            }

            selectedNakshatra == null -> {
                showSnackbar(binding.root, "Please select a nakshatra!")
            }

            else -> {
                showLoader(this@DonationActivity, "Loading your QR code... Please wait.")
                genaratePaymentToken(donationAmount)
            }
        }
    }


    private fun genaratePaymentToken(donationAmount: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = paymentRepository.generateToken(sessionManager.getUserId(), sessionManager.getCompanyId())
                if (response.Status == "success") {
                    val token = response.Data?.AccessToken
                    if (token != null) {
                        generatePaymentQrCode(token, donationAmount)
                    }else{
                        dismissLoader()
                        showSnackbar(
                            binding.root,
                            "unable to generate QR code! Please try again..."
                        )
                    }
                } else {
                    dismissLoader()
                    showSnackbar(
                        binding.root,
                        "unable to generate QR code! Please try again..."
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
                dismissLoader()
                showSnackbar(binding.root, "Something went wrong! Please try again...")
            }
        }
    }


    private fun generatePaymentQrCode(token: String, donationAmount: Double) {
        val paymentRequest = PaymentRequest(
            acessToken = "Bearer $token",
            transactionReferenceID = generateNumericTransactionReferenceID(),
            amount = donationAmount.toString()
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    paymentRepository.generateQr(sessionManager.getUserId(), sessionManager.getCompanyId(), paymentRequest)
                }
                if (response.Status == "success") {
                    val url = response.Data?.IntentUrl
                    if (url != null) {
                        dismissLoader()
                        runOnUiThread {
                            customQRPopupDialog.setData(
                                donationAmount.toString(),
                                url,
                                paymentRequest.transactionReferenceID,
                                token,
                                binding.editName.text.toString(),
                                binding.editPhno.text.toString(),
                                selectedNakshatra!!,
                                selectDevatha!!

                            )
                            customQRPopupDialog.show(supportFragmentManager, "CustomPopup")
                        }
                    } else {
                        dismissLoader()
                        showSnackbar(
                            binding.root,
                            "unable to generate QR code! Please try again..."
                        )
                    }
                } else {
                    dismissLoader()
                    showSnackbar(
                        binding.root,
                        "unable to generate QR code! Please try again..."
                    )
                }
            } catch (e: Exception) {
                dismissLoader()
                withContext(Dispatchers.Main) {
                    showSnackbar(binding.root, "Something went wrong! Please try again...")
                }
            }
        }

    }

}