package com.xenia.templekiosk.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.xenia.templekiosk.R

import com.xenia.templekiosk.common.Screen
import com.xenia.templekiosk.data.network.model.PaymentRequest
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.databinding.ActivityDonationBinding
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
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
            finish()
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
                val response = paymentRepository.generateToken("SIBL000000184313")
                if (response.status == "success") {
                    val token = response.data?.accessToken
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
            transactionReferenceID = "assa8dhdfhrd564dskdkDsd",
            amount = donationAmount.toString()
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    paymentRepository.generateQr("SIBL000000184313", paymentRequest)
                }

                if (response.status == "success") {
                    val url = response.data?.intentUrl
                    if (url != null) {
                        withContext(Dispatchers.Main) {
                           showQRPayPopup(this@DonationActivity, url, donationAmount.toString(), Screen.DonationAreaScreen)
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

    @SuppressLint("DefaultLocale", "SetTextI18n")
    fun showQRPayPopup(activity: Activity, url: String, amount: String, screen: String) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        val dialogBuilder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_popup_pay, null)

        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setGravity(Gravity.CENTER)

        val amountTextView = dialogView.findViewById<TextView>(R.id.txt_amount)
        val formattedAmount = String.format("%.2f", amount.toDoubleOrNull() ?: 0.0)

        amountTextView.text = "Amount ₹ $formattedAmount/-"

        val qrCodeImageView = dialogView.findViewById<ImageView>(R.id.qrCodeImageView)
        val qrCodeBitmap = generateUPIQRCode(url)
        qrCodeImageView.setImageBitmap(qrCodeBitmap)

        val fullScreen = dialogView.findViewById<LinearLayout>(R.id.pop_full_screen)
        fullScreen.setOnClickListener {
            val intent = Intent(activity, PaymentActivity::class.java)
            intent.putExtra("ScreenType", screen)
            activity.startActivity(intent)
        }

        val btnHome = dialogView.findViewById<ImageView>(R.id.btnHome)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClose)

        btnHome.setOnClickListener {
            activity.startActivity(Intent(activity, LanguageActivity::class.java))
        }

        btnClose.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun generateUPIQRCode(url: String): Bitmap? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(
                url, BarcodeFormat.QR_CODE, 300, 300
            )
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
    


}