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
import com.xenia.templekiosk.data.repository.VazhipaduRepository
import com.xenia.templekiosk.data.room.entity.Vazhipadu
import com.xenia.templekiosk.databinding.ActivityPaymentBinding
import com.xenia.templekiosk.utils.common.CommonMethod.setLocale
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
            val distinctPersons = vazhipaduRepository.getAllVazhipaduItems()
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

                for (offerings in distinctPersons) {
                    val bitmapDevasam = drawableDevasam.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val compressedBitmapDevasam =
                        Bitmap.createScaledBitmap(bitmapDevasam, 550, 200, true)

                    val bitmapDevi = drawableDevi.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val compressedBitmapDevi =
                        Bitmap.createScaledBitmap(bitmapDevi, 500, 100, true)

                    val receiptBitmap: Bitmap = when (selectedLanguage) {
                        "en" -> generateReceiptBitmap(transID!!, offerings, currentDate)
                        "ml" -> generateReceiptBitmapMl(transID!!, offerings, currentDate)
                        "ta" -> generateReceiptBitmapTa(transID!!, offerings, currentDate)
                        "kn" -> generateReceiptBitmapKa(transID!!, offerings, currentDate)
                        "te" -> generateReceiptBitmapTe(transID!!, offerings, currentDate)
                        "hi" -> generateReceiptBitmapHi(transID!!, offerings, currentDate)
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
                }

                redirect()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmap(
        transID: String,
        offerings: Vazhipadu,
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
        val melkavuDevathaInMalayalam: String
        val melkavuDevathaInEnglish: String
        when (offerings.vaSubTempleName) {
            "MELKAVU\nBHAGAVATI" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.melkavu_devi, "en")
            }
            "KEEZHKAVU\nBHAGAVATI" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.keezhkavu_devi, "en")
            }
            "SHIVAN\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.shiva, "en")
            }
            "NAGAM\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.nagam, "ml")
                melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.nagam, "en")
            }
            else -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                melkavuDevathaInEnglish = getLocalizedDevathaName(R.string.ayyappa, "en")
            }
        }

        yOffset += 35f
        tempCanvas.drawText("ദേവത (Devatha): $melkavuDevathaInMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(melkavuDevathaInEnglish, 205f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("വിഭാഗം (Category): ${offerings.vaCategoryNameMa}", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(offerings.vaCategoryName, 220f, yOffset, paint)
        yOffset += 35f

        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 28f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT
        tempCanvas.drawText(offerings.vaName, 20f, yOffset, paint)

        paint.textAlign = Paint.Align.RIGHT
        val nakshatrasMl = getLocalizedNakshatras("ml")
        val nakshatrasEn = getLocalizedNakshatras("en")
        val starName = offerings.vaStar.trim()
        val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
        if (starIndex >= 0) {
            tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
            tempCanvas.drawText(nakshatrasEn[starIndex], width - 20f, yOffset + 30f, paint)
        } else {
            tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
            tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
        }

        yOffset += 60f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 20f
        val nameLines = splitText(offerings.vaOfferingsName, width - 40, paint)
        nameLines.forEach { line ->
            tempCanvas.drawText(line, 20f, yOffset, paint)
            yOffset += 25f
        }

        tempCanvas.drawText(offerings.vaOfferingsNameMa, 20f, yOffset, paint)

        paint.textAlign = Paint.Align.RIGHT
        val amountStr = String.format("%.2f", offerings.vaOfferingsAmount)
        tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)

        val totalAmount = offerings.vaOfferingsAmount
        yOffset += 35f

        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 20f

        yOffset += 20f
        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("ആകെ തുക (Total Amount): $totalAmountStr", width - 20f, yOffset, paint)
        yOffset += 35f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        yOffset += 40f

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapMl(
        transID: String,
        offering: Vazhipadu,
        currentDate: String
    ): Bitmap {
        val width = 576

        val tempBitmap = Bitmap.createBitmap(width, 1000, Bitmap.Config.ARGB_8888)
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
        yOffset += 35f
        tempCanvas.drawText("തീയതി : $currentDate", 20f, yOffset, paint)

        yOffset += 35f
        tempCanvas.drawText("ദേവത: ${offering.vaSubTempleName}", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("വിഭാഗം: ${offering.vaCategoryNameMa}", 20f, yOffset, paint)
        yOffset += 35f
        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 30f
        tempCanvas.drawText(offering.vaName, 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 22f
        paint.textAlign = Paint.Align.LEFT
        tempCanvas.drawText(offering.vaOfferingsNameMa, 20f, yOffset, paint)

        paint.textAlign = Paint.Align.RIGHT
        val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
        tempCanvas.drawText(amountStr, width - 20f, yOffset, paint)

        yOffset += 40f
        paint.strokeWidth = 1f
        tempCanvas.drawLine(20f, yOffset, width - 20f, yOffset, paint)
        yOffset += 40f

        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        tempCanvas.drawText("ആകെ തുക: $amountStr", width - 20f, yOffset, paint)

        yOffset += 40f
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapTa(
        transID: String,
        offering: Vazhipadu,
        currentDate: String
    ): Bitmap {
        val width = 576
        val initialHeight = 1000
        val tempBitmap = Bitmap.createBitmap(width, initialHeight, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        val headerTextSize = 30f
        val contentTextSize = 22f
        val lineSpacing = 35f
        val leftPadding = 20f


        paint.textSize = headerTextSize
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("வழிபாடு ரசீது", width / 2f, 80f, paint)

        var yOffset = 140f
        paint.textSize = contentTextSize
        paint.textAlign = Paint.Align.LEFT
        tempCanvas.drawText("ரசீது எண் (രസീത് നം): $orderID", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText("தேதி (തീയതി): $currentDate", leftPadding, yOffset, paint)

        val devathaMalayalam = when (offering.vaSubTempleName) {
            "மேல்காவில்\nபகவதி" -> getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            "கீழ்காவில்\nபகவதி" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            "சிவன்\n" -> getLocalizedDevathaName(R.string.shiva, "ml")
            "நாகம்\n" -> getLocalizedDevathaName(R.string.nagam, "ml")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ml")
        }

        val devathaTamil = when (offering.vaSubTempleName) {
            "மேல்காவில்\nபகவதி" -> getLocalizedDevathaName(R.string.melkavu_devi, "ta")
            "கீழ்காவில்\nபகவதி" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ta")
            "சிவன்\n" -> getLocalizedDevathaName(R.string.shiva, "ta")
            "நாகம்\n" -> getLocalizedDevathaName(R.string.nagam, "ta")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ta")
        }

        yOffset += lineSpacing
        tempCanvas.drawText("தேவி (ദേവത): $devathaMalayalam", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText(devathaTamil, leftPadding + 180f, yOffset, paint)


        yOffset += lineSpacing
        tempCanvas.drawText("வகை (വിഭാഗം): ${offering.vaCategoryNameMa}", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText(offering.vaCategoryNameTa, leftPadding + 180f, yOffset, paint)

        yOffset += lineSpacing
        paint.strokeWidth = 1f
        tempCanvas.drawLine(leftPadding, yOffset, width - leftPadding, yOffset, paint)
        yOffset += lineSpacing


        paint.textSize = 20f
        val nameLines = splitText(offering.vaOfferingsNameTa, width - 40, paint)
        nameLines.forEach { line ->
            tempCanvas.drawText(line, leftPadding, yOffset, paint)
            yOffset += 25f
        }
        tempCanvas.drawText(offering.vaOfferingsNameMa, leftPadding, yOffset, paint)

        paint.textAlign = Paint.Align.RIGHT
        val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
        tempCanvas.drawText(amountStr, width - leftPadding, yOffset, paint)


        yOffset += lineSpacing * 2
        paint.textSize = 26f
        tempCanvas.drawText("மொத்த தொகை (ആകെ തുക): $amountStr", width - leftPadding, yOffset, paint)


        yOffset += lineSpacing
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - leftPadding, yOffset, paint)

        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(finalBitmap).apply {
            drawBitmap(tempBitmap, 0f, 0f, null)
        }

        tempBitmap.recycle()
        return finalBitmap
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapKa(
        transID: String,
        offering: Vazhipadu,
        currentDate: String
    ): Bitmap {
        val width = 576
        val initialHeight = 1000
        val tempBitmap = Bitmap.createBitmap(width, initialHeight, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        val headerTextSize = 30f
        val contentTextSize = 22f
        val lineSpacing = 35f
        val leftPadding = 20f


        paint.textSize = headerTextSize
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("ವಜಿಪಾಡು ರಸೀದಿ", width / 2f, 80f, paint)

        var yOffset = 140f
        paint.textSize = contentTextSize
        paint.textAlign = Paint.Align.LEFT
        tempCanvas.drawText("ರಸೀದಿ ಸಂಖ್ಯೆ (രസീത് നം): $orderID", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText("ದಿನಾಂಕ (തീയതി): $currentDate", leftPadding, yOffset, paint)

        val devathaMalayalam = when (offering.vaSubTempleName) {
            "ಮೇಲ್ಕಾವು\nದೇವಿ" -> getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            "ಕೀಳ್ಕಾವು\nದೇವಿ" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            "ಶಿವ\n"  -> getLocalizedDevathaName(R.string.shiva, "ml")
            "ನಾಗಂ\n" -> getLocalizedDevathaName(R.string.nagam, "ml")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ml")
        }

        val devathaKannada = when (offering.vaSubTempleName) {
            "ಮೇಲ್ಕಾವು\nದೇವಿ" -> getLocalizedDevathaName(R.string.melkavu_devi, "kn")
            "ಕೀಳ್ಕಾವು\nದೇವಿ" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "kn")
            "ಶಿವ\n" -> getLocalizedDevathaName(R.string.shiva, "kn")
            "ನಾಗಂ\n" -> getLocalizedDevathaName(R.string.nagam, "kn")
            else -> getLocalizedDevathaName(R.string.ayyappa, "kn")
        }

        yOffset += lineSpacing
        tempCanvas.drawText("ದೇವತಾ (ദേവത): $devathaMalayalam", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText(devathaKannada, leftPadding + 180f, yOffset, paint)

        yOffset += lineSpacing
        tempCanvas.drawText("ವರ್ಗ (വിഭാഗം): ${offering.vaCategoryNameMa}", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText(offering.vaCategoryNameKa, leftPadding + 180f, yOffset, paint)

        yOffset += lineSpacing
        paint.strokeWidth = 1f
        tempCanvas.drawLine(leftPadding, yOffset, width - leftPadding, yOffset, paint)
        yOffset += lineSpacing

        paint.textSize = 20f
        val nameLines = splitText(offering.vaOfferingsNameKa, width - 40, paint)
        nameLines.forEach { line ->
            tempCanvas.drawText(line, leftPadding, yOffset, paint)
            yOffset += 25f
        }
        tempCanvas.drawText(offering.vaOfferingsNameMa, leftPadding, yOffset, paint)

        paint.textAlign = Paint.Align.RIGHT
        val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
        tempCanvas.drawText(amountStr, width - leftPadding, yOffset, paint)

        yOffset += lineSpacing * 2
        paint.textSize = 26f
        tempCanvas.drawText("ಒಟ್ಟು ಮೊತ್ತ (ആകെ തുക): $amountStr", width - leftPadding, yOffset, paint)


        yOffset += lineSpacing
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - leftPadding, yOffset, paint)


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(finalBitmap).apply {
            drawBitmap(tempBitmap, 0f, 0f, null)
        }

        tempBitmap.recycle()
        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapTe(
        transID: String,
        offering: Vazhipadu,
        currentDate: String
    ): Bitmap {
        val width = 576
        val initialHeight = 1000
        val tempBitmap = Bitmap.createBitmap(width, initialHeight, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        val headerTextSize = 30f
        val contentTextSize = 22f
        val lineSpacing = 35f
        val leftPadding = 20f


        paint.textSize = headerTextSize
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("వాజిపాడు రసీదు", width / 2f, 80f, paint)

        var yOffset = 140f
        paint.textSize = contentTextSize
        paint.textAlign = Paint.Align.LEFT
        tempCanvas.drawText("రసీదు సంఖ్య (രസീത് നം): $transID", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText("తేదీ (തീയതി): $currentDate", leftPadding, yOffset, paint)

        val devathaMalayalam = when (offering.vaSubTempleName) {
            "మెల్కావిల\nభగవతి" -> getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            "కీజ్\u200Cకావిల్\nభగవతి" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            "శివన్\n"  -> getLocalizedDevathaName(R.string.shiva, "ml")
            "నాగం\n" -> getLocalizedDevathaName(R.string.nagam, "ml")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ml")
        }

        val devathaTelugu = when (offering.vaSubTempleName) {
            "మెల్కావిల\nభగవతి" -> getLocalizedDevathaName(R.string.melkavu_devi, "te")
            "కీజ్\u200Cకావిల్\nభగవతి" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "te")
            "శివన్\n" -> getLocalizedDevathaName(R.string.shiva, "te")
            "నాగం\n" -> getLocalizedDevathaName(R.string.nagam, "te")
            else -> getLocalizedDevathaName(R.string.ayyappa, "te")
        }

        yOffset += lineSpacing
        tempCanvas.drawText("దేవత (ദേവത): $devathaMalayalam", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText(devathaTelugu, leftPadding + 180f, yOffset, paint)

        yOffset += lineSpacing
        tempCanvas.drawText("వర్గం (വിഭാഗം): ${offering.vaCategoryNameMa}", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText(offering.vaCategoryNameTe, leftPadding + 180f, yOffset, paint)

        yOffset += lineSpacing
        paint.strokeWidth = 1f
        tempCanvas.drawLine(leftPadding, yOffset, width - leftPadding, yOffset, paint)
        yOffset += lineSpacing

        paint.textSize = 20f
        val nameLines = splitText(offering.vaOfferingsNameTe, width - 40, paint)
        nameLines.forEach { line ->
            tempCanvas.drawText(line, leftPadding, yOffset, paint)
            yOffset += 25f
        }
        tempCanvas.drawText(offering.vaOfferingsNameMa, leftPadding, yOffset, paint)

        paint.textAlign = Paint.Align.RIGHT
        val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
        tempCanvas.drawText(amountStr, width - leftPadding, yOffset, paint)

        yOffset += lineSpacing * 2
        paint.textSize = 26f
        tempCanvas.drawText("మొత్తం మొత్తం (ആകെ തുക): $amountStr", width - leftPadding, yOffset, paint)


        yOffset += lineSpacing
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - leftPadding, yOffset, paint)


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(finalBitmap).apply {
            drawBitmap(tempBitmap, 0f, 0f, null)
        }

        tempBitmap.recycle()
        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapHi(
        transID: String,
        offering: Vazhipadu,
        currentDate: String
    ): Bitmap {
        val width = 576
        val initialHeight = 1000
        val tempBitmap = Bitmap.createBitmap(width, initialHeight, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        val headerTextSize = 30f
        val contentTextSize = 22f
        val lineSpacing = 35f
        val leftPadding = 20f


        paint.textSize = headerTextSize
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("वज़ीपाडु रसीद", width / 2f, 80f, paint)

        var yOffset = 140f
        paint.textSize = contentTextSize
        paint.textAlign = Paint.Align.LEFT
        tempCanvas.drawText("रसीद संख्या (രസീത് നം): $transID", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText("तारीख (തീയതി): $currentDate", leftPadding, yOffset, paint)

        val devathaMalayalam = when (offering.vaSubTempleName) {
            "मेल्काविल\nभगवती" -> getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            "कीझ्काविल\nभगवती" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            "शिवन\n"  -> getLocalizedDevathaName(R.string.shiva, "ml")
            "नागम\n" -> getLocalizedDevathaName(R.string.nagam, "ml")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ml")
        }

        val devathaTelugu = when (offering.vaSubTempleName) {
            "मेल्काविल\nभगवती" -> getLocalizedDevathaName(R.string.melkavu_devi, "te")
            "कीझ्काविल\nभगवती" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "te")
            "शिवन\n" -> getLocalizedDevathaName(R.string.shiva, "te")
            "नागम\n" -> getLocalizedDevathaName(R.string.nagam, "te")
            else -> getLocalizedDevathaName(R.string.ayyappa, "te")
        }

        yOffset += lineSpacing
        tempCanvas.drawText("देवथा (ദേവത): $devathaMalayalam", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText(devathaTelugu, leftPadding + 180f, yOffset, paint)

        yOffset += lineSpacing
        tempCanvas.drawText("वर्ग (വിഭാഗം): ${offering.vaCategoryNameMa}", leftPadding, yOffset, paint)
        yOffset += lineSpacing
        tempCanvas.drawText(offering.vaCategoryNameHi, leftPadding + 180f, yOffset, paint)

        yOffset += lineSpacing
        paint.strokeWidth = 1f
        tempCanvas.drawLine(leftPadding, yOffset, width - leftPadding, yOffset, paint)
        yOffset += lineSpacing

        paint.textSize = 20f
        val nameLines = splitText(offering.vaOfferingsNameHi, width - 40, paint)
        nameLines.forEach { line ->
            tempCanvas.drawText(line, leftPadding, yOffset, paint)
            yOffset += 25f
        }
        tempCanvas.drawText(offering.vaOfferingsNameMa, leftPadding, yOffset, paint)

        paint.textAlign = Paint.Align.RIGHT
        val amountStr = String.format("%.2f", offering.vaOfferingsAmount)
        tempCanvas.drawText(amountStr, width - leftPadding, yOffset, paint)

        yOffset += lineSpacing * 2
        paint.textSize = 26f
        tempCanvas.drawText("कुल राशि (ആകെ തുക): $amountStr", width - leftPadding, yOffset, paint)


        yOffset += lineSpacing
        paint.textSize = 22f
        tempCanvas.drawText("UPI Reference No: $transID", width - leftPadding, yOffset, paint)


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(finalBitmap).apply {
            drawBitmap(tempBitmap, 0f, 0f, null)
        }

        tempBitmap.recycle()
        return finalBitmap
    }


    private fun splitText(text: String, maxWidth: Int, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        var start = 0
        val length = text.length

        while (start < length) {
            val breakIndex = paint.breakText(text.substring(start), true, maxWidth.toFloat(), null)
            if (breakIndex > 0) {
                lines.add(text.substring(start, start + breakIndex))
                start += breakIndex
            } else {
                lines.add(text.substring(start, start + 1))
                start++
            }
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

