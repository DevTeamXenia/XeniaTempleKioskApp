package com.xenia.churchkiosk.utils

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.xenia.churchkiosk.ui.dialogue.CustomInactivityDialog
import com.xenia.churchkiosk.ui.dialogue.CustomQRPopupDialogue


class InactivityHandler(
    private val activity: AppCompatActivity,
    private val fragmentManager: FragmentManager,
    private val inactivityDialog: CustomInactivityDialog,
    private val qrPopupDialogue: CustomQRPopupDialogue
) {
    private val inactivityRunnable = Runnable {
        if (!activity.isFinishing && !activity.isDestroyed) {
            showDialogSafely()
        }
    }

    private val inactivityTimeout = 30000L
    private val timer: Handler = Handler(Looper.getMainLooper())
    private var isInactivityCheckPaused = false

    init {
        resetTimer()
    }

    fun resetTimer() {
        if (!isInactivityCheckPaused) {
            timer.removeCallbacks(inactivityRunnable)
            timer.postDelayed(inactivityRunnable, inactivityTimeout)
        }
    }

    fun pauseInactivityCheck() {
        isInactivityCheckPaused = true
        timer.removeCallbacks(inactivityRunnable)
    }

    fun resumeInactivityCheck() {
        isInactivityCheckPaused = false
        resetTimer()
    }

    fun cleanup() {
        timer.removeCallbacks(inactivityRunnable)
    }

    fun showDialogSafely() {
        if (qrPopupDialogue.isDialogShowing()) {
            return
        }

        val ft = fragmentManager.beginTransaction()
        val prevDialog = fragmentManager.findFragmentByTag("inactivity_dialog")
        if (prevDialog != null) {
            ft.remove(prevDialog)
        }
        ft.add(inactivityDialog, "inactivity_dialog")
        ft.commitAllowingStateLoss()
    }
}
