package com.xenia.churchkiosk.ui.dialogue

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.xenia.churchkiosk.R
import com.xenia.churchkiosk.ui.screens.LanguageActivity

class CustomInactivityDialog(private val callback: InactivityCallback) : DialogFragment() {

    interface InactivityCallback {
        fun resetInactivityTimer()
    }

    private var countdownTimer: CountDownTimer? = null
    private lateinit var btnNo: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
            }
        }.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_inactivity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnNo = view.findViewById(R.id.btnNo)

        startCountdown()

        view.findViewById<Button>(R.id.btnNo).setOnClickListener {
            redirectToLanguageActivity()
        }

        view.findViewById<Button>(R.id.btnYes).setOnClickListener {
            callback.resetInactivityTimer()
            countdownTimer?.cancel()
            dismiss()
        }
    }

    private fun startCountdown() {
        val countdownTime = 10000L

        countdownTimer = object : CountDownTimer(countdownTime, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                btnNo.text = getString(R.string.no) +"("+ secondsRemaining +")"
            }
            override fun onFinish() {
                redirectToLanguageActivity()
            }
        }.start()
    }

    private fun redirectToLanguageActivity() {
        val intent = Intent(requireContext(), LanguageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.55).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownTimer?.cancel()
    }
}
