package com.personal.robot_v4

import android.app.Application
import android.bluetooth.BluetoothSocket
import android.widget.Toast
import java.util.*

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun msg(s: String?) {
        if (!already) {
            toast = Toast.makeText(applicationContext, s, Toast.LENGTH_LONG)
            toast?.show()
            already = true
        } else {
            toast!!.cancel()
            already = false
        }
    }

    fun msg(s: String?, sh: Boolean) {
        if (sh) {
            if (!already) {
                toast = Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT)
                toast?.show()
                already = true
            } else {
                toast!!.cancel()
                already = false
            }
        } else {
            if (!already) {
                toast = Toast.makeText(applicationContext, s, Toast.LENGTH_LONG)
                toast?.show()
                already = true
            } else {
                toast!!.cancel()
                already = false
            }
        }
    }

    companion object {
        var instance: MyApp? = null
            private set

        var btSocket: BluetoothSocket? = null
        var toast: Toast? = null
        var already = false
        val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val REQUEST_CODE_BLUETOOTH_SCAN = 1011 // Choose a unique integer
        const val REQUEST_CODE_BLUETOOTH_CONNECT = 1012 // Choose a unique integer
    }
}