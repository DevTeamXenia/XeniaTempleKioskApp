package com.xeniatechnologies.app.templekiosktirupati.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import net.posprinter.IDeviceConnection
import net.posprinter.IPOSListener
import net.posprinter.POSConnect
import net.posprinter.POSConst
import net.posprinter.POSPrinter
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQractivityBinding
    private var pollingTimer: CountDownTimer? = null
    private var paymentStatusJob: Job? = null
    private val paymentRepository: PaymentRepository by inject()
    private val sessionManager: SessionManager by inject()
    private var isCheckingPaymentStatus = false

    private var curConnect: IDeviceConnection? = null

    private var amount: Double = 0.0
    private var url: String = ""
    private var transactionReferenceID: String = ""
    private var token: String = ""
    private var phno: String = ""
    private var formattedAmount = ""
    private var orderID: String? = null


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

        formattedAmount = String.format("%.2f", amount)

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

        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_payments)
            .apply(
                RequestOptions()
                    .override(Target.SIZE_ORIGINAL)
                    .fitCenter()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .priority(Priority.HIGH)
            )
            .into(binding.imgPay)

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
        val totalTime = 60000L
        val pollInterval = 3000L
        var elapsedTime = 0L

        pollingTimer = object : CountDownTimer(totalTime, 1000) {
            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime += 1000
                val secondsRemaining = millisUntilFinished / 1000

                val formattedTime = String.format("%02d", secondsRemaining)

                binding.txtTimer.text = formattedTime

                if (elapsedTime % pollInterval == 0L) {
                    checkPaymentStatus()
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                stopCheckingPaymentStatus()


                binding.relQr.visibility = View.GONE
                binding.relSuccessStatus.visibility = View.GONE
                binding.relFailedStatus.visibility = View.GONE
                binding.relExpireQr.visibility = View.VISIBLE

                val initialTime = 3
                binding.btnSessionCancel.text = "Cancel($initialTime)"

                object : CountDownTimer((initialTime * 1000).toLong(), 1000) {
                    @SuppressLint("SetTextI18n")
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsLeft = (millisUntilFinished / 1000).toInt()
                        binding.btnSessionCancel.text = "Cancel($secondsLeft)"
                    }

                    override fun onFinish() {
                        navigateToHomeScreen()
                    }
                }.start()
            }
        }.start()
    }


    private fun stopCheckingPaymentStatus() {
        isCheckingPaymentStatus = false
        paymentStatusJob?.cancel()
        pollingTimer?.cancel()
        pollingTimer = null
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
                    orderID = response.data.orderId.toString()
                    handleTransactionStatus(status)
                } else {
                    postPaymentHistory(status)
                }
            } catch (e: Exception) {
                postPaymentHistory(status)
            }
        }
    }



    @SuppressLint("SetTextI18n")
    private fun handleTransactionStatus(status: String) {

        if(status == "S"){
            configPrinter()
            binding.relQr.visibility = View.GONE
            binding.relSuccessStatus.visibility = View.VISIBLE
            binding.relFailedStatus.visibility = View.GONE
            binding.relExpireQr.visibility = View.GONE

            val initialTime = 5
            binding.btnSessionCancel.text = "Cancel($initialTime)"

            object : CountDownTimer((initialTime * 1000).toLong(), 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    val secondsLeft = (millisUntilFinished / 1000).toInt()
                    binding.btnSuccess.text = "Cancel($secondsLeft)"
                }

                override fun onFinish() {
                    navigateToHomeScreen()
                }
            }.start()
        }else{
            binding.relQr.visibility = View.GONE
            binding.relSuccessStatus.visibility = View.GONE
            binding.relFailedStatus.visibility = View.VISIBLE
            binding.relExpireQr.visibility = View.GONE
        }

    }


    private fun configPrinter() {
        try {
            POSConnect.init(applicationContext)
            val entries = POSConnect.getUsbDevices(applicationContext)

            if (entries.isNotEmpty()) {
                try {
                    printReceipt(entries[0])
                } catch (e: Exception) {
                    Toast.makeText(this, "Error printing receipt: ${e.message}", Toast.LENGTH_LONG)
                        .show()

                }
            } else {
                navigateToHomeScreen()
                Toast.makeText(this, "No USB printer devices found", Toast.LENGTH_LONG).show()
            }
        } catch (_: Exception) {
            navigateToHomeScreen()

        }
    }

    private fun printReceipt(pathName: String) {
        connectUSB(pathName)
    }

    private fun connectUSB(pathName: String) {
        curConnect?.close()
        curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_USB)
        curConnect!!.connect(pathName, connectListener)
    }


    private fun navigateToHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }


    private val connectListener = IPOSListener { code, _ ->
        when (code) {
            POSConnect.CONNECT_SUCCESS -> {
                initReceiptPrint()
            }

            POSConnect.CONNECT_FAIL -> {
                navigateToHomeScreen()
            }

            POSConnect.CONNECT_INTERRUPT -> {
                navigateToHomeScreen()

            }

            POSConnect.SEND_FAIL -> {
                navigateToHomeScreen()

            }

            POSConnect.USB_DETACHED -> {
                navigateToHomeScreen()

            }

            POSConnect.USB_ATTACHED -> {
                navigateToHomeScreen()

            }
        }
    }


    @SuppressLint("DefaultLocale")
    private fun initReceiptPrint() {
        val cd = sessionManager.getCompanyDetails()
        if (cd != null) {
            val currentDate = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(
                Date()
            )
            val drawableDevasam = ContextCompat.getDrawable(this, R.drawable.print_header_logo)
            val bitmapDevasam = (drawableDevasam as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val drawableDevi = ContextCompat.getDrawable(this, R.drawable.print_bottom_logo)
            val bitmapDevi = (drawableDevi as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

            POSPrinter(curConnect)
                .printBitmap(bitmapDevasam, POSConst.ALIGNMENT_CENTER, 590)
                .printText("${cd.companyName}\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT)
                .feedLine(1)
                .printText("${cd.companyAddress}\n", POSConst.ALIGNMENT_CENTER, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printText("${cd.companyPhone1}\n", POSConst.ALIGNMENT_CENTER, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .feedLine(1)
                .printText("Receipt No : $orderID\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printText("Date : $currentDate\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printText("Contact No : $phno\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .feedLine(2)
                .printText("Amount : $formattedAmount\n\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD , POSConst.TXT_2WIDTH or POSConst.TXT_2HEIGHT)
                .printText("UPI Reference No: $transactionReferenceID\n\n", POSConst.ALIGNMENT_CENTER, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printBitmap(bitmapDevi, POSConst.ALIGNMENT_CENTER, 500)
                .cutHalfAndFeed(1)
        }
    }



}
