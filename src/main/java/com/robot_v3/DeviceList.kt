package com.robot_v3

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_device_list.*
import java.util.*

class DeviceList : AppCompatActivity() {
    private var myBluetooth: BluetoothAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        myBluetooth = BluetoothAdapter.getDefaultAdapter()
        MyApp.alreadyshow = false
        if (myBluetooth == null) {
            MyApp.instance?.msg("Bluetooth Device Not Available")
            finish()
        } else if (!myBluetooth!!.isEnabled) {
            //Ask to the user turn the bluetooth on
            val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnBTon, 1)
        }

        button.setOnClickListener {
            pairedDevicesList() }
    }

    private fun pairedDevicesList() {
        myBluetooth!!.bondedDevices
        val list = ArrayList<String>()
        MyApp.alreadyshow = false
        if (myBluetooth!!.bondedDevices.isNotEmpty()) {
            for (bt in myBluetooth!!.bondedDevices) {
                list.add(bt.name + "\n" + bt.address) //Get the device's name and the address
            }
        } else {
            MyApp.instance?.msg("No Paired Bluetooth Devices Found.")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        listView!!.adapter = adapter
        listView!!.onItemClickListener = myListClickListener //Method called when the device from the list is clicked
    }

    private val myListClickListener = OnItemClickListener { _, v, _, _ -> // Get the device MAC address, the last 17 chars in the View
        val info = (v as TextView).text.toString()
        // Make an intent to start next activity.
        when(val address = info.substring(info.length - 17)) {
            "00:18:91:D8:17:30" -> {
                val i = Intent(this@DeviceList, RobotController::class.java)
                //Change the activity.
                i.putExtra(EXTRA_ADDRESS, address) //this will be received at Robot Controller (class) Activity
                startActivity(i)
            }
            "00:20:08:00:14:55" -> {
                val i = Intent(this@DeviceList, RobotController2::class.java)
                //Change the activity.
                i.putExtra(EXTRA_ADDRESS, address) //this will be received at Robot Controller (class) Activity
                startActivity(i)
            }
            else -> {
                MyApp.alreadyshow = false
                MyApp.instance?.msg("This device is not compatible with this app.")
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        var EXTRA_ADDRESS = "device_address"
    }
}