@file:Suppress("DEPRECATION")

package com.xeniatechnologies.app.templekiosktirupati.utils.common

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.xeniatechnologies.app.templekiosktirupati.R
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
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        val params = snackbarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER
        snackbarView.layoutParams = params

        snackbar.show()
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



    @SuppressLint("ObsoleteSdkInt")
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected ?: false
        }
    }


    fun convertNumberToWords(number: Double): String {
        val wholeNumber = number.toLong()
        val fraction = (number - wholeNumber) * 100

        return if (fraction == 0.0) {
            convertWholeNumberToWords(wholeNumber) + " Rupees Only"
        } else {
            "${convertWholeNumberToWords(wholeNumber)} and ${fraction.toInt()} Paise"
        }
    }

    private fun convertWholeNumberToWords(number: Long): String {
        val units = arrayOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
            "Eighteen", "Nineteen"
        )

        val tens = arrayOf(
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        )

        if (number == 0L) return "Zero"

        var num = number
        var words = ""
        if (num >= 1000) {
            words += convertWholeNumberToWords(num / 1000) + " Thousand "
            num %= 1000
        }
        if (num >= 100) {
            words += convertWholeNumberToWords(num / 100) + " Hundred "
            num %= 100
        }
        if (num >= 20) {
            words += tens[(num / 10).toInt()] + " "
            num %= 10
        }
        if (num > 0) {
            words += units[num.toInt()]
        }

        return words.trim()
    }


}