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
                val groupedByDeityAndCategory = allVazhipaduItems.groupBy {
                    it.vaSubTempleName to it.vaCategoryName
                }

                val printer = POSPrinter(curConnect)

                for ((key, offerings) in groupedByDeityAndCategory) {
                    val (deityName, categoryName) = key

                    val categoryNameMa = offerings.firstOrNull()?.vaCategoryNameMa ?: categoryName
                    val categoryNameTa = offerings.firstOrNull()?.vaCategoryNameTa ?: categoryName
                    val categoryNameKa = offerings.firstOrNull()?.vaCategoryNameKa ?: categoryName
                    val categoryNameTe = offerings.firstOrNull()?.vaCategoryNameTe ?: categoryName
                    val categoryNameHi = offerings.firstOrNull()?.vaCategoryNameHi ?: categoryName

                    val bitmapDevasam = drawableDevasam.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val compressedBitmapDevasam =
                        Bitmap.createScaledBitmap(bitmapDevasam, 550, 200, true)

                    val bitmapDevi = drawableDevi.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val compressedBitmapDevi =
                        Bitmap.createScaledBitmap(bitmapDevi, 500, 100, true)

                    val receiptBitmap: Bitmap = when (selectedLanguage) {
                        "en" -> generateReceiptBitmap(transID!!, deityName, categoryName,categoryNameMa, offerings, currentDate)
                        "ml" -> generateReceiptBitmapMl(transID!!, deityName, categoryNameMa,offerings, currentDate)
                        "ta" -> generateReceiptBitmapTa(transID!!, deityName,categoryNameMa,categoryNameTa, offerings, currentDate)
                        "kn" -> generateReceiptBitmapKa(transID!!, deityName,categoryNameMa,categoryNameKa, offerings, currentDate)
                        "te" -> generateReceiptBitmapTe(transID!!, deityName,categoryNameMa,categoryNameTe, offerings, currentDate)
                        "hi" -> generateReceiptBitmapHi(transID!!, deityName,categoryNameMa,categoryNameHi, offerings, currentDate)
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
        deityName: String,
        categoryName: String,
        categoryNameMa: String,
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

        val devathaMalayalam = when (deityName) {
            "MELKAVU\nBHAGAVATI" ->  getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            "KEEZHKAVU\nBHAGAVATI" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            "SHIVAN\n" -> getLocalizedDevathaName(R.string.shiva, "ml")
            "NAGAM\n" -> getLocalizedDevathaName(R.string.nagam, "ml")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ml")
        }

        tempCanvas.drawText("ദേവത (Devatha): $devathaMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(deityName, 210f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("വിഭാഗം (Category): $categoryNameMa", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(categoryName, 220f, yOffset, paint)
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

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = offeringsInGroup.firstOrNull()?.vaStar?.trim() ?: "Unknown"

            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            val starDisplayEn = if (starIndex >= 0) nakshatrasEn[starIndex] else "Unknown"
            val starDisplayMl = if (starIndex >= 0) nakshatrasMl[starIndex] else "Unknown"

            tempCanvas.drawText(starDisplayMl, width - 20f, yOffset, paint)
            yOffset += 35f
            tempCanvas.drawText(starDisplayEn, width - 20f, yOffset, paint)
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
        deityName: String,
        categoryName: String,
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
        tempCanvas.drawText("ദേവത : $deityName", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("വിഭാഗം : $categoryName", 20f, yOffset, paint)
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

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = offeringsInGroup.firstOrNull()?.vaStar?.trim() ?: "Unknown"

            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            val starDisplayMl = if (starIndex >= 0) nakshatrasMl[starIndex] else "Unknown"

            tempCanvas.drawText(starDisplayMl, width - 20f, yOffset, paint)

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
        deityName: String,
        categoryNameMa: String,
        categoryNameTa: String,
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

        val devathaMalayalam = when (deityName) {
            "மேல்காவில்\nபகவதி" -> getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            "கீழ்காவில்\nபகவதி" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            "சிவன்\n" -> getLocalizedDevathaName(R.string.shiva, "ml")
            "நாகம்\n" -> getLocalizedDevathaName(R.string.nagam, "ml")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ml")
        }

        val devathaTamil = when (deityName) {
            "மேல்காவில்\nபகவதி" -> getLocalizedDevathaName(R.string.melkavu_devi, "ta")
            "கீழ்காவில்\nபகவதி" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ta")
            "சிவன்\n" -> getLocalizedDevathaName(R.string.shiva, "ta")
            "நாகம்\n" -> getLocalizedDevathaName(R.string.nagam, "ta")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ta")
        }

        tempCanvas.drawText("ദേവത (Devatha): $devathaMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(devathaTamil, 210f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("വിഭാഗം (Category): $categoryNameMa", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(categoryNameTa, 220f, yOffset, paint)
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

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val nakshatrasTa = getLocalizedNakshatras("ta")
            val starName = offeringsInGroup.firstOrNull()?.vaStar?.trim() ?: "Unknown"

            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            val starDisplayTa = if (starIndex >= 0) nakshatrasTa[starIndex] else "Unknown"
            val starDisplayMl = if (starIndex >= 0) nakshatrasMl[starIndex] else "Unknown"

            tempCanvas.drawText(starDisplayMl, width - 20f, yOffset, paint)
            yOffset += 35f
            tempCanvas.drawText(starDisplayTa, width - 20f, yOffset, paint)
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
        deityName: String,
        categoryNameMa: String,
        categoryNameKa: String,
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

        val devathaMalayalam = when (deityName) {
            "ಮೇಲ್ಕಾವು\nದೇವಿ" -> getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            "ಕೀಳ್ಕಾವು\nದೇವಿ" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            "ಶಿವ\n"  -> getLocalizedDevathaName(R.string.shiva, "ml")
            "ನಾಗಂ\n" -> getLocalizedDevathaName(R.string.nagam, "ml")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ml")
        }

        val devathaKannada = when (deityName) {
            "ಮೇಲ್ಕಾವು\nದೇವಿ" -> getLocalizedDevathaName(R.string.melkavu_devi, "kn")
            "ಕೀಳ್ಕಾವು\nದೇವಿ" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "kn")
            "ಶಿವ\n" -> getLocalizedDevathaName(R.string.shiva, "kn")
            "ನಾಗಂ\n" -> getLocalizedDevathaName(R.string.nagam, "kn")
            else -> getLocalizedDevathaName(R.string.ayyappa, "kn")
        }


        tempCanvas.drawText("ദേവത (Devatha): $devathaMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(devathaKannada, 210f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("വിഭാഗം (Category): $categoryNameMa", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(categoryNameKa, 220f, yOffset, paint)
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

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val nakshatrasKn = getLocalizedNakshatras("kn")
            val starName = offeringsInGroup.firstOrNull()?.vaStar?.trim() ?: "Unknown"

            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            val starDisplayKn = if (starIndex >= 0) nakshatrasKn[starIndex] else "Unknown"
            val starDisplayMl = if (starIndex >= 0) nakshatrasMl[starIndex] else "Unknown"

            tempCanvas.drawText(starDisplayMl, width - 20f, yOffset, paint)
            yOffset += 35f
            tempCanvas.drawText(starDisplayKn, width - 20f, yOffset, paint)
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
        deityName: String,
        categoryNameMa: String,
        categoryNameKa: String,
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

        val devathaMalayalam = when (deityName) {
            "మెల్కావిల\nభగవతి" -> getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            "కీజ్\u200Cకావిల్\nభగవతి" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            "శివన్\n"  -> getLocalizedDevathaName(R.string.shiva, "ml")
            "నాగం\n" -> getLocalizedDevathaName(R.string.nagam, "ml")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ml")
        }

        val devathaTelugu = when (deityName) {
            "మెల్కావిల\nభగవతి" -> getLocalizedDevathaName(R.string.melkavu_devi, "te")
            "కీజ్\u200Cకావిల్\nభగవతి" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "te")
            "శివన్\n" -> getLocalizedDevathaName(R.string.shiva, "te")
            "నాగం\n" -> getLocalizedDevathaName(R.string.nagam, "te")
            else -> getLocalizedDevathaName(R.string.ayyappa, "te")
        }


        tempCanvas.drawText("ദേവത (Devatha): $devathaMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(devathaTelugu, 210f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("വിഭാഗം (Category): $categoryNameMa", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(categoryNameKa, 220f, yOffset, paint)
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

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val nakshatrasTe = getLocalizedNakshatras("te")
            val starName = offeringsInGroup.firstOrNull()?.vaStar?.trim() ?: "Unknown"

            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            val starDisplayTe = if (starIndex >= 0) nakshatrasTe[starIndex] else "Unknown"
            val starDisplayMl = if (starIndex >= 0) nakshatrasMl[starIndex] else "Unknown"

            tempCanvas.drawText(starDisplayMl, width - 20f, yOffset, paint)
            yOffset += 35f
            tempCanvas.drawText(starDisplayTe, width - 20f, yOffset, paint)
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
        deityName: String,
        categoryNameMa: String,
        categoryNameHi: String,
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

        val devathaMalayalam = when (deityName) {
            "मेल्काविल\nभगवती" -> getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            "कीझ्काविल\nभगवती" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            "शिवन\n"  -> getLocalizedDevathaName(R.string.shiva, "ml")
            "नागम\n" -> getLocalizedDevathaName(R.string.nagam, "ml")
            else -> getLocalizedDevathaName(R.string.ayyappa, "ml")
        }

        val devathaHindi = when (deityName) {
            "मेल्काविल\nभगवती" -> getLocalizedDevathaName(R.string.melkavu_devi, "hi")
            "कीझ्काविल\nभगवती" -> getLocalizedDevathaName(R.string.keezhkavu_devi, "hi")
            "शिवन\n" -> getLocalizedDevathaName(R.string.shiva, "hi")
            "नागम\n" -> getLocalizedDevathaName(R.string.nagam, "hi")
            else -> getLocalizedDevathaName(R.string.ayyappa, "hi")
        }


        tempCanvas.drawText("ദേവത (Devatha): $devathaMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(devathaHindi, 210f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("വിഭാഗം (Category): $categoryNameMa", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(categoryNameHi, 220f, yOffset, paint)
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

            paint.textAlign = Paint.Align.RIGHT
            val nakshatrasMl = getLocalizedNakshatras("ml")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val nakshatrasTe = getLocalizedNakshatras("te")
            val starName = offeringsInGroup.firstOrNull()?.vaStar?.trim() ?: "Unknown"

            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            val starDisplayTe = if (starIndex >= 0) nakshatrasTe[starIndex] else "Unknown"
            val starDisplayMl = if (starIndex >= 0) nakshatrasMl[starIndex] else "Unknown"

            tempCanvas.drawText(starDisplayMl, width - 20f, yOffset, paint)
            yOffset += 35f
            tempCanvas.drawText(starDisplayTe, width - 20f, yOffset, paint)
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

