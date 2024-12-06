package com.xenia.templekiosk.ui.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xenia.templekiosk.data.network.model.CartItem
import com.xenia.templekiosk.data.network.model.PaymentRequest
import com.xenia.templekiosk.data.network.model.PersonWithItems
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.databinding.ActivitySummaryBinding
import com.xenia.templekiosk.ui.adapter.PersonAdapter
import com.xenia.templekiosk.ui.dialogue.CustomInternetAvailabilityDialog
import com.xenia.templekiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.templekiosk.ui.dialogue.CustomVazhipaduQRPopupDialogue
import com.xenia.templekiosk.utils.SessionManager
import com.xenia.templekiosk.utils.common.CommonMethod.dismissLoader
import com.xenia.templekiosk.utils.common.CommonMethod.generateNumericTransactionReferenceID
import com.xenia.templekiosk.utils.common.CommonMethod.isInternetAvailable
import com.xenia.templekiosk.utils.common.CommonMethod.showLoader
import com.xenia.templekiosk.utils.common.CommonMethod.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class SummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummaryBinding
    private val sessionManager: SessionManager by inject()
    private val paymentRepository: PaymentRepository by inject()
    private val customVazhipaduQRPopupDialog: CustomVazhipaduQRPopupDialogue by inject()
    private val customInternetAvailabilityDialog: CustomInternetAvailabilityDialog by inject()
    private lateinit var personAdapter: PersonAdapter
    private var allCartItems: MutableList<CartItem> = mutableListOf()
    private var donationAmount: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cartJson = intent.getStringExtra("cartItems") ?: "[]"
        allCartItems = Gson().fromJson(cartJson, object : TypeToken<MutableList<CartItem>>() {}.type)

        val distinctPersons = allCartItems
            .filter { !it.personName.isNullOrEmpty() && !it.personStar.isNullOrEmpty() }
            .groupBy { Pair(it.personName!!, it.personStar!!) }
            .map { (person, items) ->
                PersonWithItems(
                    personName = person.first,
                    personStar = person.second,
                    items = items
                )
            }

        personAdapter = PersonAdapter(distinctPersons, { removedItem ->
            handleItemRemoved(removedItem)
        }, sessionManager)


        binding.relSummary.apply {
            layoutManager = LinearLayoutManager(this@SummaryActivity)
            adapter = personAdapter
        }

        binding.btnPay.setOnClickListener {
            generatePayment()
        }

        updateTotalAmount()
    }

    private fun handleItemRemoved(item: CartItem) {
        allCartItems.remove(item)
        updateTotalAmount()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun updateTotalAmount() {
        donationAmount = allCartItems.sumOf { it.amount }
        binding.btnPay.text = "Pay ₹${String.format("%.2f", donationAmount)}"
    }

    private fun generatePayment() {
        when {
            donationAmount == null || donationAmount!! <= 0 -> {
                showSnackbar(binding.root, "Please enter a valid amount")
            }
            else -> {
                if (isInternetAvailable(this)) {
                    showLoader(this@SummaryActivity, "Loading your QR code... Please wait.")
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
                            customVazhipaduQRPopupDialog.setData(
                                allCartItems,
                                paymentRequest.transactionReferenceID,
                                token,
                                url,
                                donationAmount.toString()
                            )
                            customVazhipaduQRPopupDialog.show(supportFragmentManager, "CustomPopup")
                        }
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

