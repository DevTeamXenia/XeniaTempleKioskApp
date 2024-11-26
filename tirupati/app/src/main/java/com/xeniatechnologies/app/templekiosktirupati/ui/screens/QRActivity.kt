package com.xeniatechnologies.app.templekiosktirupati.ui.screens

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.xenia.templekiosk.data.network.model.OrderRequest
import com.xenia.templekiosk.data.network.model.PaymentStatus
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.utils.SessionManager
import com.xeniatechnologies.app.templekiosktirupati.utils.common.CommonMethod.convertNumberToWords
import com.xeniatechnologies.app.templekiosktirupati.R
import com.xeniatechnologies.app.templekiosktirupati.databinding.ActivityQractivityBinding
import com.xeniatechnologies.app.templekiosktirupati.utils.PrinterConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.posprinter.IDeviceConnection
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
    private var blinkAnimator: ObjectAnimator? = null

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
            stopCheckingPaymentStatus()
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

                if (secondsRemaining < 10) {
                    binding.txtTimer.setTextColor(Color.RED)
                    startBlinkingText()
                } else {
                    binding.txtTimer.setTextColor(Color.WHITE)
                    stopBlinkingText()
                }

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

                startCancelButtonCountdown()
            }
        }.start()
    }


    private fun startBlinkingText() {
        if (blinkAnimator == null) {
            blinkAnimator = ObjectAnimator.ofFloat(binding.txtTimer, View.ALPHA, 1f, 0f).apply {
                duration = 300
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }
        }
    }

    private fun stopBlinkingText() {
        blinkAnimator?.cancel()
        blinkAnimator = null
        binding.txtTimer.alpha = 1f
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

    @SuppressLint("SetTextI18n")
    private fun startCancelButtonCountdown() {
        val initialTime = 6
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


    private fun stopCheckingPaymentStatus() {
        isCheckingPaymentStatus = false
        paymentStatusJob?.cancel()
        pollingTimer?.cancel()
        pollingTimer = null
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
        if (status == "S") {
            configPrinter()
            binding.relQr.visibility = View.GONE
            binding.relSuccessStatus.visibility = View.VISIBLE
            binding.relFailedStatus.visibility = View.GONE
            binding.relExpireQr.visibility = View.GONE

            val initialTime = 6
            binding.btnSuccess.text = "Cancel($initialTime)"

            object : CountDownTimer((initialTime * 1000).toLong(), 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    val secondsLeft = (millisUntilFinished / 1000).toInt()
                    binding.btnSuccess.text = "Cancel($secondsLeft)"
                }

                override fun onFinish() {
                    stopCheckingPaymentStatus()
                    navigateToHomeScreen()
                }
            }.start()
        } else if(status == "F"){
            binding.relQr.visibility = View.GONE
            binding.relSuccessStatus.visibility = View.GONE
            binding.relFailedStatus.visibility = View.VISIBLE
            binding.relExpireQr.visibility = View.GONE
            val initialTime = 6
            binding.btnCancel.text = "Cancel($initialTime)"

            object : CountDownTimer((initialTime * 1000).toLong(), 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    val secondsLeft = (millisUntilFinished / 1000).toInt()
                    binding.btnCancel.text = "Cancel($secondsLeft)"
                }

                override fun onFinish() {
                    stopCheckingPaymentStatus()
                    //navigateToHomeScreen()
                }
            }.start()
        }

    }


    private fun configPrinter() {
        PrinterConnectionManager.getPrinterConnection(this) { success ->
            if (success) {
                curConnect = PrinterConnectionManager.getPrinterConnection(this) { }
                initReceiptPrint()
            } else {
                Toast.makeText(this, "No USB printer devices found", Toast.LENGTH_LONG).show()
                navigateToHomeScreen()
            }
        }
    }


    private fun navigateToHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    @SuppressLint("DefaultLocale")
    private fun initReceiptPrint() {
        val cd = sessionManager.getCompanyDetails()
        if (cd != null) {
            val currentDate = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(
                Date()
            )
            val qrContent = listOf(
                "Ref.No: $transactionReferenceID",
                "GST No.: 37AAATT4126G3ZI",
                "Amount(Rs.): $formattedAmount",
                "Tr.Date: $currentDate",
                "MobileNo: $phno"
            ).joinToString(separator = ", ")


            val qrBitmap = generateQRCode(qrContent, 200, 200)

            val headerImg = ContextCompat.getDrawable(this, R.drawable.print_header_logo)
            val bitmapHeader =
                (headerImg as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)


            val footerImg = ContextCompat.getDrawable(this, R.drawable.print_bottom_logo)
            val bitmapFooter =
                (footerImg as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

            POSPrinter(curConnect)
                .printBitmap(bitmapHeader, POSConst.ALIGNMENT_CENTER, 580)
                .printText(
                    "\n",
                    POSConst.ALIGNMENT_CENTER,
                    POSConst.FNT_BOLD,
                    POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT
                )
                .printBitmap(qrBitmap, POSConst.ALIGNMENT_CENTER, 200)
                .printText(
                    "\n",
                    POSConst.ALIGNMENT_CENTER,
                    POSConst.FNT_BOLD,
                    POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT
                )
                .printText(
                    " Rec.No: $transactionReferenceID\n\n",
                    POSConst.ALIGNMENT_LEFT,
                    POSConst.FNT_BOLD,
                    POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT
                )
                .printText(
                    " Tr. Date: $currentDate\n\n",
                    POSConst.ALIGNMENT_LEFT,
                    POSConst.FNT_BOLD,
                    POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT
                )
                .printText(
                    " Amount(Rs.):$formattedAmount/-\n\n",
                    POSConst.ALIGNMENT_LEFT,
                    POSConst.FNT_BOLD,
                    POSConst.TXT_2WIDTH or POSConst.TXT_2HEIGHT
                )
                .printBitmap(bitmapFooter, POSConst.ALIGNMENT_CENTER, 600)
                .cutHalfAndFeed(1)
        }

    }

    private fun generateQRCode(content: String, width: Int, height: Int): Bitmap {
        val bitMatrix: BitMatrix =
            MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height)
        val startX = bitMatrix.enclosingRectangle[0]
        val startY = bitMatrix.enclosingRectangle[1]
        val qrWidth = bitMatrix.enclosingRectangle[2]
        val qrHeight = bitMatrix.enclosingRectangle[3]

        val bitmap = Bitmap.createBitmap(qrWidth, qrHeight, Bitmap.Config.RGB_565)
        for (x in 0 until qrWidth) {
            for (y in 0 until qrHeight) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x + startX, y + startY]) Color.BLACK else Color.WHITE
                )
            }
        }
        return bitmap
    }

    private fun generateUPIQRCode(url: String): Bitmap? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(
                url, BarcodeFormat.QR_CODE, 400, 400
            )
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
}
