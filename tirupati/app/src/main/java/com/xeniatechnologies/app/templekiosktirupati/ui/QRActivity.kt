package com.xeniatechnologies.app.templekiosktirupati.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.xenia.templekiosk.utils.common.CommonMethod.convertNumberToWords
import com.xeniatechnologies.app.templekiosktirupati.R
import com.xeniatechnologies.app.templekiosktirupati.databinding.ActivityQractivityBinding

class QRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQractivityBinding

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val donationAmount = intent.getStringExtra("donationAmount")?.toDoubleOrNull() ?: 0.0
        val url = intent.getStringExtra("url")
        val transactionReferenceID = intent.getStringExtra("transactionReferenceID")
        val token = intent.getStringExtra("token")
        val phone = intent.getStringExtra("phone")

        val formattedAmount = String.format("%.2f", donationAmount)

        binding.txtAmount.text = getString(R.string.amount) + formattedAmount + "/-"

        val donationInWords = convertNumberToWords(donationAmount)
        binding.txtWords.text = donationInWords


        val qrCodeBitmap = generateUPIQRCode(url!!)
        binding.imgQRCode.setImageBitmap(qrCodeBitmap)


        binding.btnCancel.setOnClickListener {
            startActivity(Intent(applicationContext,HomeActivity::class.java))
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
                .priority(Priority.HIGH))
            .into(binding.imgBackground)

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



    }
