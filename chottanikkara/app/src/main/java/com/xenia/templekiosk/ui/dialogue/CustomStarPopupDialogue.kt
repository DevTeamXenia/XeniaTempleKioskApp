package com.xenia.templekiosk.ui.dialogue

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xenia.templekiosk.R
import com.xenia.templekiosk.ui.adapter.NakshatraAdapter
import com.xenia.templekiosk.ui.screens.VazhipaduActivity
import org.koin.android.ext.android.inject

class CustomStarPopupDialogue : DialogFragment() {

    var onNakshatraSelected: ((String) -> Unit)? = null
    private val sharedPreferences: SharedPreferences by inject()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
                // Do nothing to disable back press
            }
        }.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.custome_star_dialogue, container, false)
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedLanguage = sharedPreferences.getString("SL", "en") ?: "en"

        val nakshatrasArrayRes = when (selectedLanguage) {
            "hi" -> R.array.nakshatras
            "ta" -> R.array.nakshatras
            "te" -> R.array.nakshatras
            "ml" -> R.array.nakshatras
            "kn" -> R.array.nakshatras
            else -> R.array.nakshatras
        }

        val translatedNakshatras = resources.getStringArray(nakshatrasArrayRes)

        val listStar = view.findViewById<RecyclerView>(R.id.list_star)
        listStar.layoutManager = GridLayoutManager(requireContext(), 4)
        listStar.adapter = NakshatraAdapter(translatedNakshatras) { selectedNakshatra ->
            onNakshatraSelected?.invoke(selectedNakshatra)
            dismiss()
        }
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.99).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? VazhipaduActivity)?.isDialogVisible = false
    }
}
