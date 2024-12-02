package com.xeniatechnologies.app.templekiosktirupati.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.xenia.templekiosk.data.network.model.PaymentRequest
import com.xenia.templekiosk.data.repository.LoginRepository
import com.xeniatechnologies.app.templekiosktirupati.data.repository.PaymentRepository
import com.xenia.templekiosk.utils.SessionManager
import com.xeniatechnologies.app.templekiosktirupati.utils.common.CommonMethod.dismissLoader
import com.xeniatechnologies.app.templekiosktirupati.utils.common.CommonMethod.generateNumericTransactionReferenceID
import com.xeniatechnologies.app.templekiosktirupati.utils.common.CommonMethod.isInternetAvailable
import com.xeniatechnologies.app.templekiosktirupati.utils.common.CommonMethod.showLoader
import com.xeniatechnologies.app.templekiosktirupati.R
import com.xeniatechnologies.app.templekiosktirupati.databinding.ActivityHomeBinding
import com.xeniatechnologies.app.templekiosktirupati.utils.PrinterConnectionManager
import com.xeniatechnologies.app.templekiosktirupati.utils.common.CommonMethod.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.posprinter.IDeviceConnection
import org.koin.android.ext.android.inject


class HomeActivity : AppCompatActivity() {


    private lateinit var binding: ActivityHomeBinding

    private val sessionManager: SessionManager by inject()
    private val paymentRepository: PaymentRepository by inject()
    private val loginRepository: LoginRepository by inject()
    private var donationAmount: Double? = null
    private var curConnect: IDeviceConnection? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateButtonState()
        PrinterConnectionManager.initialize(applicationContext)
        configPrinter()
        binding.editTxtDonation.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editTxtDonation.windowToken, 0)

        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)


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


        val numberButtons = listOf(
            R.id.btnOne, R.id.btnTwo, R.id.btnThree,
            R.id.btnFour, R.id.btnFive, R.id.btnSix,
            R.id.btnSeven, R.id.btnEight, R.id.btnNine,
            R.id.btnZero
        )


        numberButtons.forEach { buttonId ->
            findViewById<TextView>(buttonId).setOnClickListener {
                appendToFocusedEditText((it as TextView).text.toString())
            }
        }


        binding.btnClear.setOnClickListener {
            clearFocusedEditText()
        }

        binding.btnBack.setOnClickListener {
            removeLastCharacterFromFocusedEditText()
        }

        binding.editTxtDonation.setOnTouchListener { _, _ ->
            binding.editTxtDonation.showSoftInputOnFocus = false
            true
        }


        binding.editTxtDonation.setOnTouchListener { _, _ ->
            binding.editTxtDonation.requestFocus()
            binding.editTxtDonation.showSoftInputOnFocus = false
            true
        }

        binding.edtPhNo.setOnTouchListener { _, _ ->
            binding.edtPhNo.requestFocus()
            binding.edtPhNo.showSoftInputOnFocus = false
            true
        }


        binding.editTxtDonation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                val input = editable.toString()
                val amount = input.toDoubleOrNull()

                if (amount != null) {
                    if (amount > 100000) {
                        binding.editTxtDonation.error = "Donation amount cannot exceed ₹1,00,000"
                        binding.btnPay.isEnabled = false

                    }else{
                        binding.editTxtDonation.error = null
                        binding.btnPay.isEnabled = true
                    }
                }else{
                    binding.editTxtDonation.error = null
                    binding.btnPay.isEnabled = true
                }
            }
        })

    }

    override fun onResume() {
        super.onResume()
        loadCompanyDetails()

    }


    private fun configPrinter() {
        PrinterConnectionManager.getPrinterConnection(this) { success ->
            if (success) {
                curConnect = PrinterConnectionManager.getPrinterConnection(this) { }
            } else {
                Toast.makeText(this, "No USB printer devices found", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadCompanyDetails() {
        if (isInternetAvailable(this)) {
            showLoader(this@HomeActivity, "Loading settings...")
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
            //customInternetAvailabilityDialog.show(supportFragmentManager, "CustomPopup")
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
                ContextCompat.getColor(this, R.color.dark_grey)
            )
        }
    }

    private fun generatePayment() {
        donationAmount = binding.editTxtDonation.text.toString().toDoubleOrNull()
        when {
            donationAmount == null || donationAmount!! <= 0 -> {
                binding.editTxtDonation.error = "Please enter a valid amount"
            }
            donationAmount!! > 100000 -> {
                binding.editTxtDonation.error = "Donation amount cannot exceed ₹1,00,000"
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
                val response = paymentRepository.generateToken(
                    sessionManager.getUserId(),
                    sessionManager.getCompanyId()
                )
                if (response.Status == "success") {
                    val token = response.Data?.AccessToken
                    if (token != null) {
                        generatePaymentQrCode(token, donationAmount)
                    } else {
                        dismissLoader()
                        showSnackbar(
                            binding.root,
                            "unable to generate QR code! Please try again..."
                        )
                    }
                } else {
                    dismissLoader()
                    showSnackbar(binding.root, "unable to generate QR code! Please try again...")
                }
            } catch (e: Exception) {
                dismissLoader()
                showSnackbar(binding.root, "unable to generate QR code! Please try again...")
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
                            putExtra(
                                "transactionReferenceID",
                                paymentRequest.transactionReferenceID
                            )
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
                    showSnackbar(binding.root, "Unable to generate QR code! Please try again...")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun appendToFocusedEditText(text: String) {
        when {
            binding.editTxtDonation.isFocused -> {
                val currentText = binding.editTxtDonation.text.toString()
                binding.editTxtDonation.setText(currentText + text)
                binding.editTxtDonation.setSelection(binding.editTxtDonation.text!!.length)
            }
            binding.edtPhNo.isFocused -> {
                val currentText = binding.edtPhNo.text.toString()
                binding.edtPhNo.setText(currentText + text)
                binding.edtPhNo.setSelection(binding.edtPhNo.text.length)
            }
        }
    }

    private fun clearFocusedEditText() {
        binding.editTxtDonation.text!!.clear()
        binding.edtPhNo.text.clear()
        binding.editTxtDonation.requestFocus()
    }

    private fun removeLastCharacterFromFocusedEditText() {
        when {
            binding.editTxtDonation.isFocused -> {
                val currentText = binding.editTxtDonation.text.toString()
                if (currentText.isNotEmpty()) {
                    val updatedText = currentText.substring(0, currentText.length - 1)
                    binding.editTxtDonation.setText(updatedText)
                    binding.editTxtDonation.setSelection(updatedText.length)
                }
            }
            binding.edtPhNo.isFocused -> {
                val currentText = binding.edtPhNo.text.toString()
                if (currentText.isNotEmpty()) {
                    val updatedText = currentText.substring(0, currentText.length - 1)
                    binding.edtPhNo.setText(updatedText)
                    binding.edtPhNo.setSelection(updatedText.length)
                }
            }
        }
    }


}