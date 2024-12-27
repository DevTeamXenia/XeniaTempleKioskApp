package com.xeniatechnologies.app.nimahal.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xeniatechnologies.app.nimahal.data.network.model.PaymentRequest
import com.xeniatechnologies.app.nimahal.data.repository.PaymentRepository
import com.xeniatechnologies.app.nimahal.databinding.ActivityDonationBinding

import com.xeniatechnologies.app.nimahal.dialogue.CustomInternetAvailabilityDialog
import com.xeniatechnologies.app.nimahal.utils.Methods.dismissLoader
import com.xeniatechnologies.app.nimahal.utils.Methods.generateNumericTransactionReferenceID
import com.xeniatechnologies.app.nimahal.utils.Methods.isInternetAvailable
import com.xeniatechnologies.app.nimahal.utils.Methods.showLoader
import com.xeniatechnologies.app.nimahal.utils.Methods.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class DonationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonationBinding
    private val customInternetAvailabilityDialog: CustomInternetAvailabilityDialog by inject()
    private val paymentRepository: PaymentRepository by inject()
    private var donationAmount: Double? = null
    private var selectedDonation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()
    }

    private fun setListener() {
        binding.btnPay.setOnClickListener {
            generatePayment()
        }

    }


    private fun generatePayment() {
        donationAmount = binding.editEnterAmount.text.toString().toDoubleOrNull()
        when {
            binding.editName.text.toString().isEmpty() -> {
                showSnackbar(binding.root, "Please enter your name")
            }
            donationAmount == null || donationAmount!! <= 0 -> {
                showSnackbar(binding.root, "Please enter a valid amount")
            }
            else -> {
                if (isInternetAvailable(this)) {
                    showLoader(this@DonationActivity, "Loading your QR code... Please wait.")
                    generatePaymentToken(donationAmount!!)
                } else {
                    customInternetAvailabilityDialog.show(supportFragmentManager, "warning_dialog")
                }
            }
        }
    }

    private fun generatePaymentToken(donationAmount: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = paymentRepository.generateToken(14, 6)
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
                    paymentRepository.generateQr(14, 6, paymentRequest)
                }
                if (response.Status == "success") {
                    val url = response.Data?.IntentUrl
                    if (url != null) {
                        dismissLoader()
                        runOnUiThread {
                            val intent = Intent(applicationContext, QRActivity::class.java)
                            intent.putExtra("AMOUNT", donationAmount.toString())
                            intent.putExtra("URL", url)
                            intent.putExtra("UPI_REF",  paymentRequest.transactionReferenceID)
                            intent.putExtra("TOKEN", token)
                            intent.putExtra("NAME",binding.editName.text.toString())
                            intent.putExtra("PHONE_NUMBER",binding.editPhoneNumber.text.toString())
                            intent.putExtra("SD",selectedDonation)
                            startActivity(intent)
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