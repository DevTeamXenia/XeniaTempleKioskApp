package com.xeniatechnologies.app.nimahal.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.xeniatechnologies.app.nimahal.R
import com.xeniatechnologies.app.nimahal.data.network.model.PaymentStatus
import com.xeniatechnologies.app.nimahal.data.repository.PaymentRepository
import com.xeniatechnologies.app.nimahal.databinding.ActivityQractivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class QRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQractivityBinding
    private var pollingTimer: CountDownTimer? = null
    private var isCheckingPaymentStatus = false
    private var paymentStatusJob: Job? = null
    private val paymentRepository: PaymentRepository by inject()
    private var amount: String = ""
    private var url: String = ""
    private var transactionReferenceID: String = ""
    private var token: String = ""
    private var name: String = ""
    private var phoneNumber: String = ""
    private var selectedDonation: String = ""

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        amount = intent.getStringExtra("AMOUNT") ?: "0.0"
        url = intent.getStringExtra("URL") ?: ""
        transactionReferenceID = intent.getStringExtra("UPI_REF") ?: ""
        token = intent.getStringExtra("TOKEN") ?: ""
        name = intent.getStringExtra("NAME") ?: ""
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""
        selectedDonation = intent.getStringExtra("SD") ?: ""


        val amountValue: Float = amount.toFloat()
        val formattedAmount = String.format("%.2f", amountValue)
        binding.txtAmount.text = getString(R.string.amount) + " ₹ $formattedAmount /-"
        val qrCodeBitmap = generateUPIQRCode(url)
        binding.imgQR.setImageBitmap(qrCodeBitmap)

        startTimer()

    }

    override fun onDestroy() {
        super.onDestroy()
        pollingTimer?.cancel()
        isCheckingPaymentStatus = false
        paymentStatusJob?.cancel()
        stopCheckingPaymentStatus()
    }

    private fun startTimer() {
        val totalTime = 300000L
        var elapsedTime = 0L
        val pollInterval = 3000L

        pollingTimer = object : CountDownTimer(totalTime, 1000) {
            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime += 1000
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                binding.txtTimer.text = getString(R.string.qr_expire) + " " + timeFormatted
                if (elapsedTime % pollInterval == 0L) {
                    checkPaymentStatus()


                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                stopCheckingPaymentStatus()
                val intent = Intent(applicationContext, LanguageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }.start()
    }

    private fun checkPaymentStatus() {
        if (isCheckingPaymentStatus) return

        isCheckingPaymentStatus = true

        paymentStatusJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                while (isCheckingPaymentStatus) {
                    val paymentRequest = PaymentStatus(
                        pspRefNo = transactionReferenceID,
                        accessToken = "Bearer $token",
                    )
                    val response = withContext(Dispatchers.IO) {
                        paymentRepository.paymentStatus(14, 6, paymentRequest)
                    }

                    if (response.Status == "success") {
                        val status = response.Data?.status
                        val statusDesc = response.Data?.statusDesc
                        if (status != null && statusDesc != null) {
                            if (status == "S" && statusDesc == "Transaction success") {
                                handleTransactionStatus(status)
                                // postPaymentHistory(status)
                                return@launch
                            } else if (status == "F" && statusDesc == "Transaction fail:Debit was failed") {
                                handleTransactionStatus(status)
                                //postPaymentHistory(status)
                                return@launch
                            } else if (status == "F" && statusDesc != "Invalid PsprefNo") {
                                handleTransactionStatus(status)
                                // postPaymentHistory(status)
                                return@launch

                            }
                        }
                    }
                    delay(2000)
                }
            } catch (e: Exception) {
                checkPaymentStatus()

            } finally {
                isCheckingPaymentStatus = false
            }
        }
    }

    private fun handleTransactionStatus(status: String) {
        val intent = Intent(applicationContext, PaymentActivity::class.java).apply {
            putExtra("STATUS", status)
            putExtra("AMOUNT", amount)
            putExtra("TRANS_ID", transactionReferenceID)
            putExtra("NAME", name)
            putExtra("PHONE_NUMBER",phoneNumber)
        }
        startActivity(intent)
        finish()
    }

    private fun stopCheckingPaymentStatus() {
        isCheckingPaymentStatus = false
        paymentStatusJob?.cancel()
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