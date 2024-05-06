package com.personal.robot_v4

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance: MyApp? = null
            private set

        var btSocket: BluetoothSocket? = null
        var address = ""
        private val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val REQUEST_CODE_BLUETOOTH_SCAN = 1011 // Choose a unique integer
        const val REQUEST_CODE_BLUETOOTH_CONNECT = 1012 // Choose a unique integer

        fun setupBluetoothConnection(context: Context, activity: Activity, address: String): String {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // Request BLUETOOTH_SCAN permission
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    REQUEST_CODE_BLUETOOTH_SCAN
                )
                val text = "Please enable Bluetooth permission!"
                return text
            } else {
                if(btSocket == null) {
                    // Get the BluetoothManager from the system's context
                    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

                    // Retrieve the BluetoothAdapter from the BluetoothManager
                    val bluetoothAdapter = bluetoothManager.adapter

                    try {// Now you can use bluetoothAdapter for Bluetooth operations
                        btSocket = bluetoothAdapter.getRemoteDevice(address).createInsecureRfcommSocketToServiceRecord(
                            myUUID
                        )
                        try {
                            btSocket?.connect()
                            return if(btSocket!!.isConnected) {
                                "Connected."
                            } else {
                                "Failed to connect."
                            }
                        } catch (e: IOException) {
                            return context.getString(R.string.connection_error, e.message)
                        }
                    } catch (e: Exception) {
                        return "Please turn on Bluetooth: $e"
                    } //start connection
                } else {
                    if(btSocket!!.isConnected) {
                        val text = "Connected."
                        return text
                    } else {
                        try {
                            btSocket?.connect()
                            return  if(btSocket!!.isConnected) {
                                "Connected."
                            } else {
                                "Failed to connect."
                            }
                        } catch (e: IOException) {
                            return context.getString(R.string.connection_error, e.message)
                        }
                    }
                }
            }
        }
    }
}
