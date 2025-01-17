package com.xenia.churchkiosk.ui.screens


import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.xenia.churchkiosk.R
import com.xenia.churchkiosk.data.network.model.PaymentRequest
import com.xenia.churchkiosk.data.repository.PaymentRepository
import com.xenia.churchkiosk.databinding.ActivityDonationBinding
import com.xenia.churchkiosk.ui.dialogue.CustomInactivityDialog
import com.xenia.churchkiosk.ui.dialogue.CustomInternetAvailabilityDialog
import com.xenia.churchkiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.churchkiosk.ui.dialogue.CustomWarningPopupDialog
import com.xenia.churchkiosk.utils.InactivityHandler
import com.xenia.churchkiosk.utils.SessionManager
import com.xenia.churchkiosk.utils.common.CommonMethod.dismissLoader
import com.xenia.churchkiosk.utils.common.CommonMethod.generateNumericTransactionReferenceID
import com.xenia.churchkiosk.utils.common.CommonMethod.isInternetAvailable
import com.xenia.churchkiosk.utils.common.CommonMethod.setLocale
import com.xenia.churchkiosk.utils.common.CommonMethod.showLoader
import com.xenia.churchkiosk.utils.common.CommonMethod.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class DonationActivity : AppCompatActivity(), CustomInactivityDialog.InactivityCallback,
    com.xenia.churchkiosk.data.listeners.InactivityHandlerActivity, CustomInternetAvailabilityDialog.InternetAvailabilityListener {

    private lateinit var binding: ActivityDonationBinding
    private val sessionManager: SessionManager by inject()
    private val paymentRepository: PaymentRepository by inject()
    private val customPopupDialog: CustomWarningPopupDialog by inject()
    private val customQRPopupDialog: CustomQRPopupDialogue by inject()
    private val customInternetAvailabilityDialog: CustomInternetAvailabilityDialog by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private lateinit var inactivityHandler: InactivityHandler
    private lateinit var inactivityDialog: CustomInactivityDialog
    private var donationAmount: Double? = null


    @SuppressLint("ServiceCast", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
        setLocale(this, selectedLanguage)

        initUI()


        inactivityDialog = CustomInactivityDialog(this)
        inactivityHandler = InactivityHandler(this, supportFragmentManager, inactivityDialog,customQRPopupDialog)

        binding.leftHome?.setOnClickListener {
            customPopupDialog.show(supportFragmentManager, "warning_dialog")
        }

        binding.editTxtDonation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnBack?.setOnClickListener {
            removeLastCharacterFromFocusedEditText()
        }
        binding.btnPay.setOnClickListener {
            generatePayment()
        }

        binding.btnClear?.setOnClickListener {
            clearFocusedEditText()
        }

        binding.editTxtDonation.setOnTouchListener { _, _ ->
            binding.editTxtDonation.requestFocus()
            binding.editTxtDonation.showSoftInputOnFocus = false
            true
        }

        binding.editPhno.setOnTouchListener { _, _ ->
            binding.editPhno.requestFocus()
            binding.editPhno.showSoftInputOnFocus = false
            true
        }
    }


    private fun initUI() {

        binding.txtFill.text = getString(R.string.fill_out_your_details)
        binding.txtDonate.text = getString(R.string.donation_amount)
        binding.txtHome.text = getString(R.string.home)
        binding.txtPhno.text = getString(R.string.phone_number)
        binding.txtName.text = getString(R.string.name)

        binding.editTxtDonation.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editTxtDonation.windowToken, 0)

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

    }

    @SuppressLint("SetTextI18n")
    fun appendToFocusedEditText(text: String) {
        when {
            binding.editTxtDonation.isFocused -> {
                val currentText = binding.editTxtDonation.text.toString()
                binding.editTxtDonation.setText(currentText + text)
                binding.editTxtDonation.setSelection(binding.editTxtDonation.text!!.length)
            }
            binding.editPhno.isFocused -> {
                val currentText = binding.editPhno.text.toString()
                binding.editPhno.setText(currentText + text)
                binding.editPhno.setSelection(binding.editPhno.text.length)
            }
        }
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
            binding.editPhno.isFocused -> {
                val currentText = binding.editPhno.text.toString()
                if (currentText.isNotEmpty()) {
                    val updatedText = currentText.substring(0, currentText.length - 1)
                    binding.editPhno.setText(updatedText)
                    binding.editPhno.setSelection(updatedText.length)
                }
            }
        }
    }

    private fun clearFocusedEditText() {
        binding.editTxtDonation.text!!.clear()
        binding.editPhno.text.clear()
        binding.editTxtDonation.requestFocus()
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
                ContextCompat.getColor(this, R.color.light_grey)
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

    override fun onRetryClicked() {
        inactivityHandler.resumeInactivityCheck()
        generatePaymentToken(donationAmount!!)
    }

    override fun onDialogInactive() {
        inactivityHandler.resumeInactivityCheck()
        inactivityHandler.showDialogSafely()
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
