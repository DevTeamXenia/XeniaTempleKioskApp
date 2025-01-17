package com.xenia.churchkiosk.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.xenia.churchkiosk.R
import com.xenia.churchkiosk.databinding.ActivityPaymentBinding
import com.xenia.churchkiosk.utils.common.CommonMethod.setLocale
import kotlinx.coroutines.Dispatchers
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


class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val sharedPreferences: SharedPreferences by inject()
    private var curConnect: IDeviceConnection? = null

    private var status: String? = null
    private var amount: String? = null
    private var transID: String? = null
    private var name: String? = null
    private var orderID: String? = null
    private var phoneNo: String? = null
    private var selectedLanguage: String? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
        setLocale(this@PaymentActivity, selectedLanguage)
        status = intent.getStringExtra("status")
        amount = intent.getStringExtra("amount")
        transID = intent.getStringExtra("transID")
        name = intent.getStringExtra("name")

        orderID = intent.getStringExtra("orderID")
        phoneNo = intent.getStringExtra("phno")


        if (status.equals("S")) {
            binding.linSuccess.visibility = View.VISIBLE
            binding.linFailed.visibility = View.GONE
            binding.txtAmount.text = getString(R.string.amount) + " " + amount
            binding.txtTransId.text = getString(R.string.transcation_id) + " " + transID
            if (name!!.isNotEmpty()) {
                binding.txtName.visibility = View.VISIBLE
                binding.txtName.text = getString(R.string.pay_name) + " " + name
            }

            configPrinter()

        } else {
            binding.linSuccess.visibility = View.GONE
            binding.linFailed.visibility = View.VISIBLE
            redirect()
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
                redirect()
                Toast.makeText(this, "No USB printer devices found", Toast.LENGTH_LONG).show()
            }
        } catch (_: Exception) {
            redirect()

        }
    }

    private val connectListener = IPOSListener { code, _ ->
        when (code) {
            POSConnect.CONNECT_SUCCESS -> {
                initReceiptPrint()
            }

            POSConnect.CONNECT_FAIL -> {
                redirect()
            }

            POSConnect.CONNECT_INTERRUPT -> {
                redirect()

            }

            POSConnect.SEND_FAIL -> {
                redirect()

            }

            POSConnect.USB_DETACHED -> {
                redirect()

            }

            POSConnect.USB_ATTACHED -> {
                redirect()

            }
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

    @SuppressLint("DefaultLocale")
    private fun initReceiptPrint() {
        lifecycleScope.launch {
            val currentDate =
                SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date())


            val drawableDevasam = ContextCompat.getDrawable(
                this@PaymentActivity,
                R.drawable.print_header_logo
            ) as BitmapDrawable
            val drawableDevi = ContextCompat.getDrawable(
                this@PaymentActivity,
                R.drawable.print_bottom_logo
            ) as BitmapDrawable

            withContext(Dispatchers.IO) {
                val printer = POSPrinter(curConnect)
                val bitmapDevasam = drawableDevasam.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val compressedBitmapDevasam =
                    Bitmap.createScaledBitmap(bitmapDevasam, 550, 200, true)

                val bitmapDevi = drawableDevi.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val compressedBitmapDevi =
                    Bitmap.createScaledBitmap(bitmapDevi, 500, 100, true)

                val receiptBitmap: Bitmap = when (selectedLanguage) {
                    "en" -> generateReceiptBitmap(currentDate)
                    "ml" -> generateReceiptBitmapMl(currentDate)
                    "ta" -> generateReceiptBitmapTa(currentDate)
                    "kn" -> generateReceiptBitmapKa(currentDate)
                    "te" -> generateReceiptBitmapTe(currentDate)
                    "hi" -> generateReceiptBitmapHi(currentDate)
                    else -> throw IllegalArgumentException("Unsupported language: $selectedLanguage")
                }

                printer.printBitmap(compressedBitmapDevasam, POSConst.ALIGNMENT_CENTER, 500)
                    .feedLine(1)
                printer.printBitmap(receiptBitmap, POSConst.ALIGNMENT_CENTER, 600)
                    .feedLine(1)
                printer.printBitmap(compressedBitmapDevi, POSConst.ALIGNMENT_CENTER, 500)
                    .cutHalfAndFeed(1)

                compressedBitmapDevasam.recycle()
                compressedBitmapDevi.recycle()
                receiptBitmap.recycle()

                delay(2000)

                redirect()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmap(currentDate: String): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 1000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("ഇ-കാണിക്ക രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("E-Kannikka Receipt", width / 2f, 80f, paint)

        var yOffset = 150f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("രസീത് നം (Receipt No) : $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("തീയതി (Date) : $currentDate", 20f, yOffset, paint)
        yOffset += 65f
        tempCanvas.drawText("Name (പേര്)  : $name", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("Phone No (ഫോൺ നമ്പർ) : $phoneNo", 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 35f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 40f
        val amountDouble = amount?.toDoubleOrNull() ?: 0.0
        val formattedAmount = String.format("%.2f", amountDouble)
        tempCanvas.drawText("Amount Paid : $formattedAmount", width - 20f, yOffset, paint)

        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapMl(currentDate: String): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 1000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("ഇ-കാണിക്ക രസീത്", width / 2f, 40f, paint)

        var yOffset = 100f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("രസീത് നം : $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("തീയതി : $currentDate", 20f, yOffset, paint)
        yOffset += 65f
        tempCanvas.drawText("പേര് : $name", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("ഫോൺ നമ്പർ : $phoneNo", 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 35f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 40f
        val amountDouble = amount?.toDoubleOrNull() ?: 0.0
        val formattedAmount = String.format("%.2f", amountDouble)
        tempCanvas.drawText("ആകെ തുക : $formattedAmount", width - 20f, yOffset, paint)

        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapTa(currentDate: String): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 1000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("ഇ-കാണിക്ക രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("தானம் ரசீது", width / 2f, 80f, paint)

        var yOffset = 150f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 20f
        tempCanvas.drawText("ரசீது எண் (രസീത് നം): $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("தேதி (തീയതി): $currentDate", 20f, yOffset, paint)
        yOffset += 65f
        tempCanvas.drawText("பெயர் (പേര്) : $name", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("ഫதொலைபேசி எண் (ോൺ നമ്പർ) : $phoneNo", 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 40f
        val amountDouble = amount?.toDoubleOrNull() ?: 0.0
        val formattedAmount = String.format("%.2f", amountDouble)
        tempCanvas.drawText("மொத்த தொகை (ആകെ തുക) : $formattedAmount", width - 20f, yOffset, paint)

        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapKa(currentDate: String): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 1000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("ഇ-കാണിക്ക രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("ಎ ಕನಿಕಾ ರಸೀದಿ", width / 2f, 80f, paint)

        var yOffset = 150f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("ರಸೀದಿ ಸಂಖ್ಯೆ (രസീത് നം): $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("ದಿನಾಂಕ (തീയതി): $currentDate", 20f, yOffset, paint)
        yOffset += 65f
        tempCanvas.drawText("ಹೆಸರು (പേര്) : $name", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("ಫೋನ್ ಸಂಖ್ಯೆ (ഫോൺ നമ്പർ) : $phoneNo", 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 35f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 40f
        val amountDouble = amount?.toDoubleOrNull() ?: 0.0
        val formattedAmount = String.format("%.2f", amountDouble)
        tempCanvas.drawText("ಒಟ್ಟು ಮೊತ್ತ (ആകെ തുക): $formattedAmount", width - 20f, yOffset, paint)

        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapTe(currentDate: String): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 1000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("ഇ-കാണിക്ക രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("ఈ కనికా ರಸೀದಿ", width / 2f, 80f, paint)

        var yOffset = 150f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("రసీదు సంఖ్య (രസീത് നം): $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("తేదీ (തീയതി): $currentDate", 20f, yOffset, paint)
        yOffset += 65f
        tempCanvas.drawText("పేరు (പേര്) : $name", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("ఫోన్ సంఖ్య (ഫോൺ നമ്പർ) : $phoneNo", 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 35f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 40f
        val amountDouble = amount?.toDoubleOrNull() ?: 0.0
        val formattedAmount = String.format("%.2f", amountDouble)
        tempCanvas.drawText(
            "మొత్తం మొత్తం (ആകെ തുക): $formattedAmount",
            width - 20f,
            yOffset,
            paint
        )

        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapHi(currentDate: String): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 1000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("ഇ-കാണിക്ക രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("ए कनिका रसीद", width / 2f, 80f, paint)

        var yOffset = 150f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("रसीद संख्या (രസീത് നം): $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("तारीख (തീയതി): $currentDate", 20f, yOffset, paint)
        yOffset += 65f
        tempCanvas.drawText("नाम (പേര്) : $name", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("फोन नंबर (ഫോൺ നമ്പർ) : $phoneNo", 20f, yOffset, paint)
        yOffset += 35f
        val amountDouble = amount?.toDoubleOrNull() ?: 0.0
        val formattedAmount = String.format("%.2f", amountDouble)
        tempCanvas.drawText("कुल राशि (ആകെ തുക) $formattedAmount", width - 20f, yOffset, paint)

        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    private fun redirect() {
        Handler(mainLooper).postDelayed({
            val intent = Intent(applicationContext, LanguageActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }, 1000)
    }


}
