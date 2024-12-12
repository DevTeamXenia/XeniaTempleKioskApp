package com.xenia.templekiosk.ui.screens

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
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.PersonWithItems
import com.xenia.templekiosk.data.repository.VazhipaduRepository
import com.xenia.templekiosk.databinding.ActivityPaymentBinding
import com.xenia.templekiosk.utils.SessionManager
import com.xenia.templekiosk.utils.common.CommonMethod.setLocale
import kotlinx.coroutines.launch
import net.posprinter.IDeviceConnection
import net.posprinter.IPOSListener
import net.posprinter.POSConnect
import net.posprinter.POSConst
import net.posprinter.POSPrinter
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PaymentVazhipaduActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val sharedPreferences: SharedPreferences by inject()
    private val vazhipaduRepository: VazhipaduRepository by inject()
    private var curConnect: IDeviceConnection? = null
    private lateinit var selectedLanguage :String

    private var status: String? = null
    private var amount: String? = null
    private var transID: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

         status = intent.getStringExtra("status")
         amount = intent.getStringExtra("amount")
         transID = intent.getStringExtra("transID")

        if(status.equals("S")){
            binding.linSuccess.visibility = View.VISIBLE
            binding.linFailed.visibility = View.GONE
            binding.txtAmount.text = getString(R.string.amount)+" "+ amount
            binding.txtTransId.text = getString(R.string.transcation_id) +" "+ transID

            configPrinter()

        }else{
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
            val distinctPersons = vazhipaduRepository.getDistinctPersonsWithOfferings()
            val currentDate = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date())

            val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"
            setLocale(this@PaymentVazhipaduActivity, selectedLanguage)
            val receiptBitmap: Bitmap

            if (selectedLanguage == "en") {
                receiptBitmap = generateReceiptBitmap(transID!!, distinctPersons, currentDate)
            } else {
                receiptBitmap = generateReceiptBitmapMl(transID!!, distinctPersons, currentDate)
            }


            val drawableDevasam = ContextCompat.getDrawable(this@PaymentVazhipaduActivity, R.drawable.print_header_logo)
            val bitmapDevasam = (drawableDevasam as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val drawableDevi = ContextCompat.getDrawable(this@PaymentVazhipaduActivity, R.drawable.print_bottom_logo)
            val bitmapDevi = (drawableDevi as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)


            POSPrinter(curConnect).printBitmap(bitmapDevasam, POSConst.ALIGNMENT_CENTER, 590)
                .feedLine(1)
            POSPrinter(curConnect).printBitmap(receiptBitmap, POSConst.ALIGNMENT_CENTER, 800)
                .feedLine(1)
            POSPrinter(curConnect).printBitmap(bitmapDevi, POSConst.ALIGNMENT_CENTER, 500)
                .cutHalfAndFeed(1)
        }

        redirect()
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmap(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String
    ): Bitmap {
        val width = 576
        val baseHeight = 200f
        val itemLineHeight = 40f
        val interItemSpacing = 30f

        val totalItemLines = allCartItems.sumBy { personWithItems ->
            personWithItems.items.size + 3
        }

        val height = (baseHeight + (totalItemLines * itemLineHeight) + (allCartItems.size * interItemSpacing) + 180f).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true

        paint.textSize = 40f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("വഴിപാട് രസീത്", width / 2f, 50f, paint)
        canvas.drawText("Vazhipadu Receipt", width / 2f, 100f, paint)

        var yOffset = 150f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 24f
        canvas.drawText("രസീത് നം : $transID", 20f, yOffset, paint)
        yOffset += 30f
        canvas.drawText("Receipt No", 20f, yOffset, paint)
        yOffset += 30f
        canvas.drawText("തീയതി : $currentDate", 20f, yOffset, paint)
        yOffset += 30f
        canvas.drawText("Date", 20f, yOffset, paint)

        yOffset += 60f

        val nakshatras = resources.getStringArray(R.array.nakshatras)

        var totalAmount = 0.0

        allCartItems.forEach { personWithItems ->
            paint.textSize = 30f
            paint.color = Color.BLACK

            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(personWithItems.personName, 20f, yOffset, paint)

            paint.textAlign = Paint.Align.RIGHT

            selectedLanguage = "ml"

            val nakshatrasArrayRes = when (selectedLanguage) {
                "hi" -> R.array.nakshatras // Hindi
                "ta" -> R.array.nakshatras // Tamil
                "te" -> R.array.nakshatras // Telugu
                "ml" -> R.array.nakshatras // Malayalam
                "kn" -> R.array.nakshatras // Kannada
                else -> R.array.nakshatras // Default (English or fallback)
            }

            val nakshatras = resources.getStringArray(nakshatrasArrayRes)

            val starIndex = personWithItems.personStar.toIntOrNull() ?: 0
            val starText = if (starIndex in nakshatras.indices) nakshatras[starIndex] else "Unknown"

            canvas.drawText(starText, width - 20f, yOffset, paint)
            yOffset += 30f
            selectedLanguage = "en"

            val nakshatrasArrayRes1 = when (selectedLanguage) {
                "hi" -> R.array.nakshatras // Hindi
                "ta" -> R.array.nakshatras // Tamil
                "te" -> R.array.nakshatras // Telugu
                "ml" -> R.array.nakshatras // Malayalam
                "kn" -> R.array.nakshatras // Kannada
                else -> R.array.nakshatras // Default (English or fallback)
            }

            val nakshatras1 = resources.getStringArray(nakshatrasArrayRes1)

            val starIndex1 = personWithItems.personStar.toIntOrNull() ?: 0
            val starText1 = if (starIndex1 in nakshatras1.indices) nakshatras[starIndex] else "Unknown"

            canvas.drawText(starText1, width - 20f, yOffset, paint)


            paint.color = Color.BLACK
            paint.strokeWidth = 1f
            canvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)

            yOffset += 50f


            personWithItems.items.forEach { item ->
                val offeringName = item.vaOfferingsNameMa
                val amountStr = String.format("%.2f", item.vaOfferingsAmount)

                paint.color = Color.BLACK
                paint.textSize = 22f
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(offeringName, 20f, yOffset, paint)
                yOffset += 30f
                canvas.drawText(item.vaOfferingsName, 20f, yOffset, paint)

                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(amountStr, width - 20f, yOffset, paint)


                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(amountStr, width - 20f, yOffset, paint)

                totalAmount += item.vaOfferingsAmount
                yOffset += itemLineHeight
            }


            paint.color = Color.BLACK
            paint.strokeWidth = 1f
            canvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)


            yOffset += interItemSpacing
        }

        yOffset += 30f


        paint.color = Color.BLACK
        paint.textSize = 36f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("ആകെ തുക: ${String.format("%.2f", totalAmount)}", width - 20f, yOffset, paint)
        yOffset += 40f
        val moveLeft = 135f
        canvas.drawText("Amount Paid", width - 20f - moveLeft, yOffset, paint)

        paint.textSize = 24f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 30f
        canvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        return bitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapMl(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String
    ): Bitmap {
        val width = 576
        val baseHeight = 200f
        val itemLineHeight = 40f
        val interItemSpacing = 30f

        val totalItemLines = allCartItems.sumBy { personWithItems ->
            personWithItems.items.size + 3
        }

        val height = (baseHeight + (totalItemLines * itemLineHeight) + (allCartItems.size * interItemSpacing) + 100f).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true

        paint.textSize = 40f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("വഴിപാട് രസീത്", width / 2f, 50f, paint)

        var yOffset = 120f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 24f
        canvas.drawText("രസീത് നം : $transID", 20f, yOffset, paint)
        yOffset += 30f
        canvas.drawText("തീയതി : $currentDate", 20f, yOffset, paint)

        yOffset += 60f

        val nakshatras = resources.getStringArray(R.array.nakshatras)

        var totalAmount = 0.0

        allCartItems.forEach { personWithItems ->
            paint.textSize = 30f
            paint.color = Color.BLACK

            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(personWithItems.personName, 20f, yOffset, paint)

            paint.textAlign = Paint.Align.RIGHT

            val starIndex = personWithItems.personStar.toIntOrNull() ?: 0
            val starText = if (starIndex in nakshatras.indices) nakshatras[starIndex] else "Unknown"
            canvas.drawText(starText, width - 20f, yOffset, paint)

            yOffset += 40f
            paint.color = Color.BLACK
            paint.strokeWidth = 1f
            canvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)

            yOffset += 100f


            personWithItems.items.forEach { item ->
                val offeringName = item.vaOfferingsNameMa
                val amountStr = String.format("%.2f", item.vaOfferingsAmount)

                paint.color = Color.BLACK
                paint.textSize = 22f
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(offeringName, 20f, yOffset, paint)

                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(amountStr, width - 20f, yOffset, paint)

                totalAmount += item.vaOfferingsAmount
                yOffset += itemLineHeight
            }


            paint.color = Color.BLACK
            paint.strokeWidth = 1f
            canvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)


            yOffset += interItemSpacing
        }

        yOffset += 30f


        paint.color = Color.BLACK
        paint.textSize = 36f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("ആകെ തുക: ${String.format("%.2f", totalAmount)}", width - 20f, yOffset, paint)

        paint.textSize = 24f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 30f
        canvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        return bitmap
    }

    private fun redirect(){
        Handler(mainLooper).postDelayed({
            lifecycleScope.launch {
                startActivity(Intent(applicationContext, LanguageActivity::class.java))
                finish()
            }

        }, 1000)
    }

    fun getNakshatrasByLanguage(selectedLanguage: String): Array<String> {
        val nakshatrasArrayRes = when (selectedLanguage) {
            "hi" -> R.array.nakshatras // Hindi
            "ta" -> R.array.nakshatras // Tamil
            "te" -> R.array.nakshatras // Telugu
            "ml" -> R.array.nakshatras // Malayalam
            "kn" -> R.array.nakshatras // Kannada
            else -> R.array.nakshatras // Default (English or fallback)
        }
        return resources.getStringArray(nakshatrasArrayRes)
    }

}

