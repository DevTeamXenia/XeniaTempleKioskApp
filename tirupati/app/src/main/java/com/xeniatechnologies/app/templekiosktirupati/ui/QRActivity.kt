package com.xeniatechnologies.app.templekiosktirupati.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.xenia.templekiosk.data.network.model.OrderRequest
import com.xenia.templekiosk.data.network.model.PaymentStatus
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.utils.SessionManager
import com.xenia.templekiosk.utils.common.CommonMethod.convertNumberToWords
import com.xeniatechnologies.app.templekiosktirupati.R
import com.xeniatechnologies.app.templekiosktirupati.databinding.ActivityQractivityBinding
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
    private var paymentStatusJob: Job? = null
    private val paymentRepository: PaymentRepository by inject()
    private val sessionManager: SessionManager by inject()
    private var isCheckingPaymentStatus = false

    private var amount: Double = 0.0
    private var url: String = ""
    private var transactionReferenceID: String = ""
    private var token: String = ""
    private var phno: String = ""


    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        amount = intent.getStringExtra("donationAmount")?.toDoubleOrNull() ?: 0.0
        url = intent.getStringExtra("url").toString()
        transactionReferenceID = intent.getStringExtra("transactionReferenceID").toString()
        token = intent.getStringExtra("token").toString()
        phno = intent.getStringExtra("phone").toString()

        val formattedAmount = String.format("%.2f", amount)

        binding.txtAmount.text = getString(R.string.amount) + formattedAmount + "/-"

        val donationInWords = convertNumberToWords(amount)
        binding.txtWords.text = donationInWords


        val qrCodeBitmap = generateUPIQRCode(url)
        binding.imgQRCode.setImageBitmap(qrCodeBitmap)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        startTimer()


        binding.btnCancel.setOnClickListener {
            startActivity(Intent(applicationContext, HomeActivity::class.java))
            finish()
        }

        binding.btnSessionCancel.setOnClickListener {
            startActivity(Intent(applicationContext, HomeActivity::class.java))
            finish()
        }


        Glide.with(this)
            .asGif()
            .load(R.drawable.time)
            .apply(
                RequestOptions()
                    .override(Target.SIZE_ORIGINAL)
                    .fitCenter()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .priority(Priority.HIGH)
            )
            .into(binding.imgBackground)

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

    private fun startTimer() {
        val totalTime = 30000L
        var elapsedTime = 0L
        val pollInterval = 3000L

        pollingTimer = object : CountDownTimer(totalTime, 1000) {
            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime += 1000
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                binding.txtTimer.text = timeFormatted
                if (elapsedTime % pollInterval == 0L) {
                    checkPaymentStatus()


                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                stopCheckingPaymentStatus()
                binding.relQr.visibility = View.GONE
                binding.relStatus.visibility = View.GONE
                binding.relExpireQr.visibility = View.VISIBLE

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
                        paymentRepository.paymentStatus(
                            sessionManager.getUserId(),
                            sessionManager.getCompanyId(),
                            paymentRequest
                        )
                    }

                    if (response.Status == "success") {
                        val status = response.Data?.status
                        val statusDesc = response.Data?.statusDesc
                        if (status != null && statusDesc != null) {
                            if (status == "S" && statusDesc == "Transaction success") {
                                postPaymentHistory(status)
                                return@launch

                            } else if (status == "F" && statusDesc != "Invalid PsprefNo") {
                                postPaymentHistory(status)
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

    private fun postPaymentHistory(status: String) {
        val orderRequest = OrderRequest(
            transactionId = transactionReferenceID,
            phoneNumber = phno,
            orderAmount = amount,
            paymentStatus = status,
            paymentMethod = "UPI"
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    paymentRepository.postOrder(
                        sessionManager.getUserId(),
                        sessionManager.getCompanyId(),
                        orderRequest
                    )
                }
                if (response.status == "success") {
                    //handleTransactionStatus(status, response.data.orderId)
                } else {
                    postPaymentHistory(status)
                }
            } catch (e: Exception) {
                postPaymentHistory(status)
            }
        }
    }


    private fun stopCheckingPaymentStatus() {
        isCheckingPaymentStatus = false
        paymentStatusJob?.cancel()
    }



}
