package com.xenia.templekiosk.data.common

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
    fun showQRPayPopup(context: Context, upiId: String, amount: String, screen: String) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_popup_pay, null)

        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setGravity(Gravity.CENTER)

        val amountTextView = dialogView.findViewById<TextView>(R.id.txt_amount)
        amountTextView.text = "Amount ₹ $amount/-"

        // Generate QR code with UPI payment link
        val qrCodeImageView = dialogView.findViewById<ImageView>(R.id.qrCodeImageView)
        val qrCodeBitmap = generateUPIQRCode(upiId, amount)
        qrCodeImageView.setImageBitmap(qrCodeBitmap)

        val fullScreen = dialogView.findViewById<LinearLayout>(R.id.pop_full_screen)
        fullScreen.setOnClickListener {
            val intent = Intent(context, PaymentActivity::class.java)
            intent.putExtra("ScreenType", screen )
            context.startActivity(intent)
        }

        val btnHome = dialogView.findViewById<ImageView>(R.id.btnHome)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClose)

        btnHome.setOnClickListener {
            context.startActivity(Intent(context,LanguageActivity::class.java))
        }

        btnClose.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun generateUPIQRCode(upiId: String, amount: String): Bitmap? {
        val qrText = "upi://pay?pa=$upiId&pn=Recipient%20Name&am=$amount&cu=INR"
        try {
            val bitMatrix = MultiFormatWriter().encode(
                qrText,
                BarcodeFormat.QR_CODE,
                300,
                300
            )
            val barcodeEncoder = BarcodeEncoder()
            return barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }
}