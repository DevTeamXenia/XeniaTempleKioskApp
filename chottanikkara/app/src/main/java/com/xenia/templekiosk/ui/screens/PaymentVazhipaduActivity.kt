package com.xenia.templekiosk.ui.screens

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
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.PersonWithItems
import com.xenia.templekiosk.data.repository.VazhipaduRepository
import com.xenia.templekiosk.databinding.ActivityPaymentBinding
import com.xenia.templekiosk.utils.common.CommonMethod.setLocale
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


@Suppress("DEPRECATION")
class PaymentVazhipaduActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val sharedPreferences: SharedPreferences by inject()
    private val vazhipaduRepository: VazhipaduRepository by inject()
    private var curConnect: IDeviceConnection? = null

    private var status: String? = null
    private var amount: String? = null
    private var transID: String? = null

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        status = intent.getStringExtra("status")
        amount = intent.getStringExtra("amount")
        transID = intent.getStringExtra("transID")

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
            val distinctPersons = vazhipaduRepository.getDistinctPersonsWithOfferings()
            val currentDate = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date())
            val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"

            setLocale(this@PaymentVazhipaduActivity, selectedLanguage)

            val receiptBitmap: Bitmap = when (selectedLanguage) {
                "en" -> generateReceiptBitmap(transID!!, distinctPersons, currentDate)
                "ml" -> generateReceiptBitmapMl(transID!!, distinctPersons, currentDate)
                "ta" -> generateReceiptBitmapTa(transID!!, distinctPersons, currentDate)
                "kn" -> generateReceiptBitmapKa(transID!!, distinctPersons, currentDate)
                "te" -> generateReceiptBitmapTe(transID!!, distinctPersons, currentDate)
                "hi" -> generateReceiptBitmapHi(transID!!, distinctPersons, currentDate)
                else -> throw IllegalArgumentException("Unsupported language: $selectedLanguage")
            }

            val drawableDevasam = ContextCompat.getDrawable(
                this@PaymentVazhipaduActivity,
                R.drawable.print_header_logo
            )
            val bitmapDevasam = (drawableDevasam as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val drawableDevi = ContextCompat.getDrawable(
                this@PaymentVazhipaduActivity,
                R.drawable.print_bottom_logo
            )
            val bitmapDevi = (drawableDevi as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val compressedBitmapDevasam = Bitmap.createScaledBitmap(bitmapDevasam, 500, 100, true)
            val compressedBitmapDevi = Bitmap.createScaledBitmap(bitmapDevi, 500, 100, true)

            withContext(Dispatchers.IO) {
                val printer = POSPrinter(curConnect)
                printer.printBitmap(compressedBitmapDevasam, POSConst.ALIGNMENT_CENTER, 500)
                    .feedLine(1)

                printer.printBitmap(receiptBitmap, POSConst.ALIGNMENT_CENTER, 600)
                    .feedLine(1)

                printer.printBitmap(compressedBitmapDevi, POSConst.ALIGNMENT_CENTER, 500)
                    .cutHalfAndFeed(1)
            }

            redirect()
        }
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmap(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String
    ): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("Vazhipadu Receipt", width / 2f, 80f, paint)


        var yOffset = 140f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("രസീത് നം (Receipt No) : $transID", 20f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText("തീയതി (Date) : $currentDate", 20f, yOffset, paint)
        yOffset += 25f
        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        var totalAmount = 0.0
        allCartItems.forEachIndexed { index, personWithItems ->
            if (index > 0) {
                yOffset += 40f
            }

            paint.textSize = 28f
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(personWithItems.personName, 20f, yOffset, paint)

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = personWithItems.personStar.trim()
            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            if (starIndex >= 0) {
                tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
                tempCanvas.drawText(nakshatrasEn[starIndex], width - 20f, yOffset + 25f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f

                val melkavuDevathaInMalayalam: String?
                val melkavuDevathaInEnglish: String?

                when (item.vaSubTempleName) {
                    R.string.melkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.melkavu_devi, "en")
                    }
                    R.string.keezhkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.keezhkavu_devi, "en")
                    }
                    R.string.shiva.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.shiva, "en")
                    }
                    else -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.ayyappa, "en")
                    }
                }

                tempCanvas.drawText(melkavuDevathaInMalayalam, 20f, yOffset, paint)
                yOffset += 25f

                tempCanvas.drawText(melkavuDevathaInEnglish, 20f, yOffset, paint)
                yOffset += 25f

                val nameLines = splitText(item.vaOfferingsName, width - 40, paint)
                nameLines.forEach { line ->
                    tempCanvas.drawText(line, 20f, yOffset, paint)
                    yOffset += 25f
                }

                tempCanvas.drawText(item.vaOfferingsNameMa, 20f, yOffset, paint)

                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", item.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)

                totalAmount += item.vaOfferingsAmount
                yOffset += 35f
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }



        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 20f
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("ആകെ തുക (Total Amount): $totalAmountStr", width - 20f, yOffset, paint)
        paint.textSize = 22f
        yOffset += 30f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 30f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapMl(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String
    ): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)

        var yOffset = 130f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("രസീത് നം : $transID", 20f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText("തീയതി : $currentDate", 20f, yOffset, paint)
        yOffset += 25f
        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        val nakshatras = resources.getStringArray(R.array.nakshatras)
        var totalAmount = 0.0

        allCartItems.forEach { personWithItems ->
            paint.textSize = 30f
            paint.color = Color.BLACK

            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(personWithItems.personName, 20f, yOffset, paint)

            paint.textAlign = Paint.Align.RIGHT

            val starIndex = personWithItems.personStar.toIntOrNull() ?: 0
            val starText = if (starIndex in nakshatras.indices) nakshatras[starIndex] else "Unknown"
            tempCanvas.drawText(starText, width - 20f, yOffset, paint)

            yOffset += 40f
            personWithItems.items.forEach { item ->
                val offeringName = item.vaOfferingsNameMa
                val amountStr = String.format("%.2f", item.vaOfferingsAmount)

                paint.color = Color.BLACK
                paint.textSize = 22f
                paint.textAlign = Paint.Align.LEFT
                tempCanvas.drawText(offeringName, 20f, yOffset, paint)

                paint.textAlign = Paint.Align.RIGHT
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)

                totalAmount += item.vaOfferingsAmount
                yOffset += 35f
            }

            paint.color = Color.BLACK
            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)

            yOffset += 20f
        }

        yOffset += 20f

        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        tempCanvas.drawText("ആകെ തുക: $totalAmount", width - 20f, yOffset, paint)

        yOffset += 35f

        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        yOffset += 40f

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        return finalBitmap
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapTa(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String
    ): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("வழிபாடு ரசீது", width / 2f, 80f, paint)


        var yOffset = 140f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("ரசீது எண் (Receipt No) : $transID", 20f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText("தேதி (Date) : $currentDate", 20f, yOffset, paint)
        yOffset += 25f
        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        var totalAmount = 0.0
        allCartItems.forEachIndexed { index, personWithItems ->
            if (index > 0) {
                yOffset += 40f
            }

            paint.textSize = 28f
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(personWithItems.personName, 20f, yOffset, paint)

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = personWithItems.personStar.trim()
            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            if (starIndex >= 0) {
                tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
                tempCanvas.drawText(nakshatrasEn[starIndex], width - 20f, yOffset + 25f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f

                val melkavuDevathaInMalayalam: String?
                val melkavuDevathaInEnglish: String?

                when (item.vaSubTempleName) {
                    R.string.melkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.melkavu_devi, "en")
                    }
                    R.string.keezhkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.keezhkavu_devi, "en")
                    }
                    R.string.shiva.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.shiva, "en")
                    }
                    else -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.ayyappa, "en")
                    }
                }

                tempCanvas.drawText(melkavuDevathaInMalayalam, 20f, yOffset, paint)
                yOffset += 25f

                tempCanvas.drawText(melkavuDevathaInEnglish, 20f, yOffset, paint)
                yOffset += 25f

                val nameLines = splitText(item.vaOfferingsName, width - 40, paint)
                nameLines.forEach { line ->
                    tempCanvas.drawText(line, 20f, yOffset, paint)
                    yOffset += 25f
                }

                tempCanvas.drawText(item.vaOfferingsNameMa, 20f, yOffset, paint)

                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", item.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)

                totalAmount += item.vaOfferingsAmount
                yOffset += 35f
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }



        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 20f
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("மொத்த தொகை (Total Amount): $totalAmountStr", width - 20f, yOffset, paint)
        paint.textSize = 22f
        yOffset += 30f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 30f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        return finalBitmap
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapKa(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String
    ): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("ವಜಿಪಾಡು ರಸೀದಿ", width / 2f, 80f, paint)


        var yOffset = 140f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("ರಸೀದಿ ಸಂಖ್ಯೆ (Receipt No) : $transID", 20f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText("ದಿನಾಂಕ (Date) : $currentDate", 20f, yOffset, paint)
        yOffset += 25f
        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        var totalAmount = 0.0
        allCartItems.forEachIndexed { index, personWithItems ->
            if (index > 0) {
                yOffset += 40f
            }

            paint.textSize = 28f
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(personWithItems.personName, 20f, yOffset, paint)

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = personWithItems.personStar.trim()
            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            if (starIndex >= 0) {
                tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
                tempCanvas.drawText(nakshatrasEn[starIndex], width - 20f, yOffset + 25f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f

                val melkavuDevathaInMalayalam: String?
                val melkavuDevathaInEnglish: String?

                when (item.vaSubTempleName) {
                    R.string.melkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.melkavu_devi, "en")
                    }
                    R.string.keezhkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.keezhkavu_devi, "en")
                    }
                    R.string.shiva.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.shiva, "en")
                    }
                    else -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.ayyappa, "en")
                    }
                }

                tempCanvas.drawText(melkavuDevathaInMalayalam, 20f, yOffset, paint)
                yOffset += 25f

                tempCanvas.drawText(melkavuDevathaInEnglish, 20f, yOffset, paint)
                yOffset += 25f

                val nameLines = splitText(item.vaOfferingsName, width - 40, paint)
                nameLines.forEach { line ->
                    tempCanvas.drawText(line, 20f, yOffset, paint)
                    yOffset += 25f
                }

                tempCanvas.drawText(item.vaOfferingsNameMa, 20f, yOffset, paint)

                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", item.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)

                totalAmount += item.vaOfferingsAmount
                yOffset += 35f
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }



        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 20f
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("ಒಟ್ಟು ಮೊತ್ತ (Total Amount): $totalAmountStr", width - 20f, yOffset, paint)
        paint.textSize = 22f
        yOffset += 30f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 30f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapTe(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String
    ): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("వాజిపాడు రసీదు", width / 2f, 80f, paint)


        var yOffset = 140f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("రసీదు సంఖ్య (Receipt No) : $transID", 20f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText("తేదీ (Date) : $currentDate", 20f, yOffset, paint)
        yOffset += 25f
        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        var totalAmount = 0.0
        allCartItems.forEachIndexed { index, personWithItems ->
            if (index > 0) {
                yOffset += 40f
            }

            paint.textSize = 28f
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(personWithItems.personName, 20f, yOffset, paint)

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = personWithItems.personStar.trim()
            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            if (starIndex >= 0) {
                tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
                tempCanvas.drawText(nakshatrasEn[starIndex], width - 20f, yOffset + 25f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f

                val melkavuDevathaInMalayalam: String?
                val melkavuDevathaInEnglish: String?

                when (item.vaSubTempleName) {
                    R.string.melkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.melkavu_devi, "en")
                    }
                    R.string.keezhkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.keezhkavu_devi, "en")
                    }
                    R.string.shiva.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.shiva, "en")
                    }
                    else -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.ayyappa, "en")
                    }
                }

                tempCanvas.drawText(melkavuDevathaInMalayalam, 20f, yOffset, paint)
                yOffset += 25f

                tempCanvas.drawText(melkavuDevathaInEnglish, 20f, yOffset, paint)
                yOffset += 25f

                val nameLines = splitText(item.vaOfferingsName, width - 40, paint)
                nameLines.forEach { line ->
                    tempCanvas.drawText(line, 20f, yOffset, paint)
                    yOffset += 25f
                }

                tempCanvas.drawText(item.vaOfferingsNameMa, 20f, yOffset, paint)

                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", item.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)

                totalAmount += item.vaOfferingsAmount
                yOffset += 35f
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }



        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 20f
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("మొత్తం మొత్తం (Total Amount): $totalAmountStr", width - 20f, yOffset, paint)
        paint.textSize = 22f
        yOffset += 30f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 30f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapHi(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String
    ): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("वज़ीपाडु रसीद", width / 2f, 80f, paint)


        var yOffset = 140f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("रसीद संख्या (Receipt No) : $transID", 20f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText("तारीख (Date) : $currentDate", 20f, yOffset, paint)
        yOffset += 25f
        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        var totalAmount = 0.0
        allCartItems.forEachIndexed { index, personWithItems ->
            if (index > 0) {
                yOffset += 40f
            }

            paint.textSize = 28f
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.LEFT
            tempCanvas.drawText(personWithItems.personName, 20f, yOffset, paint)

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = personWithItems.personStar.trim()
            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            if (starIndex >= 0) {
                tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
                tempCanvas.drawText(nakshatrasEn[starIndex], width - 20f, yOffset + 25f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f

                val melkavuDevathaInMalayalam: String?
                val melkavuDevathaInEnglish: String?

                when (item.vaSubTempleName) {
                    R.string.melkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.melkavu_devi, "en")
                    }
                    R.string.keezhkavu_devi.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.keezhkavu_devi, "en")
                    }
                    R.string.shiva.toString() -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.shiva, "en")
                    }
                    else -> {
                        melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                        melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.ayyappa, "en")
                    }
                }

                tempCanvas.drawText(melkavuDevathaInMalayalam, 20f, yOffset, paint)
                yOffset += 25f

                tempCanvas.drawText(melkavuDevathaInEnglish, 20f, yOffset, paint)
                yOffset += 25f

                val nameLines = splitText(item.vaOfferingsName, width - 40, paint)
                nameLines.forEach { line ->
                    tempCanvas.drawText(line, 20f, yOffset, paint)
                    yOffset += 25f
                }

                tempCanvas.drawText(item.vaOfferingsNameMa, 20f, yOffset, paint)

                paint.textAlign = Paint.Align.RIGHT
                val amountStr = String.format("%.2f", item.vaOfferingsAmount)
                tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)

                totalAmount += item.vaOfferingsAmount
                yOffset += 35f
            }

            paint.strokeWidth = 1f
            tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
            yOffset += 20f
        }



        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 20f
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("कुल राशि (Total Amount): $totalAmountStr", width - 20f, yOffset, paint)
        paint.textSize = 22f
        yOffset += 30f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 30f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        return finalBitmap
    }


    private fun splitText(text: String, maxWidth: Int, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        var start = 0
        val length = text.length
        while (start < length) {
            val count = paint.breakText(text, start, length, true, maxWidth.toFloat(), null)
            lines.add(text.substring(start, start + count))
            start += count
        }
        return lines
    }



    private fun redirect() {
        Handler(mainLooper).postDelayed({
            lifecycleScope.launch {
                startActivity(Intent(applicationContext, LanguageActivity::class.java))
                finish()
            }
        }, 1000)
    }

    private fun getLocalizedNakshatras(languageCode: String): Array<String> {
        val configuration = resources.configuration
        val originalLocale = configuration.locale
        val updatedConfiguration = Configuration(configuration)
        updatedConfiguration.setLocale(Locale(languageCode))
        val localizedResources = createConfigurationContext(updatedConfiguration).resources

        val localizedNakshatras = localizedResources.getStringArray(R.array.nakshatras)
        configuration.setLocale(originalLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        return localizedNakshatras
    }

    private fun getLocalizedDevathaName(devathaNameKey: Int, languageCode: String): String {
        val configuration = resources.configuration
        val originalLocale = configuration.locale
        val updatedConfiguration = Configuration(configuration)
        updatedConfiguration.setLocale(Locale(languageCode))
        val localizedResources = createConfigurationContext(updatedConfiguration).resources
        val localizedDevathaName = localizedResources.getString(devathaNameKey)
        configuration.setLocale(originalLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        return localizedDevathaName
    }



}

