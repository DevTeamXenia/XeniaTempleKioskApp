package com.xenia.churchkiosk.ui.screens


import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.xenia.churchkiosk.R
import com.xenia.churchkiosk.data.network.model.PaymentRequest
import com.xenia.churchkiosk.data.repository.PaymentRepository
import com.xenia.churchkiosk.data.repository.VazhipaduRepository
import com.xenia.churchkiosk.databinding.ActivitySummaryBinding
import com.xenia.churchkiosk.ui.adapter.PersonAdapter
import com.xenia.churchkiosk.ui.adapter.PersonItemAdapter
import com.xenia.churchkiosk.ui.dialogue.CustomInternetAvailabilityDialog
import com.xenia.churchkiosk.ui.dialogue.CustomVazhipaduQRPopupDialogue
import com.xenia.churchkiosk.utils.SessionManager
import com.xenia.churchkiosk.utils.common.CommonMethod.dismissLoader
import com.xenia.churchkiosk.utils.common.CommonMethod.generateNumericTransactionReferenceID
import com.xenia.churchkiosk.utils.common.CommonMethod.isInternetAvailable
import com.xenia.churchkiosk.utils.common.CommonMethod.showLoader
import com.xenia.churchkiosk.utils.common.CommonMethod.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject


class SummaryActivity : AppCompatActivity(), PersonItemAdapter.OnItemDeleteListener {

    private lateinit var binding: ActivitySummaryBinding
    private lateinit var personAdapter: PersonAdapter
    private val vazhipaduRepository: VazhipaduRepository by inject()
    private val paymentRepository: PaymentRepository by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private val sessionManager: SessionManager by inject()
    private val customVazhipaduQRPopupDialog: CustomVazhipaduQRPopupDialogue by inject()
    private val customInternetAvailabilityDialog: CustomInternetAvailabilityDialog by inject()
    private var donationAmount :Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("USER_NAME")

        onBackPressedDispatcher.addCallback(this) {
            lifecycleScope.launch {
                    val intent = Intent(this@SummaryActivity, VazhipaduActivity::class.java).apply {
                        putExtra("USER_NAME", userName)
                    }
                    startActivity(intent)
                    finish()
            }
        }

        lifecycleScope.launch {
            val distinctPersons = vazhipaduRepository.getDistinctPersonsWithOfferings()
            personAdapter = PersonAdapter(
                personList = distinctPersons,
                sharedPreferences,
                onItemDeleteListener = this@SummaryActivity
            )
            binding.relSummary.layoutManager = LinearLayoutManager(this@SummaryActivity)
            binding.relSummary.adapter = personAdapter
        }

        updateCartCount()

        binding.btnPay.setOnClickListener {
            generatePayment()
        }

        binding.addMore.setOnClickListener {
            startActivity(Intent(applicationContext,VazhipaduActivity::class.java))
            finish()
        }

    }

    override fun onItemDelete(name: String, offeringId: Int) {
        lifecycleScope.launch {
            vazhipaduRepository.removeOffering(name, offeringId)
            val updatedPersons = vazhipaduRepository.getDistinctPersonsWithOfferings()
            personAdapter.updatePersonList(updatedPersons)
            updateCartCount()
        }
    }

      @SuppressLint("DefaultLocale")
      private fun updateCartCount() {
        lifecycleScope.launch {
            donationAmount = vazhipaduRepository.getTotalAmount()
            val formattedAmount = String.format("%.2f", donationAmount)
            val btnPay: MaterialButton = findViewById(R.id.btn_pay)
            btnPay.text = getString(R.string.pay_vazhipadu, formattedAmount)

        }
    }

     private fun generatePayment() {
          when {
              donationAmount <= 0 -> {
                  showSnackbar(binding.root, "Cart is empty!")
              }
              else -> {
                  if (isInternetAvailable(this)) {
                      showLoader(this@SummaryActivity, "Loading your QR code... Please wait.")
                      generatePaymentToken(donationAmount)
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
                              customVazhipaduQRPopupDialog.setData(
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
                      showSnackbar(binding.root, e.toString())
                  }
              }
          }
      }
}

