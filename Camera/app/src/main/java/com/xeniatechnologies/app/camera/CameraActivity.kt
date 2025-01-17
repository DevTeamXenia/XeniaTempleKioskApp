package com.xeniatechnologies.app.camera

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Build

class CameraActivity : AppCompatActivity() {
    private lateinit var usbManager: UsbManager
    private val ACTION_USB_PERMISSION = "com.xeniatechnologies.app.camera.USB_PERMISSION"

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
                            Log.d("USB", "Permission granted for device: ${it.deviceName}")
                            // Now you can open the device
                            connectToUsbDevice(it)
                        }
                    } else {
                        Log.d("USB", "Permission denied for device: ${device?.deviceName}")
                        // Show permission denied dialog
                        showPermissionDeniedDialog()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Initialize USB Manager
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        // Register for USB permission broadcast
        val filter = IntentFilter(ACTION_USB_PERMISSION).apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        registerReceiver(usbReceiver, filter)

        // Check for already connected USB devices
        checkForConnectedDevices()
    }

    private fun checkForConnectedDevices() {
        val deviceList = usbManager.deviceList
        for (device in deviceList.values) {
            // Check if it's a camera (USB device class 14 is Video)
            if (isUsbCamera(device)) {
                requestUsbPermission(device)
                break
            }
        }
    }

    private fun isUsbCamera(device: UsbDevice): Boolean {
        return device.deviceClass == UsbConstants.USB_CLASS_VIDEO ||
                (device.interfaceCount > 0 &&
                        device.getInterface(0).interfaceClass == UsbConstants.USB_CLASS_VIDEO)
    }

    private fun requestUsbPermission(device: UsbDevice) {
        if (!usbManager.hasPermission(device)) {
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_IMMUTABLE
            )

            // Show dialog before requesting permission
            AlertDialog.Builder(this)
                .setTitle("USB Camera Permission")
                .setMessage("This app needs permission to access the USB camera. Please allow when prompted.")
                .setPositiveButton("OK") { _, _ ->
                    usbManager.requestPermission(device, permissionIntent)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(this, "Camera access cancelled", Toast.LENGTH_SHORT).show()
                }
                .setCancelable(false)  // Prevent user from dismissing without responding
                .show()
        } else {
            // Already has permission
            connectToUsbDevice(device)
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("USB Camera access was denied. The app needs this permission to function. Would you like to try again?")
            .setPositiveButton("Retry") { _, _ ->
                // Request permission again
                checkForConnectedDevices()
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(this, "Camera access denied", Toast.LENGTH_LONG).show()
                finish()  // Close the activity if permission is denied
            }
            .setCancelable(false)  // Prevent accidental dismissal
            .show()
    }

    private fun connectToUsbDevice(device: UsbDevice) {
        try {
            val connection = usbManager.openDevice(device)
            if (connection != null) {
                Log.d("USB", "Successfully connected to device: ${device.deviceName}")
                Toast.makeText(this, "USB Camera connected successfully", Toast.LENGTH_SHORT).show()
                // Continue with your camera setup here
                setupCamera(device, connection)
            } else {
                Log.e("USB", "Failed to open device connection")
                Toast.makeText(this, "Failed to connect to USB camera", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("USB", "Error connecting to device: ${e.message}")
            Toast.makeText(this, "Error connecting to USB camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCamera(device: UsbDevice, connection: UsbDeviceConnection) {
        // Your camera setup code here
        // This is where you will configure the camera (e.g., open streams, set up surfaces, etc.)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            Log.e("USB", "Error unregistering receiver: ${e.message}")
        }
    }
}
