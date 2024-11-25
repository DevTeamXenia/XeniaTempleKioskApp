package com.xeniatechnologies.app.templekiosktirupati.utils

import android.content.Context
import net.posprinter.IDeviceConnection
import net.posprinter.IPOSListener
import net.posprinter.POSConnect
import net.posprinter.POSConst

object PrinterConnectionManager {
    private var printerConnection: IDeviceConnection? = null
    private var isConnected = false
    private var lastConnectedPath: String? = null

    fun initialize(context: Context) {
        try {
            POSConnect.init(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    fun getPrinterConnection(context: Context, onConnectionResult: (Boolean) -> Unit): IDeviceConnection? {
        if (isConnected && printerConnection != null) {
            onConnectionResult(true)
            return printerConnection
        }

        val entries = POSConnect.getUsbDevices(context)
        if (entries.isEmpty()) {
            onConnectionResult(false)
            return null
        }

        connectToPrinter(entries[0]) { success ->
            isConnected = success
            onConnectionResult(success)
        }

        return printerConnection
    }

    private fun connectToPrinter(pathName: String, onResult: (Boolean) -> Unit) {
        try {
            if (printerConnection != null && lastConnectedPath == pathName) {
                onResult(true)
                return
            }

            printerConnection?.close()
            printerConnection = POSConnect.createDevice(POSConnect.DEVICE_TYPE_USB)
            lastConnectedPath = pathName

            printerConnection!!.connect(pathName, object : IPOSListener {
                override fun onStatus(status: Int, tag: String?) {
                    when (status) {
                        POSConnect.CONNECT_SUCCESS -> {
                            isConnected = true
                            onResult(true)
                        }
                        POSConnect.CONNECT_FAIL -> {
                            isConnected = false
                            onResult(false)
                        }
                        POSConnect.USB_DETACHED -> {
                            isConnected = false
                            lastConnectedPath = null
                            onResult(false)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            isConnected = false
            onResult(false)
        }
    }


}