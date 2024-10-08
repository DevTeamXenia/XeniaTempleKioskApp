package com.xenia.templekiosk.ui.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.databinding.ActivityPaymentBinding
import com.xenia.templekiosk.utils.PrinterHelper
import net.posprinter.POSConnect
import org.koin.android.ext.android.inject

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val printerHelper: PrinterHelper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

      /*  val status = intent.getStringExtra("status")
        val amount = intent.getStringExtra("amount")
        val transID = intent.getStringExtra("transID")

        binding.txtAmount.text = "Amount : $amount"
        binding.txtTransId.text = "Transcation ID : $transID"*/

        POSConnect.init(applicationContext)
        val entries = POSConnect.getUsbDevices(applicationContext)

        printerHelper.printReceipt(entries[0])




    }
}