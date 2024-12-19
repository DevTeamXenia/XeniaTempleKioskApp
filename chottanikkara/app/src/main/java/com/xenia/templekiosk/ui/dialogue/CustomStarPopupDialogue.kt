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
import java.util.Locale

@Suppress("DEPRECATION")
class CustomStarPopupDialogue : DialogFragment() {
    var onNakshatraSelected: ((String, String) -> Unit)? = null
    private val sharedPreferences: SharedPreferences by inject()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
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

        val englishNakshatras = getArrayForLocale("en", R.array.nakshatras)
        val translatedNakshatras = getArrayForLocale(selectedLanguage, R.array.nakshatras)

        val listStar = view.findViewById<RecyclerView>(R.id.list_star)
        listStar.layoutManager = GridLayoutManager(requireContext(), 4)
        listStar.adapter = NakshatraAdapter(translatedNakshatras) { selectedTranslatedNakshatra ->
            val index = translatedNakshatras.indexOf(selectedTranslatedNakshatra)
            val selectedEnglishNakshatra = englishNakshatras[index]
            onNakshatraSelected?.invoke(selectedTranslatedNakshatra, selectedEnglishNakshatra)
            dismiss()
        }
    }

    private fun getArrayForLocale(language: String, arrayResId: Int): Array<String> {
        val config = resources.configuration
        val originalLocale = config.locale
        val newConfig = config.apply { setLocale(Locale(language)) }
        val context = requireContext().createConfigurationContext(newConfig)
        val localizedArray = context.resources.getStringArray(arrayResId)
        config.setLocale(originalLocale)
        return localizedArray
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
