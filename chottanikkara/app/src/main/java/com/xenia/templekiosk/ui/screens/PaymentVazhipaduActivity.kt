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
            val distinctPersons = vazhipaduRepository.getDistinctPersonsWithOfferings()
            val currentDate =
                SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date())
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

                for (personWithItems in distinctPersons) {
                    val personDevathaOfferings = personWithItems.items.groupBy {
                        it.vaSubTempleName
                    }
                    val devathasToProcess = personDevathaOfferings.keys.take(5)

                    devathasToProcess.forEachIndexed { _, devatha ->

                        val offerings = listOf(
                            PersonWithItems(
                                personName = personWithItems.personName,
                                personStar = personWithItems.personStar,
                                personStarLa = personWithItems.personStarLa,
                                items = personDevathaOfferings[devatha] ?: emptyList()
                            )
                        )

                        val bitmapDevasam = drawableDevasam.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                        val compressedBitmapDevasam =
                            Bitmap.createScaledBitmap(bitmapDevasam, 550, 200, true)

                        val bitmapDevi = drawableDevi.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                        val compressedBitmapDevi = Bitmap.createScaledBitmap(bitmapDevi, 500, 100, true)

                        val receiptBitmap: Bitmap = when (selectedLanguage) {
                            "en" -> generateReceiptBitmap(transID!!, offerings, currentDate, devatha)
                            "ml" -> generateReceiptBitmapMl(transID!!, offerings, currentDate, devatha)
                            "ta" -> generateReceiptBitmapTa(transID!!, offerings, currentDate, devatha)
                            "kn" -> generateReceiptBitmapKa(transID!!, offerings, currentDate, devatha)
                            "te" -> generateReceiptBitmapTe(transID!!, offerings, currentDate, devatha)
                            "hi" -> generateReceiptBitmapHi(transID!!, offerings, currentDate, devatha)
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
                }

                redirect()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmap(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String,
        devathaName: String
    ): Bitmap {
        val width = 576
        val paint = Paint().apply { isAntiAlias = true }

        val tempBitmap = Bitmap.createBitmap(width, 10000, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)

        // Header
        paint.textSize = 30f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        tempCanvas.drawText("വഴിപാട് രസീത്", width / 2f, 40f, paint)
        tempCanvas.drawText("Vazhipadu Receipt", width / 2f, 80f, paint)

        var yOffset = 130f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        tempCanvas.drawText("രസീത് നം (Receipt No): $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("തീയതി (Date): $currentDate", 20f, yOffset, paint)
        val melkavuDevathaInMalayalam: String
        val melkavuDevathaInEnglish: String
        when (devathaName) {
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
                tempCanvas.drawText(nakshatrasEn[starIndex], width - 20f, yOffset + 30f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f
                yOffset += 15f
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
        allCartItems: List<PersonWithItems>,
        currentDate: String,
        devatha: String
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
        tempCanvas.drawText("രസീത് നം : $orderID", 20f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText("തീയതി : $currentDate", 20f, yOffset, paint)
        val melkavuDevathaInMalayalam: String = when (devatha) {
            "മേൽക്കാവിൽ\nഭഗവതി" -> {
                getLocalizedDevathaName(R.string.melkavu_devi, "ml")
            }

            "കീഴ്ക്കാവിൽ\nഭഗവതി" -> {
                getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
            }

            "ശിവൻ\n" -> {
                getLocalizedDevathaName(R.string.shiva, "ml")
            }
            "നാഗം\n" -> {
                getLocalizedDevathaName(R.string.nagam, "ml")
            }
            else -> {
                getLocalizedDevathaName(R.string.ayyappa, "ml")
            }
        }
        yOffset += 25f
        tempCanvas.drawText("ദേവത: $melkavuDevathaInMalayalam", 20f, yOffset, paint)

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
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("ആകെ തുക: $totalAmountStr", width - 20f, yOffset, paint)

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
    private fun generateReceiptBitmapTa(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String,
        devatha: String
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
        tempCanvas.drawText("ரசீது எண் (രസീത് നം) : $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("தேதி (തീയതി) : $currentDate", 20f, yOffset, paint)
        val melkavuDevathaInMalayalam: String?
        val melkavuDevathaInTamil: String?
        when (devatha) {
            "மேல்காவில்\nபகவதி" -> {
                melkavuDevathaInMalayalam =
                    getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                melkavuDevathaInTamil = getLocalizedDevathaName(R.string.melkavu_devi, "ta")
            }

            "கீழ்காவில்\nபகவதி" -> {
                melkavuDevathaInMalayalam =
                    getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                melkavuDevathaInTamil =
                    getLocalizedDevathaName(R.string.keezhkavu_devi, "ta")
            }

            "சிவன்\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                melkavuDevathaInTamil = getLocalizedDevathaName(R.string.shiva, "ta")
            }

            "நாகம்\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.nagam, "ml")
                melkavuDevathaInTamil = getLocalizedDevathaName(R.string.nagam, "ta")
            }

            else -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                melkavuDevathaInTamil = getLocalizedDevathaName(R.string.ayyappa, "ta")
            }
        }
        yOffset += 35f
        tempCanvas.drawText("தேவி (ദേവത): $melkavuDevathaInMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(melkavuDevathaInTamil, 200f, yOffset, paint)
        yOffset += 35f
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
            val nakshatrasTa = getLocalizedNakshatras("ta")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = personWithItems.personStar.trim()
            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            if (starIndex >= 0) {
                tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
                tempCanvas.drawText(nakshatrasTa[starIndex], width - 20f, yOffset + 25f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f
                val nameLines = splitText(item.vaOfferingsNameTa, width - 40, paint)
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



        paint.textSize = 26f
        paint.textAlign = Paint.Align.RIGHT
        yOffset += 20f
        val totalAmountStr = String.format("%.2f", totalAmount)
        tempCanvas.drawText("மொத்த தொகை (ആകെ തുക): $totalAmountStr", width - 20f, yOffset, paint)
        paint.textSize = 22f
        yOffset += 30f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 30f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }


    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapKa(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String,
        devatha: String
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
        tempCanvas.drawText("ರಸೀದಿ ಸಂಖ್ಯೆ (രസീത് നം) : $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("ದಿನಾಂಕ (തീയതി) : $currentDate", 20f, yOffset, paint)
        val melkavuDevathaInMalayalam: String?
        val melkavuDevathaInKannada: String?

        when (devatha) {
            "ಮೇಲ್ಕಾವು\nದೇವಿ" -> {
                melkavuDevathaInMalayalam =
                    getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                melkavuDevathaInKannada =
                    getLocalizedDevathaName(R.string.melkavu_devi, "kn")
            }

            "ಕೀಳ್ಕಾವು\nದೇವಿ" -> {
                melkavuDevathaInMalayalam =
                    getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                melkavuDevathaInKannada =
                    getLocalizedDevathaName(R.string.keezhkavu_devi, "kn")
            }

            "ಶಿವ\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                melkavuDevathaInKannada = getLocalizedDevathaName(R.string.shiva, "kn")
            }

            "ನಾಗಂ\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.nagam, "ml")
                melkavuDevathaInKannada = getLocalizedDevathaName(R.string.nagam, "kn")
            }

            else -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                melkavuDevathaInKannada = getLocalizedDevathaName(R.string.ayyappa, "kn")
            }
        }
        yOffset += 35f
        tempCanvas.drawText("ದೇವತಾ (ദേവത): $melkavuDevathaInMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(melkavuDevathaInKannada, 200f, yOffset, paint)
        yOffset += 35f
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
            val nakshatrasKa = getLocalizedNakshatras("kn")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = personWithItems.personStar.trim()
            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            if (starIndex >= 0) {
                tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
                tempCanvas.drawText(nakshatrasKa[starIndex], width - 20f, yOffset + 25f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f
                val nameLines = splitText(item.vaOfferingsNameKa, width - 40, paint)
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
        tempCanvas.drawText("ಒಟ್ಟು ಮೊತ್ತ (ആകെ തുക): $totalAmountStr", width - 20f, yOffset, paint)
        paint.textSize = 22f
        yOffset += 30f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 30f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapTe(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String,
        devatha: String
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
        tempCanvas.drawText("రసీదు సంఖ్య (രസീത് നം) : $orderID", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText("తేదీ (തീയതി) : $currentDate", 20f, yOffset, paint)

        val melkavuDevathaInMalayalam: String?
        val melkavuDevathaInTelugu: String?

        when (devatha) {
            "మెల్కావిల\nభగవతి" -> {
                melkavuDevathaInMalayalam =
                    getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                melkavuDevathaInTelugu =
                    getLocalizedDevathaName(R.string.melkavu_devi, "te")
            }

            "కీజ్\u200Cకావిల్\nభగవతి" -> {
                melkavuDevathaInMalayalam =
                    getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                melkavuDevathaInTelugu =
                    getLocalizedDevathaName(R.string.keezhkavu_devi, "te")
            }

            "శివన్\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                melkavuDevathaInTelugu = getLocalizedDevathaName(R.string.shiva, "te")
            }

            "నాగం\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.nagam, "ml")
                melkavuDevathaInTelugu = getLocalizedDevathaName(R.string.nagam, "te")
            }

            else -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                melkavuDevathaInTelugu = getLocalizedDevathaName(R.string.ayyappa, "te")
            }
        }
        yOffset += 35f
        tempCanvas.drawText("దేవత (ദേവത): $melkavuDevathaInMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(melkavuDevathaInTelugu, 200f, yOffset, paint)
        yOffset += 35f
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
            val nakshatrasTe = getLocalizedNakshatras("te")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = personWithItems.personStar.trim()
            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            if (starIndex >= 0) {
                tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
                tempCanvas.drawText(nakshatrasTe[starIndex], width - 20f, yOffset + 25f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f
                val nameLines = splitText(item.vaOfferingsNameTe, width - 40, paint)
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
        tempCanvas.drawText("మొత్తం మొత్తం (ആകെ തുക): $totalAmountStr", width - 20f, yOffset, paint)
        paint.textSize = 22f
        yOffset += 30f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 30f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

        tempBitmap.recycle()

        return finalBitmap
    }

    @SuppressLint("DefaultLocale")
    private fun generateReceiptBitmapHi(
        transID: String,
        allCartItems: List<PersonWithItems>,
        currentDate: String,
        devatha: String
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
        tempCanvas.drawText("रसीद संख्या (രസീത് നം) : $orderID", 20f, yOffset, paint)
        yOffset += 25f
        tempCanvas.drawText("तारीख (തീയതി) : $currentDate", 20f, yOffset, paint)
        val melkavuDevathaInMalayalam: String?
        val melkavuDevathaInHindi: String?

        when (devatha) {
            "मेल्काविल\nभगवती" -> {
                melkavuDevathaInMalayalam =
                    getLocalizedDevathaName(R.string.melkavu_devi, "ml")
                melkavuDevathaInHindi = getLocalizedDevathaName(R.string.melkavu_devi, "hi")
            }

            "कीझ्काविल\nभगवती" -> {
                melkavuDevathaInMalayalam =
                    getLocalizedDevathaName(R.string.keezhkavu_devi, "ml")
                melkavuDevathaInHindi =
                    getLocalizedDevathaName(R.string.keezhkavu_devi, "hi")
            }

            "शिवन\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.shiva, "ml")
                melkavuDevathaInHindi = getLocalizedDevathaName(R.string.shiva, "hi")
            }

            "नागम\n" -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.nagam, "ml")
                melkavuDevathaInHindi = getLocalizedDevathaName(R.string.nagam, "hi")
            }

            else -> {
                melkavuDevathaInMalayalam = getLocalizedDevathaName(R.string.ayyappa, "ml")
                melkavuDevathaInHindi = getLocalizedDevathaName(R.string.ayyappa, "hi")
            }
        }
        yOffset += 35f
        tempCanvas.drawText("దేవత (ദേവത): $melkavuDevathaInMalayalam", 20f, yOffset, paint)
        yOffset += 35f
        tempCanvas.drawText(melkavuDevathaInHindi, 200f, yOffset, paint)
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
            val nakshatrasHi = getLocalizedNakshatras("hi")
            val nakshatrasEn = getLocalizedNakshatras("en")
            val starName = personWithItems.personStar.trim()
            val starIndex = nakshatrasEn.indexOfFirst { it.equals(starName, ignoreCase = true) }
            if (starIndex >= 0) {
                tempCanvas.drawText(nakshatrasMl[starIndex], width - 20f, yOffset, paint)
                tempCanvas.drawText(nakshatrasHi[starIndex], width - 20f, yOffset + 25f, paint)
            } else {
                tempCanvas.drawText("Unknown", width - 20f, yOffset, paint)
                tempCanvas.drawText("Unknown", width - 20f, yOffset + 25f, paint)
            }
            yOffset += 60f

            personWithItems.items.forEach { item ->
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 20f



                tempCanvas.drawText(melkavuDevathaInMalayalam, 20f, yOffset, paint)
                yOffset += 25f

                tempCanvas.drawText(melkavuDevathaInHindi, 20f, yOffset, paint)
                yOffset += 25f

                val nameLines = splitText(item.vaOfferingsNameHi, width - 40, paint)
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
        tempCanvas.drawText("कुल राशि (ആകെ തുക): $totalAmountStr", width - 20f, yOffset, paint)
        paint.textSize = 22f
        yOffset += 30f
        tempCanvas.drawText("UPI Reference No: $transID", width - 20f, yOffset, paint)
        yOffset += 30f


        val finalBitmap = Bitmap.createBitmap(width, yOffset.toInt(), Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)
        finalCanvas.drawBitmap(tempBitmap, 0f, 0f, null)

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

