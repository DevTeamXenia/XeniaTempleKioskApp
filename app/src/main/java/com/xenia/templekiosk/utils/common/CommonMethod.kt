package com.xenia.templekiosk.utils.common

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.xenia.templekiosk.R
import java.security.SecureRandom

object CommonMethod {
    private var loader: AlertDialog? = null

    fun showLoader(context: Context, message: String) {
        if (context is AppCompatActivity && context.isFinishing) {
            return
        }

        val builder = AlertDialog.Builder(context, R.style.TransparentAlertDialog)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.custom_loader, null)

        view.findViewById<TextView>(R.id.loaderMessage).text = message

        builder.setView(view)
        builder.setCancelable(false)

        loader = builder.create()
        loader?.show()
    }

    fun dismissLoader() {
        loader?.dismiss()
        loader = null
    }

    fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }




    fun generateNumericTransactionReferenceID(): String {
        val secureRandom = SecureRandom()
        val numberStringBuilder = StringBuilder(10)

        numberStringBuilder.append(secureRandom.nextInt(9) + 1)

        for (i in 1 until 10) {
            numberStringBuilder.append(secureRandom.nextInt(10))
        }

        return numberStringBuilder.toString()
    }

}