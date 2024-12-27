package com.xeniatechnologies.app.nimahal.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.xeniatechnologies.app.nimahal.R
import com.xeniatechnologies.app.nimahal.databinding.ActivityPaymentBinding
import kotlinx.coroutines.Dispatchers
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
    private var curConnect: IDeviceConnection? = null
    private val sharedPreferences: SharedPreferences by inject()
    private var amount: String = ""
    private var status: String = ""
    private var transactionId: String = ""
    private var name: String = ""
    private var phoneNumber: String = ""
    private var formattedAmount: String = ""

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        status = intent.getStringExtra("STATUS") ?: "Unknown"
        amount = intent.getStringExtra("AMOUNT") ?: "0.0"
        val amountDouble = amount.toDoubleOrNull() ?: 0.0
        formattedAmount = String.format("%.2f", amountDouble)
        transactionId = intent.getStringExtra("TRANS_ID") ?: "N/A"
        name = intent.getStringExtra("NAME") ?: "No Name"
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: "N/A"

        if (status == "S") {
            binding.linSuccess.visibility = View.VISIBLE
            binding.linFailed.visibility = View.GONE
            binding.txtAmount.text = getString(R.string.amount) + " " + formattedAmount
            binding.txtTransId.text = getString(R.string.transcation_id) + " " + transactionId

            if (name.isNotEmpty()) {
                binding.txtName.visibility = View.VISIBLE
                binding.txtName.text = getString(R.string.name) + " " + name
            }

            binding.txtStar.text = getString(R.string.phone_number) + " " + phoneNumber

            val mediaPlayer = MediaPlayer.create(this, R.raw.success)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
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
            val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"

            val headerLogo = ContextCompat.getDrawable(
                this@PaymentActivity,
                R.drawable.print_logo
            ) as BitmapDrawable

            val bitmapHeaderLogo = headerLogo.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val compressedBitmapHeaderLogo =
                Bitmap.createScaledBitmap(bitmapHeaderLogo, 500, 150, true)

            withContext(Dispatchers.IO) {
                val printer = POSPrinter(curConnect)

                val receiptBitmap: Bitmap = when (selectedLanguage) {
                    "en" -> generateReceiptBitmap(currentDate)
                    "ml" -> generateReceiptBitmapMa(currentDate)
                    else -> throw IllegalArgumentException("Unsupported language: $selectedLanguage")
                }
                printer.printBitmap(compressedBitmapHeaderLogo, POSConst.ALIGNMENT_CENTER, 500)
                    .feedLine(1)
                printer.printBitmap(receiptBitmap, POSConst.ALIGNMENT_CENTER, 600)
                    .cutHalfAndFeed(1)

                compressedBitmapHeaderLogo.recycle()
                receiptBitmap.recycle()
            }

            redirect()
        }
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmap(
        currentDate: String,
    ): Bitmap {
        val width = 550
        val paint = Paint().apply { isAntiAlias = true }

        val tempBitmap = Bitmap.createBitmap(width, 5000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        var yOffset = 30f

        paint.textSize = 22f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("Samastha Centre, Kozhikode, Kerala 673006", width / 2f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText(
            "Phone: +91 999XXXX369, website: www.aimahal.com",
            width / 2f,
            yOffset,
            paint
        )

        yOffset += 60f
        paint.textSize = 40f
        tempCanvas.drawText("Receipt", width / 2f, yOffset, paint)

        yOffset += 60f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 25f
        tempCanvas.drawText("Receipt No: $transactionId", 10f, yOffset, paint)
        yOffset += 30f
        tempCanvas.drawText("Date: $currentDate", 10f, yOffset, paint)

        yOffset += 60f
        tempCanvas.drawText("Name: $name", 10f, yOffset, paint)
        paint.textAlign = Paint.Align.RIGHT
        tempCanvas.drawText("Phone: $phoneNumber", width - 10f, yOffset, paint)

        yOffset += 60f
        paint.textAlign = Paint.Align.LEFT
        tempCanvas.drawText("Subscription", 10f, yOffset, paint)
        paint.textAlign = Paint.Align.RIGHT
        tempCanvas.drawText(formattedAmount, width - 10f, yOffset, paint)

        yOffset += 60f
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("Amount Paid : $formattedAmount", width / 2f, yOffset, paint)
        yOffset += 30f
        paint.textSize = 18f
        tempCanvas.drawText("UPI Reference No: $transactionId", width / 2f, yOffset, paint)

        yOffset += 60f
        paint.textSize = 25f
        tempCanvas.drawText("Thank You!", width / 2f, yOffset, paint)
        yOffset += 25f
        paint.textSize = 18f
        tempCanvas.drawText("Powered by Xenia Technologies", width / 2f, yOffset, paint)
        yOffset += 40f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapMa(
        currentDate: String,
    ): Bitmap {
        val width = 550
        val paint = Paint().apply { isAntiAlias = true }

        val tempBitmap = Bitmap.createBitmap(width, 5000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        var yOffset = 30f

        paint.textSize = 22f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("Samastha Centre, Kozhikode, Kerala 673006", width / 2f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText(
            "Phone: +91 999XXXX369, website: www.aimahal.com",
            width / 2f,
            yOffset,
            paint
        )

        yOffset += 60f
        paint.textSize = 40f
        tempCanvas.drawText("രസീത്", width / 2f, yOffset, paint)

        yOffset += 60f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 25f
        tempCanvas.drawText("രസീത് നം: $transactionId", 10f, yOffset, paint)
        yOffset += 30f
        tempCanvas.drawText("തീയതി : $currentDate", 10f, yOffset, paint)

        yOffset += 60f
        tempCanvas.drawText("പേര് : $name", 10f, yOffset, paint)
        paint.textAlign = Paint.Align.RIGHT
        tempCanvas.drawText("ഫോൺ നമ്പർ: $phoneNumber", width - 10f, yOffset, paint)

        yOffset += 60f
        paint.textAlign = Paint.Align.LEFT
        tempCanvas.drawText("സബ്സ്ക്രിപ്ഷൻ", 10f, yOffset, paint)
        paint.textAlign = Paint.Align.RIGHT
        tempCanvas.drawText(formattedAmount, width - 10f, yOffset, paint)

        yOffset += 60f
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("ആകെ തുക : $formattedAmount", width / 2f, yOffset, paint)
        yOffset += 30f
        paint.textSize = 18f
        tempCanvas.drawText("UPI Reference No: $transactionId", width / 2f, yOffset, paint)

        yOffset += 60f
        paint.textSize = 25f
        tempCanvas.drawText("നന്ദി!", width / 2f, yOffset, paint)
        yOffset += 25f
        paint.textSize = 18f
        tempCanvas.drawText("Powered by Xenia Technologies", width / 2f, yOffset, paint)
        yOffset += 40f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }


    private fun redirect() {
        startActivity(Intent(applicationContext, LanguageActivity::class.java))
        finish()
    }
}