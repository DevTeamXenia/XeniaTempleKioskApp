package com.xenia.templekiosk.ui.dialogue

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.OrderRequest
import com.xenia.templekiosk.data.network.model.PaymentStatus
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.ui.screens.LanguageActivity
import com.xenia.templekiosk.ui.screens.PaymentActivity
import com.xenia.templekiosk.utils.SessionManager
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class CustomQRPopupDialogue : DialogFragment() {

    private lateinit var timerTextView: TextView
    private lateinit var amountTextView: TextView
    private lateinit var qrCodeImageView: ImageView

    private var amount: String = ""
    private var url: String = ""
    private var transactionReferenceID: String = ""
    private var token: String = ""
    private var name: String = ""
    private var star: String = ""
    private var phno: String = ""
    private var devatha: String = ""

    private var pollingTimer: CountDownTimer? = null
    private var paymentStatusJob: Job? = null
    private val paymentRepository: PaymentRepository by inject()
    private val sessionManager: SessionManager by inject()
    private var isCheckingPaymentStatus = false


    fun setData(
        amount: String,
        url: String,
        transactionReferenceID: String,
        token: String,
        name: String,
        phno: String,
        star: String,
        devatha: String
    ) {
        this.amount = amount
        this.url = url
        this.transactionReferenceID = transactionReferenceID
        this.token = token
        this.name = name
        this.phno = phno
        this.star = star
        this.devatha = devatha
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
            }
        }.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.custome_qr_dialogue, container, false)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        amountTextView = view.findViewById(R.id.txt_amount)
        qrCodeImageView = view.findViewById(R.id.qrCodeImageView)
        timerTextView = view.findViewById(R.id.txt_timer)

        val amountValue: Float = amount.toFloat()
        val formattedAmount = String.format("%.2f", amountValue)
        amountTextView.text = getString(R.string.amount) + " ₹ $formattedAmount /-"
        val qrCodeBitmap = generateUPIQRCode(url)
        qrCodeImageView.setImageBitmap(qrCodeBitmap)


        postPaymentHistory("S")
       // startTimer()

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dismiss()
        }

        view.findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(requireContext(), LanguageActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            dismiss()
        }
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
                timerTextView.text = getString(R.string.qr_expire) + " " + timeFormatted
                if (elapsedTime % pollInterval == 0L) {
                        checkPaymentStatus()


                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                stopCheckingPaymentStatus()
                val intent = Intent(requireContext(), LanguageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dismiss()
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
                        paymentRepository.paymentStatus(sessionManager.getUserId(), sessionManager.getCompanyId(), paymentRequest)
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
            devatha = devatha,
            nakshatra = star,
            name = name,
            phoneNumber =phno,
            orderAmount = amount.toDoubleOrNull()!!,
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
                    handleTransactionStatus(status,response.data.orderId)
                } else {
                    postPaymentHistory(status)
                }
            } catch (e: Exception) {
                postPaymentHistory(status)
            }
        }

    }

    private fun handleTransactionStatus(status: String, orderId: Int) {
        val intent = Intent(requireContext(), PaymentActivity::class.java).apply {
            putExtra("status", status)
            putExtra("amount", amount)
            putExtra("transID", transactionReferenceID)
            putExtra("name", name)
            putExtra("star", star)
            putExtra("devatha", devatha)
            putExtra("orderID", orderId.toString())
            putExtra("phno",phno)
        }
        startActivity(intent)
        dismiss()
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.75).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        pollingTimer?.cancel()
        stopCheckingPaymentStatus()
    }

    private fun stopCheckingPaymentStatus() {
        isCheckingPaymentStatus = false
        paymentStatusJob?.cancel()
    }
    fun isDialogShowing(): Boolean {
        return dialog?.isShowing == true
    }
}
