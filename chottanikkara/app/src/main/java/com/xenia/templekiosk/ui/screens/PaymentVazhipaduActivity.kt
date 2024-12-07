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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xenia.templekiosk.R
import com.xenia.templekiosk.data.network.model.CartItem
import com.xenia.templekiosk.databinding.ActivityPaymentBinding
import com.xenia.templekiosk.utils.SessionManager
import net.posprinter.IDeviceConnection
import net.posprinter.IPOSListener
import net.posprinter.POSConnect
import net.posprinter.POSConst
import net.posprinter.POSPrinter
import net.posprinter.model.PTable
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PaymentVazhipaduActivity : AppCompatActivity() {

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
private var allCartItems:String? = null

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
        allCartItems = intent.getStringExtra("allCartItems")

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

        if (!allCartItems.isNullOrEmpty()) {
            val gson = Gson()
            val itemType = object : TypeToken<List<CartItem>>() {}.type
            val allCartItemsList: List<CartItem> = gson.fromJson(allCartItems, itemType)

            // Use allCartItems as needed

        val cd = sessionManager.getCompanyDetails()
        if (cd != null) {
            val currentDate = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date())
            val drawableDevasam = ContextCompat.getDrawable(this, R.drawable.print_header_logo)
            val bitmapDevasam = (drawableDevasam as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val drawableDevi = ContextCompat.getDrawable(this, R.drawable.print_bottom_logo)
            val bitmapDevi = (drawableDevi as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
val header = ContextCompat.getDrawable(this, R.drawable.header)
            val headerBitmap = (header as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

           
            val vazhipadu = ContextCompat.getDrawable(this, R.drawable.vazhipadu_receipt)
            val vazhipaduBitmap = (vazhipadu as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val amountValue: Float = amount!!.toFloat()
//            POSPrinter(curConnect)
//                .printBitmap(bitmapDevasam, POSConst.ALIGNMENT_CENTER, 590)
//                .printText("Receipt No : $orderID\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
//                .printText("Date : $currentDate\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
//                .feedLine(1)
//                .printText("Received From : $name\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
//                .printText("Contact No : $phoneNo\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
//                .printText("Birth Star : $star\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
//                .feedLine(1)
//                .printText("E-Kanikka for : $devatha\n\n", POSConst.ALIGNMENT_RIGHT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
//                .printText("Amount Paid : ${String.format("%.2f", amountValue)}\n", POSConst.ALIGNMENT_RIGHT, POSConst.FNT_BOLD, POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT)
//                .printText("UPI Reference No: $transID\n\n", POSConst.ALIGNMENT_RIGHT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
//                .printBitmap(bitmapDevi, POSConst.ALIGNMENT_CENTER, 500)
//                .cutHalfAndFeed(1)
            val totalWidth = 54
            val spaces = totalWidth - (allCartItemsList[0].personName?.length?.plus(allCartItemsList[0].personStar?.length!!)!!)
            val paddedStar = allCartItemsList[0].personStar?.padStart(spaces)

            POSPrinter(curConnect).printBitmap(headerBitmap, POSConst.ALIGNMENT_CENTER, 590)
                .feedLine(1)
                .printBitmap(vazhipaduBitmap, POSConst.ALIGNMENT_CENTER, 590)
                .feedLine(1)
                .printText("Receipt No : $transID\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printText("Date : $currentDate\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .feedLine(1)
                .printText("${allCartItemsList[0].personName} ${paddedStar}\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL , POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            .feedLine(1)


            for (item in allCartItemsList){
                val totalWidthTable = 50

                val offeringName = item.offeringName.toString()
                val amount = item.amount.toString()
                val itemSpaces = totalWidthTable - (offeringName.length.plus(amount.length))
                val paddedAmount = amount.padStart(itemSpaces)

                POSPrinter(curConnect).printText("${item.offeringName}${paddedAmount}\n", POSConst.ALIGNMENT_LEFT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
            }
            POSPrinter(curConnect)
                .feedLine(1)
                .printText("Amount Paid : ${String.format("%.2f", amountValue)}\n", POSConst.ALIGNMENT_RIGHT, POSConst.FNT_BOLD, POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT)
                .printText("UPI Reference No: $transID\n\n", POSConst.ALIGNMENT_RIGHT, POSConst.STS_NORMAL, POSConst.TXT_1WIDTH or POSConst.TXT_1HEIGHT)
                .printBitmap(bitmapDevi, POSConst.ALIGNMENT_CENTER, 500)
                .cutHalfAndFeed(1)
        }

        redirect()
        }
    }

    private fun redirect(){
        Handler(mainLooper).postDelayed({
            startActivity(Intent(applicationContext, LanguageActivity::class.java))
            finish()
        }, 1000)
    }
}
