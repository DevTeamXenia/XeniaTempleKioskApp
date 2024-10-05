package com.xenia.templekiosk.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.xenia.templekiosk.R
import com.xenia.templekiosk.ui.LanguageActivity
import com.xenia.templekiosk.ui.PaymentActivity

object Screen {
    var VazhipaduAreaScreen: String = "Vazhipadu"
    var DonationAreaScreen: String = "Donation"
}

object DialogUtils {
    fun showQRPayPopup(context: Context, url: String, amount: String, screen: String) {
        val dialogBuilder = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_popup_pay, null)

        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setGravity(Gravity.CENTER)

        // Set amount to the TextView
        val amountTextView = dialogView.findViewById<TextView>(R.id.txt_amount)
        amountTextView.text = "Amount ₹ $amount/-"

        // Generate and set the QR code to ImageView
        val qrCodeImageView = dialogView.findViewById<ImageView>(R.id.qrCodeImageView)
        val qrCodeBitmap = generateUPIQRCode(url)
        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap)
        } else {
            qrCodeImageView.setImageResource(R.drawable.ic_error_qr)
        }

        val fullScreen = dialogView.findViewById<LinearLayout>(R.id.pop_full_screen)
        fullScreen.setOnClickListener {
            val intent = Intent(context, PaymentActivity::class.java)
            intent.putExtra("ScreenType", screen)
            context.startActivity(intent)
        }

        // Home button click listener
        val btnHome = dialogView.findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            context.startActivity(Intent(context, LanguageActivity::class.java))
        }

        // Close button click listener
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
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