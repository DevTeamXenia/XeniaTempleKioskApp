package com.xeniatechnologies.app.templekiosktirupati.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.xenia.templekiosk.data.network.model.PaymentRequest
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.utils.SessionManager
import com.xenia.templekiosk.utils.common.CommonMethod.dismissLoader
import com.xenia.templekiosk.utils.common.CommonMethod.generateNumericTransactionReferenceID
import com.xenia.templekiosk.utils.common.CommonMethod.isInternetAvailable
import com.xenia.templekiosk.utils.common.CommonMethod.showLoader
import com.xenia.templekiosk.utils.common.CommonMethod.showSnackbar
import com.xeniatechnologies.app.templekiosktirupati.R
import com.xeniatechnologies.app.templekiosktirupati.databinding.ActivityHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class HomeActivity : AppCompatActivity() {


    private lateinit var binding: ActivityHomeBinding

    private val sessionManager: SessionManager by inject()
    private val paymentRepository: PaymentRepository by inject()
    private var selectedNakshatra: String? = null
    private var selectDevatha: String? = null
    private var donationAmount: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        updateButtonState()

        binding.editTxtDonation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnPay.setOnClickListener {
            generatePayment()
        }

    }


    private fun updateButtonState() {
        val inputText = binding.editTxtDonation.text.toString().trim()

        if (inputText.isNotEmpty()) {
            binding.btnPay.isEnabled = true
            binding.btnPay.setBackgroundColor(
                ContextCompat.getColor(this, R.color.primaryColor)
            )
        } else {
            binding.btnPay.isEnabled = false
            binding.btnPay.setBackgroundColor(
                ContextCompat.getColor(this, R.color.gray)
            )
        }
    }

    private fun generatePayment() {
        donationAmount = binding.editTxtDonation.text.toString().toDoubleOrNull()
        when {
            donationAmount == null || donationAmount!! <= 0 -> {
                showSnackbar(binding.root, "Please enter a valid amount")
            }
            else -> {
                if (isInternetAvailable(this)) {
                    showLoader(this@HomeActivity, "Loading your QR code... Please wait.")
                    generatePaymentToken(donationAmount!!)
                } else {
                    //customInternetAvailabilityDialog.show(supportFragmentManager, "warning_dialog")
                }
            }
        }
    }

    private fun generatePaymentToken(donationAmount: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = paymentRepository.generateToken(sessionManager.getUserId(), sessionManager.getCompanyId())
                if (response.Status == "success") {
                    val token = response.Data?.AccessToken
                    if (token != null) {
                        generatePaymentQrCode(token, donationAmount)
                    } else {
                        dismissLoader()
                        showSnackbar(binding.root, "unable to generate QR code! Please try again...")
                    }
                } else {
                    dismissLoader()
                    showSnackbar(binding.root, "unable to generate QR code! Please try again...")
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
                    paymentRepository.generateQr(
                        sessionManager.getUserId(),
                        sessionManager.getCompanyId(),
                        paymentRequest
                    )
                }
                if (response.Status == "success") {
                    val url = response.Data?.IntentUrl
                    if (url != null) {
                        dismissLoader()
                        val intent = Intent(this@HomeActivity, QRActivity::class.java).apply {
                            putExtra("donationAmount", donationAmount.toString())
                            putExtra("url", url)
                            putExtra("transactionReferenceID", paymentRequest.transactionReferenceID)
                            putExtra("token", token)
                            putExtra("phone", binding.edtPhNo.text.toString())
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        dismissLoader()
                        showSnackbar(
                            binding.root,
                            "Unable to generate QR code! Please try again..."
                        )
                    }
                } else {
                    dismissLoader()
                    showSnackbar(
                        binding.root,
                        "Unable to generate QR code! Please try again..."
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