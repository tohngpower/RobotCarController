package com.robot_v3

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.widget.Toast
import java.io.IOException
import java.util.*

class MyApp : Application() {
    var connectBT = true
    private var address: String? = null
    var btSocket: BluetoothSocket? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun setupBluetoothConnection(bt_address: String?) {
        address = bt_address
        alreadyshow = false
        connectBT = try {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
            btSocket = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address).createInsecureRfcommSocketToServiceRecord(myUUID) //start connection
            btSocket?.connect()
            msg("Connected.")
            true
        } catch (e: IOException) {
            msg("Connection Failed. Is it a SPP Bluetooth? Try again.")
            false
        }
    }

    fun msg(s: String?) {
        if (!alreadyshow) {
            toast = Toast.makeText(applicationContext, s, Toast.LENGTH_LONG)
            toast?.show()
            alreadyshow = true
        } else {
            toast!!.cancel()
            alreadyshow = false
        }
    }

    fun msg(s: String?, sh: Boolean) {
        if (sh) {
            if (!alreadyshow) {
                toast = Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT)
                toast?.show()
                alreadyshow = true
            } else {
                toast!!.cancel()
                alreadyshow = false
            }
        } else {
            if (!alreadyshow) {
                toast = Toast.makeText(applicationContext, s, Toast.LENGTH_LONG)
                toast?.show()
                alreadyshow = true
            } else {
                toast!!.cancel()
                alreadyshow = false
            }
        }
    }

    companion object {
        var instance: MyApp? = null
            private set

        var toast: Toast? = null
        var alreadyshow = false
        val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}