package com.personal.robot_v4

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class RobotController2 : AppCompatActivity() {
    private var btSocket: BluetoothSocket? = null
    private var address: String? = null
    private lateinit var buttonU: Button
    private lateinit var buttonD: Button
    private lateinit var buttonL: Button
    private lateinit var buttonR: Button
    private lateinit var buttonMN: Button
    private lateinit var buttonRE: Button
    private lateinit var btDis: Button
    private lateinit var statusText: TextView

    @Volatile
    var haveData = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i = intent
        address = i.getStringExtra(DeviceList.EXTRA_ADDRESS) //receive the address of the bluetooth device
        setContentView(R.layout.activity_robot_controller2)

        buttonU = findViewById(R.id.buttonU)
        buttonD = findViewById(R.id.buttonD)
        buttonL = findViewById(R.id.buttonL)
        buttonR = findViewById(R.id.buttonR)
        buttonMN = findViewById(R.id.buttonMN)
        buttonRE = findViewById(R.id.buttonRE)
        btDis = findViewById(R.id.btDis)
        statusText = findViewById(R.id.statusText)

        setupBluetoothConnection()

        //commands to be sent to bluetooth
        buttonU.setOnClickListener {
            moveForward() //method to move forward
        }
        buttonD.setOnClickListener {
            moveBackward() //method to reverse
        }
        buttonL.setOnClickListener {
            moveLeft() //method to turn left
        }
        buttonR.setOnClickListener {
            moveRight() //method to turn right
        }
        buttonMN.setOnClickListener {
            manualMove() //method to move manually
        }
        btDis.setOnClickListener {
            disconnect() //close connection
        }
        buttonRE.setOnClickListener {
            moveByRecord() //start moving by record
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java", ReplaceWith("moveTaskToBack(true)"))
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onPause() {
        super.onPause()
        eStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        eStop()
    }

    private fun disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket!!.outputStream.write("EMO".toByteArray())
                btSocket!!.close() //close connection
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
        finish() //return to the first layout
    }

    private fun moveForward() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("MF".toByteArray())
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            val text = "null"
            statusText.text = text
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun moveBackward() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("MB".toByteArray())
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            val text = "null"
            statusText.text = text
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun moveLeft() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("ML".toByteArray())
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            val text = "null"
            statusText.text = text
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun moveRight() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("MR".toByteArray())
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            val text = "null"
            statusText.text = text
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun manualMove() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("MN".toByteArray())
                val i = Intent(this@RobotController2, ManualMovement::class.java)
                startActivity(i)
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            val text = "null"
            statusText.text = text
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun eStop() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("EMO".toByteArray())
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Already stopped!", true)
        }
    }

    private fun checkReady() {
        haveData = true
        val readBuffer = ByteArray(1024)
        var bytes: Int
        while (haveData) {
            try {
                val available = btSocket!!.inputStream.available()
                if (available > 4) {
                    bytes = btSocket!!.inputStream.read(readBuffer)
                    if (bytes > 0) {
                        haveData = false
                    }
                }
            } catch (e: IOException) {
                MyApp.instance?.msg("Error, No data from Bluetooth module")
                haveData = false
            }
        }
    }

    private fun moveByRecord() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("REC".toByteArray())
                val i = Intent(this@RobotController2, MoveByRecord::class.java)
                startActivity(i)
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun setupBluetoothConnection() {
        MyApp.already = false
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // Request BLUETOOTH_SCAN permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    MyApp.REQUEST_CODE_BLUETOOTH_SCAN
                )
                return
            } else {
                // Get the BluetoothManager from the system's context
                val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

                // Retrieve the BluetoothAdapter from the BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter

                // Now you can use bluetoothAdapter for Bluetooth operations

                bluetoothAdapter.cancelDiscovery()
                btSocket = bluetoothAdapter.getRemoteDevice(address).createInsecureRfcommSocketToServiceRecord(
                    MyApp.myUUID
                ) //start connection
                btSocket?.connect()
                val text = "Connected!"
                statusText.text = text
                MyApp.btSocket = btSocket
            }
        } catch (e: IOException) {
            statusText.text = e.toString()
        }
    }
}