package com.xenia.templekiosk.ui.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.xenia.templekiosk.R

import com.xenia.templekiosk.data.network.model.PaymentRequest
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.databinding.ActivityDonationBinding
import com.xenia.templekiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.templekiosk.ui.dialogue.CustomWarningPopupDialog
import com.xenia.templekiosk.utils.common.CommonMethod.generateNumericTransactionReferenceID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class DonationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonationBinding
    private lateinit var payNow: Button
    private lateinit var btnHome: RelativeLayout
    private lateinit var editDonation: EditText
    private lateinit var textViewList: MutableList<TextView>
    private var selectedTextView: TextView? = null
    private val normalBackgroundResource = R.drawable.textview_board
    private val selectedBackgroundResource = R.drawable.textview_selected
    private val paymentRepository: PaymentRepository by inject()
    private val customPopupDialog: CustomWarningPopupDialog by inject()
    private val customQRPopupDialog: CustomQRPopupDialogue by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

        initUI()

        for (textView in textViewList) {
            textView.setOnClickListener {
                handleTextViewSelection(textView)
            }
        }

        btnHome.setOnClickListener {
            customPopupDialog.show(supportFragmentManager, "CustomPopup")
        }

        payNow.setOnClickListener {
            val donationAmount = editDonation.text.toString().toDoubleOrNull()

            when {
                donationAmount == null || donationAmount <= 0 -> {
                    Toast.makeText(applicationContext, "Please enter a valid amount greater than 0", Toast.LENGTH_SHORT).show()
                }
                selectedTextView == null -> {
                    Toast.makeText(applicationContext, "Please select a star", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    fetchQr(donationAmount)
                }
            }
        }
    }


    private fun initUI() {
        editDonation = findViewById(R.id.edit_txt_donation)
        editDonation.requestFocus()
        payNow = findViewById(R.id.btn_pay)
        textViewList = mutableListOf()
        textViewList.add(findViewById(R.id.Ashwini))
        textViewList.add(findViewById(R.id.Bharani))
        textViewList.add(findViewById(R.id.Krittika))
        textViewList.add(findViewById(R.id.Rohini))
        textViewList.add(findViewById(R.id.Mrigashirsha))
        textViewList.add(findViewById(R.id.Ardra))
        textViewList.add(findViewById(R.id.Punarvasu))
        textViewList.add(findViewById(R.id.Pushya))
        textViewList.add(findViewById(R.id.Ashlesha))
        textViewList.add(findViewById(R.id.Magha))
        textViewList.add(findViewById(R.id.PurvaPhalguni))
        textViewList.add(findViewById(R.id.UttaraPhalguni))
        textViewList.add(findViewById(R.id.Hasta))
        textViewList.add(findViewById(R.id.Chitra))
        textViewList.add(findViewById(R.id.Swati))
        textViewList.add(findViewById(R.id.Vishaka))
        textViewList.add(findViewById(R.id.Anuradha))
        textViewList.add(findViewById(R.id.Jyeshta))
        textViewList.add(findViewById(R.id.Moola))
        textViewList.add(findViewById(R.id.PurvaAshadha))
        textViewList.add(findViewById(R.id.UttaraAshada))
        textViewList.add(findViewById(R.id.Shravana))
        textViewList.add(findViewById(R.id.Dhanistha))
        textViewList.add(findViewById(R.id.Shatabhisaa))
        textViewList.add(findViewById(R.id.PurvaBhadrapada))
        textViewList.add(findViewById(R.id.UttaraBhadrapada))
        textViewList.add(findViewById(R.id.Revati))

        btnHome = findViewById(R.id.left_home)
    }

    private fun handleTextViewSelection(textView: TextView) {
        selectedTextView?.setBackgroundResource(normalBackgroundResource)
        selectedTextView = textView
        selectedTextView?.setBackgroundResource(selectedBackgroundResource)
    }


    private fun fetchQr(donationAmount: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = paymentRepository.generateToken(1,2)
                if (response.Status == "success") {
                    val token = response.Data?.AccessToken
                    if(token != null){
                        generateQr(token,donationAmount)
                    }

                } else {
                   /* dismissLoader()
                    showSnackBar(binding.root, "Error occurred! Please try again")*/
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
              /*  dismissLoader()
                showSnackBar(binding.root, "Something went wrong! Please try again")*/
            }
        }
    }

    private fun generateQr(token: String, donationAmount: Double) {
        val paymentRequest = PaymentRequest(
            acessToken = "Bearer $token",
            transactionReferenceID = generateNumericTransactionReferenceID(),
            amount = donationAmount.toString()
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    paymentRepository.generateQr(1,2, paymentRequest)
                }

                if (response.Status == "success") {
                    val url = response.Data?.IntentUrl
                    if (url != null) {
                        runOnUiThread {
                            customQRPopupDialog.setData(donationAmount.toString(), url,paymentRequest.transactionReferenceID,token)
                            customQRPopupDialog.show(supportFragmentManager, "CustomPopup")
                        }
                    }
                } else {
                    // Handle error, such as dismissing loaders or showing snackbar
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

}