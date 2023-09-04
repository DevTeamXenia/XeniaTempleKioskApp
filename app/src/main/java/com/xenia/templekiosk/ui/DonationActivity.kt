package com.xenia.templekiosk.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.common.DialogUtils
import com.xenia.templekiosk.data.common.Screen

class DonationActivity : AppCompatActivity() {

    private lateinit var payNow: Button
    private lateinit var btnSummary :Button
    private lateinit var btnHome: RelativeLayout
    private lateinit var btnLanguage: RelativeLayout
    private lateinit var editDonation: EditText
    private lateinit var textViewList: MutableList<TextView>
    private var selectedTextView: TextView? = null
    private val normalBackgroundResource = R.drawable.textview_board
    private val selectedBackgroundResource = R.drawable.textview_selected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

        initUI()


        for (textView in textViewList) {
            textView.setOnClickListener {
                handleTextViewSelection(textView)
            }
        }

        btnHome.setOnClickListener {
            startActivity(Intent(applicationContext, HomeActivity::class.java))
        }

        btnLanguage.setOnClickListener {
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
        }

        payNow.setOnClickListener {
            if (editDonation.text.toString().isEmpty()){
                Toast.makeText(applicationContext,"Please enter Amount",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            DialogUtils.showQRPayPopup(this,"maheshmohan7319@okaxis",editDonation.text.toString(),Screen.DonationAreaScreen )
        }
    }

    private fun initUI() {
        editDonation = findViewById(R.id.edit_txt_donation)
        editDonation.requestFocus()
        payNow = findViewById(R.id.btn_pay)
        textViewList = mutableListOf()
        textViewList.add(findViewById(R.id.Ashwini))
        textViewList.add(findViewById(R.id.Bharani))
        textViewList.add(findViewById(R.id.Krittika))
        textViewList.add(findViewById(R.id.Rohini))
        textViewList.add(findViewById(R.id.Mrigashirsha))
        textViewList.add(findViewById(R.id.Ardra))
        textViewList.add(findViewById(R.id.Punarvasu))
        textViewList.add(findViewById(R.id.Pushya))
        textViewList.add(findViewById(R.id.Ashlesha))
        textViewList.add(findViewById(R.id.Magha))
        textViewList.add(findViewById(R.id.PurvaPhalguni))
        textViewList.add(findViewById(R.id.UttaraPhalguni))
        textViewList.add(findViewById(R.id.Hasta))
        textViewList.add(findViewById(R.id.Chitra))
        textViewList.add(findViewById(R.id.Swati))
        textViewList.add(findViewById(R.id.Vishaka))
        textViewList.add(findViewById(R.id.Anuradha))
        textViewList.add(findViewById(R.id.Jyeshta))
        textViewList.add(findViewById(R.id.Moola))
        textViewList.add(findViewById(R.id.PurvaAshadha))
        textViewList.add(findViewById(R.id.UttaraAshada))
        textViewList.add(findViewById(R.id.Shravana))
        textViewList.add(findViewById(R.id.Dhanistha))
        textViewList.add(findViewById(R.id.Shatabhisaa))
        textViewList.add(findViewById(R.id.PurvaBhadrapada))
        textViewList.add(findViewById(R.id.UttaraBhadrapada))
        textViewList.add(findViewById(R.id.Revati))

        btnHome = findViewById(R.id.left_home)
        btnLanguage = findViewById(R.id.left_language)


    }

    private fun handleTextViewSelection(textView: TextView) {
        selectedTextView?.setBackgroundResource(normalBackgroundResource)
        selectedTextView = textView
        selectedTextView?.setBackgroundResource(selectedBackgroundResource)
    }


}
