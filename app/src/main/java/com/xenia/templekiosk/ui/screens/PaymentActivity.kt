package com.xenia.templekiosk.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.xenia.templekiosk.R
import com.xenia.templekiosk.databinding.ActivityPaymentBinding
import net.posprinter.IDeviceConnection
import net.posprinter.IPOSListener
import net.posprinter.POSConnect
import net.posprinter.POSConst
import net.posprinter.POSPrinter


class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var curConnect: IDeviceConnection? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val status = intent.getStringExtra("status")
        val amount = intent.getStringExtra("amount")
        val transID = intent.getStringExtra("transID")
        val name = intent.getStringExtra("name")
        val star = intent.getStringExtra("star")


        if(status.equals("S")){
            binding.linSuccess.visibility = View.VISIBLE
            binding.linFailed.visibility = View.GONE
            binding.txtAmount.text = getString(R.string.amount)+" "+ amount
            binding.txtTransId.text = getString(R.string.transcation_id) +" "+ transID
            if(name!!.isNotEmpty()){
                binding.txtName.visibility = View.VISIBLE
                binding.txtName.text = getString(R.string.pay_name) +" "+ name
            }

            binding.txtStar.text = getString(R.string.star) +" "+ star
        }else{
            binding.linSuccess.visibility = View.GONE
            binding.linFailed.visibility = View.VISIBLE
        }


        configPrinter()


    }

    private fun configPrinter() {
        try {
            POSConnect.init(applicationContext)
            val entries = POSConnect.getUsbDevices(applicationContext)

            if (entries.isNotEmpty()) {
                try {
                    printReceipt(entries[0])
                } catch (e: Exception) {
                    Toast.makeText(this, "Error printing receipt: ${e.message}", Toast.LENGTH_LONG)
                        .show()

                }
            } else {
                Toast.makeText(this, "No USB printer devices found", Toast.LENGTH_LONG).show()
            }
        } catch (_: Exception) {

        }
    }




    private val connectListener = IPOSListener { code, _ ->
        when (code) {
            POSConnect.CONNECT_SUCCESS -> {
                initReceiptPrint()
            }

            POSConnect.CONNECT_FAIL -> {
                redirect()
            }

            POSConnect.CONNECT_INTERRUPT -> {
                redirect()

            }

            POSConnect.SEND_FAIL -> {
                redirect()

            }

            POSConnect.USB_DETACHED -> {
                redirect()

            }

            POSConnect.USB_ATTACHED -> {
                redirect()

            }
        }
    }

    private fun printReceipt(pathName: String) {
        connectUSB(pathName)
    }

    private fun connectUSB(pathName: String) {
        curConnect?.close()
        curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_USB)
        curConnect!!.connect(pathName, connectListener)
    }

    private fun initReceiptPrint() {
        POSPrinter(curConnect)
            .printText("DURGA TEMPLE\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_2WIDTH or POSConst.TXT_2HEIGHT)
            .printText("8400 Durga Place, Fairfax Station, VA 22039\nPhone:(703) 690-9355;website:www.durgatemple.org\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            .feedLine(1)
            .printText("e Kanika Receipt\n", POSConst.ALIGNMENT_CENTER, POSConst.FNT_BOLD, POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT)
            .feedLine(1)
            .printText("Receipt No : 27533365\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            .printText("Date : 24 - Sep - 2024 07: 50 AM\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            .feedLine(1)
            .printText("Received From : Surya Narayana Pillai\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            .printText("Contact No : +91 9037554466\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            .printText("Birth Star : Ardra\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            .feedLine(1)
            .printText("Amount Paid : 10000.00\n", POSConst.ALIGNMENT_RIGHT, POSConst.FNT_BOLD , POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT)
            .printText("UPI Reference No: 5647784112\n", POSConst.ALIGNMENT_RIGHT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            .feedLine(1)
            .printText("Thank you for Your Generosity\n", POSConst.ALIGNMENT_CENTER, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT)
            .printText("Powered by XeniaTechnologies\n", POSConst.ALIGNMENT_CENTER, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            .cutHalfAndFeed(1)

       // redirect()
    }

    private fun redirect(){
        Handler(mainLooper).postDelayed({
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
            finish()
        }, 1000)
    }
}
