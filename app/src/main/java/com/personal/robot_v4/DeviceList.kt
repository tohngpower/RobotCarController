package com.personal.robot_v4

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class DeviceList : AppCompatActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var myBluetooth: BluetoothAdapter
    private lateinit var button: Button
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        button = findViewById(R.id.button)
        listView = findViewById(R.id.listView)

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        myBluetooth = bluetoothManager.adapter
        MyApp.already = false
        if (!myBluetooth.isEnabled) {
            //Ask to the user turn the bluetooth on
            val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(turnBTon)
        }

        button.setOnClickListener {
            pairedDevicesList() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MyApp.REQUEST_CODE_BLUETOOTH_SCAN -> {
                // Handle the result of the BLUETOOTH_SCAN permission request
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    // Proceed with Bluetooth operations
                    MyApp.instance?.msg("Bluetooth scan success!")
                } else {
                    // Permission denied
                    // Handle appropriately
                    MyApp.instance?.msg("Bluetooth scan fail!")
                }
            }
            MyApp.REQUEST_CODE_BLUETOOTH_CONNECT -> {
                // Handle the result of the BLUETOOTH_SCAN permission request
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    // Proceed with Bluetooth operations
                    MyApp.instance?.msg("Bluetooth connected!")
                } else {
                    // Permission denied
                    // Handle appropriately
                    MyApp.instance?.msg("Bluetooth failed to connect!")
                }
            }
            // Add other request code handling here if needed
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private val enableBluetoothLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth was enabled by the user
            // Handle the success case
            MyApp.instance?.msg("Bluetooth enabled!")
        } else {
            // Bluetooth was not enabled by the user
            // Handle the failure case
            MyApp.instance?.msg("Please enable bluetooth!")
        }
    }

    private fun pairedDevicesList() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                MyApp.REQUEST_CODE_BLUETOOTH_CONNECT
            )
            return
        } else {
            myBluetooth.bondedDevices
            val list = ArrayList<String>()
            MyApp.already = false
            if (myBluetooth.bondedDevices.isNotEmpty()) {
                for (bt in myBluetooth.bondedDevices) {
                    list.add(bt.name + "\n" + bt.address) //Get the device's name and the address
                }
            } else {
                MyApp.instance?.msg("No Paired Bluetooth Devices Found.")
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
            listView.adapter = adapter
            listView.onItemClickListener = myListClickListener //Method called when the device from the list is clicked
        }
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
                MyApp.already = false
                MyApp.instance?.msg("This device is not compatible with this app.")
            }
        }
    }

    companion object {
        var EXTRA_ADDRESS = "device_address"
    }
}