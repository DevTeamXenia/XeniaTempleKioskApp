package com.xenia.templekiosk.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.xenia.templekiosk.R
import com.xenia.templekiosk.databinding.ActivityPaymentBinding
import com.xenia.templekiosk.utils.SessionManager
import net.posprinter.IDeviceConnection
import net.posprinter.IPOSListener
import net.posprinter.POSConnect
import net.posprinter.POSConst
import net.posprinter.POSPrinter
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val sessionManager: SessionManager by inject()
    private var curConnect: IDeviceConnection? = null

    private var status: String? = null
    private var amount: String? = null  
    private var transID: String? = null
    private var name: String? = null
    private var star: String? = null
    private var devatha: String? = null
    private var orderID: String? = null
    private var phoneNo: String? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

         status = intent.getStringExtra("status")
         amount = intent.getStringExtra("amount")
         transID = intent.getStringExtra("transID")
         name = intent.getStringExtra("name")
         star = intent.getStringExtra("star")
         devatha = intent.getStringExtra("devatha")
         orderID = intent.getStringExtra("orderID")
         phoneNo = intent.getStringExtra("phno")


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
            configPrinter()
        }else{
            binding.linSuccess.visibility = View.GONE
            binding.linFailed.visibility = View.VISIBLE
            redirect()
        }

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
                redirect()
                Toast.makeText(this, "No USB printer devices found", Toast.LENGTH_LONG).show()
            }
        } catch (_: Exception) {
            redirect()

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

    @SuppressLint("DefaultLocale")
    private fun initReceiptPrint() {
        val cd = sessionManager.getCompanyDetails()
        if (cd != null) {
            val currentDate = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date())
            val drawableDevasam = ContextCompat.getDrawable(
                this@PaymentActivity,
                R.drawable.print_header_logo
            ) as BitmapDrawable
            val drawableDevi = ContextCompat.getDrawable(
                this@PaymentActivity,
                R.drawable.print_bottom_logo
            ) as BitmapDrawable

            val bitmapDevasam = drawableDevasam.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val compressedBitmapDevasam =
                Bitmap.createScaledBitmap(bitmapDevasam, 550, 200, true)

            val bitmapDevi = drawableDevi.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val compressedBitmapDevi = Bitmap.createScaledBitmap(bitmapDevi, 500, 100, true)
            val amountValue: Float = amount!!.toFloat()

            POSPrinter(curConnect)
                .printBitmap(compressedBitmapDevasam, POSConst.ALIGNMENT_CENTER, 500)
                .feedLine(1)
                .printText("Receipt No : $orderID\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printText("Date : $currentDate\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .feedLine(1)
                .printText("Received From : $name\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printText("Contact No : $phoneNo\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printText("Birth Star : $star\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .feedLine(1)
                .printText("E-Kanikka for : $devatha\n\n", POSConst.ALIGNMENT_RIGHT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printText("Amount Paid : ${String.format("%.2f", amountValue)}\n", POSConst.ALIGNMENT_RIGHT, POSConst.FNT_BOLD, POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT)
                .printText("UPI Reference No: $transID\n\n", POSConst.ALIGNMENT_RIGHT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printBitmap(compressedBitmapDevi, POSConst.ALIGNMENT_CENTER, 500)
                .cutHalfAndFeed(1)
        }

        redirect()
    }

    private fun redirect(){
        Handler(mainLooper).postDelayed({
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
            finish()
        }, 1000)
    }
}
