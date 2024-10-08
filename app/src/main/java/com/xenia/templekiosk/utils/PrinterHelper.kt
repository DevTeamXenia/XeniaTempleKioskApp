package com.xenia.templekiosk.utils

import net.posprinter.IDeviceConnection
import net.posprinter.IPOSListener
import net.posprinter.POSConnect
import net.posprinter.POSConst
import net.posprinter.POSPrinter

class PrinterHelper {
    private var curConnect: IDeviceConnection? = null


    private val connectListener = IPOSListener { code, msg ->
        when (code) {
            POSConnect.CONNECT_SUCCESS -> {
                initReceiptPrint()
            }

            POSConnect.CONNECT_FAIL -> {
            }

            POSConnect.CONNECT_INTERRUPT -> {

            }

            POSConnect.SEND_FAIL -> {

            }

            POSConnect.USB_DETACHED -> {

            }

            POSConnect.USB_ATTACHED -> {

            }
        }
    }

    fun printReceipt(pathName: String) {
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
    }

}