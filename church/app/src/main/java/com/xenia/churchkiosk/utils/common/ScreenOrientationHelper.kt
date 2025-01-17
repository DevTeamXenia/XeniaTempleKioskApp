package com.xenia.churchkiosk.utils.common

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

object ScreenOrientationHelper {

    fun checkScreenOnAppOpen(activity: AppCompatActivity): Boolean {
        val screenSize = activity.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

        return when {
            screenSize <= Configuration.SCREENLAYOUT_SIZE_NORMAL -> {
                Toast.makeText(activity, "This app is not compatible with this screen size.", Toast.LENGTH_LONG).show()
                activity.finish()
                false
            }
            else -> {
                // For larger screens, force landscape orientation
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                true
            }
        }
    }

    fun setScreenOrientation(context: Context) {
        val screenSize =
            context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val activity = context as? AppCompatActivity

        when {
            screenSize <= Configuration.SCREENLAYOUT_SIZE_NORMAL -> {
                Toast.makeText(
                    context,
                    "This app is not compatible with this screen size.",
                    Toast.LENGTH_LONG
                ).show()
                activity?.finish()
            }

            else -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }
}