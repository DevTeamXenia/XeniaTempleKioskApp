package com.xenia.churchkiosk.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
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
import com.xenia.churchkiosk.data.repository.VazhipaduRepository
import com.xenia.churchkiosk.data.room.entity.Vazhipadu
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


@Suppress("DEPRECATION")
class PaymentVazhipaduActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val sharedPreferences: SharedPreferences by inject()
    private val vazhipaduRepository: VazhipaduRepository by inject()
    private var curConnect: IDeviceConnection? = null

    private var status: String? = null
    private var amount: String? = null
    private var transID: String? = null
    private var orderID: String? = null

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        status = intent.getStringExtra("status")
        amount = intent.getStringExtra("amount")
        transID = intent.getStringExtra("transID")
        orderID = intent.getStringExtra("orderID")

        if (status.equals("S")) {
            binding.linSuccess.visibility = View.VISIBLE
            binding.linFailed.visibility = View.GONE
            val amountDouble = amount?.toDoubleOrNull() ?: 0.0
            val formattedAmount = String.format("%.2f", amountDouble)
            binding.txtAmount.text = getString(R.string.amount) + " " + formattedAmount
            binding.txtTransId.text = getString(R.string.transcation_id) + " " + transID

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
            val allVazhipaduItems = vazhipaduRepository.getAllVazhipaduItems()
            val currentDate = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date())
            val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"

            setLocale(this@PaymentVazhipaduActivity, selectedLanguage)

            val drawableDevasam = ContextCompat.getDrawable(
                this@PaymentVazhipaduActivity,
                R.drawable.print_header_logo
            ) as BitmapDrawable
            val drawableDevi = ContextCompat.getDrawable(
                this@PaymentVazhipaduActivity,
                R.drawable.print_bottom_logo
            ) as BitmapDrawable

            withContext(Dispatchers.IO) {
                val printer = POSPrinter(curConnect)

                val bitmapDevasam = drawableDevasam.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val compressedBitmapDevasam = Bitmap.createScaledBitmap(bitmapDevasam, 550, 200, true)

                val bitmapDevi = drawableDevi.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val compressedBitmapDevi = Bitmap.createScaledBitmap(bitmapDevi, 500, 100, true)


                val receiptBitmap: Bitmap = when (selectedLanguage) {
                    "en" -> generateReceiptBitmap(transID!!,allVazhipaduItems, currentDate)
                    "ml" -> generateReceiptBitmapMl(transID!!,allVazhipaduItems, currentDate)
                    "ta" -> generateReceiptBitmapTa(transID!!,allVazhipaduItems, currentDate)
                    "kn" -> generateReceiptBitmapKa(transID!!,allVazhipaduItems, currentDate)
                    "te" -> generateReceiptBitmapTe(transID!!, allVazhipaduItems, currentDate)
                    "hi" -> generateReceiptBitmapHi(transID!!, allVazhipaduItems, currentDate)
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


                redirect()
            }
        }
    }



    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmap(
        transID: String,
        offerings: List<Vazhipadu>,
        currentDate: String
    ): Bitmap {
        val width = 576
        val paint = Paint().apply { isAntiAlias = true }

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("Vazhipadu Receipt", width / 2f, 80f, paint)

        var yOffset = 130f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("രസീത് നം (Receipt No): $transID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("തീയതി (Date): $currentDate", 20f, yOffset, paint)
        yOffset += 35f

        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 28f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        var totalAmount = 0.0

        val groupedOfferings = offerings.groupBy { it.vaName }

        groupedOfferings.forEach { (groupName, offeringsInGroup) ->
            yOffset += 20f
            paint.textSize = 28f
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(groupName, 20f, yOffset, paint)

            yOffset += 50f

            offeringsInGroup.forEach { offering ->
                paint.textAlign = Paint.Align.LEFT
                tempCanvas.drawText(offering.vaOfferingsNameMa, 40f, yOffset, paint)
                yOffset += 35f
                tempCanvas.drawText(offering.vaOfferingsName, 40f, yOffset, paint)
                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)
                yOffset += 50f
                totalAmount += offering.vaOfferingsAmount
                paint.textAlign = Paint.Align.LEFT
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }

        yOffset += 20f
        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("ആകെ തുക (Total Amount): $totalAmountStr", width - 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 35f

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapMl(
        transID: String,
        offerings: List<Vazhipadu>,
        currentDate: String
    ): Bitmap {
        val width = 576
        val paint = Paint().apply { isAntiAlias = true }

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)

        var yOffset = 130f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("രസീത് നം : $transID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("തീയതി : $currentDate", 20f, yOffset, paint)
        yOffset += 35f

        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 28f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        var totalAmount = 0.0

        val groupedOfferings = offerings.groupBy { it.vaName }

        groupedOfferings.forEach { (groupName, offeringsInGroup) ->
            yOffset += 20f
            paint.textSize = 28f
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(groupName, 20f, yOffset, paint)
            yOffset += 50f

            offeringsInGroup.forEach { offering ->
                paint.textAlign = Paint.Align.LEFT
                tempCanvas.drawText(offering.vaOfferingsNameMa, 40f, yOffset, paint)
                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)
                yOffset += 50f
                totalAmount += offering.vaOfferingsAmount
                paint.textAlign = Paint.Align.LEFT
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }

        yOffset += 20f
        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("ആകെ തുക : $totalAmountStr", width - 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 35f

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapTa(
        transID: String,
        offerings: List<Vazhipadu>,
        currentDate: String
    ): Bitmap {
        val width = 576
        val paint = Paint().apply { isAntiAlias = true }

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("வழிபாடு ரசீது", width / 2f, 80f, paint)

        var yOffset = 130f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("രസീത് നം (ரசீது எண்): $transID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("തീയതി (தேதி): $currentDate", 20f, yOffset, paint)
        yOffset += 35f


        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 28f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        var totalAmount = 0.0

        val groupedOfferings = offerings.groupBy { it.vaName }

        groupedOfferings.forEach { (groupName, offeringsInGroup) ->
            yOffset += 20f
            paint.textSize = 28f
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(groupName, 20f, yOffset, paint)

            yOffset += 50f

            offeringsInGroup.forEach { offering ->
                paint.textAlign = Paint.Align.LEFT
                tempCanvas.drawText(offering.vaOfferingsNameMa, 40f, yOffset, paint)
                yOffset += 35f
                tempCanvas.drawText(offering.vaCategoryNameTa, 40f, yOffset, paint)
                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)
                yOffset += 50f
                totalAmount += offering.vaOfferingsAmount
                paint.textAlign = Paint.Align.LEFT
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }

        yOffset += 20f
        paint.textSize = 28f
        paint.textAlign = Paint.Align.RIGHT
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("மொத்த தொகை(ആകെ തുക) : $totalAmountStr", width - 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 35f

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapKa(
        transID: String,
        offerings: List<Vazhipadu>,
        currentDate: String
    ): Bitmap {
        val width = 576
        val paint = Paint().apply { isAntiAlias = true }

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("ವಜಿಪಾಡು ರಸೀದಿ", width / 2f, 80f, paint)

        var yOffset = 130f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("ರಸೀದಿ ಸಂಖ್ಯೆ (രസീത് നം): $transID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("ದಿನಾಂಕ (തീയതി): $currentDate", 20f, yOffset, paint)
        yOffset += 35f

        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 28f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        var totalAmount = 0.0

        val groupedOfferings = offerings.groupBy { it.vaName }

        groupedOfferings.forEach { (groupName, offeringsInGroup) ->
            yOffset += 20f
            paint.textSize = 28f
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(groupName, 20f, yOffset, paint)
            yOffset += 50f

            offeringsInGroup.forEach { offering ->
                paint.textAlign = Paint.Align.LEFT
                tempCanvas.drawText(offering.vaOfferingsNameMa, 40f, yOffset, paint)
                yOffset += 35f
                tempCanvas.drawText(offering.vaCategoryNameKa, 40f, yOffset, paint)
                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)
                yOffset += 50f
                totalAmount += offering.vaOfferingsAmount
                paint.textAlign = Paint.Align.LEFT
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }

        yOffset += 20f
        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("ಒಟ್ಟು ಮೊತ್ತ (ആകെ തുക) : $totalAmountStr", width - 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 35f

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapTe(
        transID: String,
        offerings: List<Vazhipadu>,
        currentDate: String
    ): Bitmap {
        val width = 576
        val paint = Paint().apply { isAntiAlias = true }

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("వాజిపాడు రసీదు", width / 2f, 80f, paint)

        var yOffset = 130f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("రసీదు సంఖ్య (രസീത് നം): $transID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("తేదీ (തീയതി): $currentDate", 20f, yOffset, paint)
        yOffset += 35f

        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 28f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        var totalAmount = 0.0

        val groupedOfferings = offerings.groupBy { it.vaName }

        groupedOfferings.forEach { (groupName, offeringsInGroup) ->
            yOffset += 20f
            paint.textSize = 28f
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(groupName, 20f, yOffset, paint)
            yOffset += 50f

            offeringsInGroup.forEach { offering ->
                paint.textAlign = Paint.Align.LEFT
                tempCanvas.drawText(offering.vaOfferingsNameMa, 40f, yOffset, paint)
                yOffset += 35f
                tempCanvas.drawText(offering.vaCategoryNameTe, 40f, yOffset, paint)
                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)
                yOffset += 50f
                totalAmount += offering.vaOfferingsAmount
                paint.textAlign = Paint.Align.LEFT
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }

        yOffset += 20f
        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("మొత్తం మొత్తం (ആകെ തുക): $totalAmountStr", width - 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 35f

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapHi(
        transID: String,
        offerings: List<Vazhipadu>,
        currentDate: String
    ): Bitmap {
        val width = 576
        val paint = Paint().apply { isAntiAlias = true }

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("वज़ीपाडु रसीद", width / 2f, 80f, paint)

        var yOffset = 130f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("रसीद संख्या (രസീത് നം): $transID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("तारीख (തീയതി): $currentDate", 20f, yOffset, paint)
        yOffset += 35f

        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 28f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        var totalAmount = 0.0

        val groupedOfferings = offerings.groupBy { it.vaName }

        groupedOfferings.forEach { (groupName, offeringsInGroup) ->
            yOffset += 20f
            paint.textSize = 28f
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(groupName, 20f, yOffset, paint)
            yOffset += 50f
            offeringsInGroup.forEach { offering ->
                paint.textAlign = Paint.Align.LEFT
                tempCanvas.drawText(offering.vaOfferingsNameMa, 40f, yOffset, paint)
                yOffset += 35f
                tempCanvas.drawText(offering.vaCategoryNameTe, 40f, yOffset, paint)
                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)
                yOffset += 50f
                totalAmount += offering.vaOfferingsAmount
                paint.textAlign = Paint.Align.LEFT
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }

        yOffset += 20f
        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("మొత్తం మొత్తం (ആകെ തുക): $totalAmountStr", width - 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 35f

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

