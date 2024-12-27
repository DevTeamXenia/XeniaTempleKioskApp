@file:Suppress("DEPRECATION")

package com.xeniatechnologies.app.nimahal.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.xeniatechnologies.app.nimahal.R
import java.security.SecureRandom
import java.util.Locale

object Methods {

    private var loader: AlertDialog? = null

    @Suppress("DEPRECATION")
    fun setLocale(context: Context, languageCode: String?) {
        val locale = Locale(languageCode ?: "en")
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
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