package com.personal.robot_v4

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.ArrayList

class RobotController : AppCompatActivity() {
    private var address = ""
    private lateinit var listView: Spinner
    private lateinit var buttonU: Button
    private lateinit var buttonD: Button
    private lateinit var buttonL: Button
    private lateinit var buttonR: Button
    private lateinit var buttonO: Button
    private lateinit var buttonLI: Button
    private lateinit var buttonS: Button
    private lateinit var button5: Button
    private lateinit var button20: Button
    private lateinit var buttonMN: Button
    private lateinit var buttonZI: Button
    private lateinit var buttonDM: Button
    private lateinit var buttonRE: Button
    private lateinit var btDis: Button
    private lateinit var statusText: TextView
    private lateinit var autoText: TextView
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var myBluetooth: BluetoothAdapter
    private lateinit var dos: DataOutputStream
    private lateinit var dis: DataInputStream
    private var busy = false
    private var permissionDeny = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_robot_controller)

        listView = findViewById(R.id.listView)
        buttonU = findViewById(R.id.buttonU)
        buttonD = findViewById(R.id.buttonD)
        buttonL = findViewById(R.id.buttonL)
        buttonR = findViewById(R.id.buttonR)
        buttonO = findViewById(R.id.buttonO)
        buttonLI = findViewById(R.id.buttonLI)
        buttonS = findViewById(R.id.buttonS)
        button5 = findViewById(R.id.button5)
        button20 = findViewById(R.id.button20)
        buttonMN = findViewById(R.id.buttonMN)
        buttonZI = findViewById(R.id.buttonZI)
        buttonDM = findViewById(R.id.buttonDM)
        buttonRE = findViewById(R.id.buttonRE)
        btDis = findViewById(R.id.btDis)
        statusText = findViewById(R.id.statusText)
        autoText = findViewById(R.id.textView4)

        address = MyApp.address
        disableButton()
        btDis.isEnabled = true
        buttonS.isEnabled = false
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        myBluetooth = bluetoothManager.adapter
        pairedDevicesList()
        checkStatus()

        listView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val info = parentView?.getItemAtPosition(position).toString()
                address = info.substring(info.length - 17)
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing
            }
        }
        //commands to be sent to bluetooth
        //move forward
        buttonU.setOnClickListener {
            disableButton()
            writeByte("MF")
            checkReady()
        }
        //move backward
        buttonD.setOnClickListener {
            disableButton()
            writeByte("MB")
            checkReady()
        }
        //turn left
        buttonL.setOnClickListener {
            disableButton()
            writeByte("ML")
            checkReady()
        }
        //turn right
        buttonR.setOnClickListener {
            disableButton()
            writeByte("MR")
            checkReady()
        }
        //move 5 steps
        button5.setOnClickListener {
            disableButton()
            writeByte("M05")
            checkReady()
        }
        //move 20 steps
        button20.setOnClickListener {
            disableButton()
            writeByte("M20")
            checkReady()
        }
        //mode object evasion
        buttonO.setOnClickListener {
            busy = true
            disableButton()
            buttonS.isEnabled = true
            writeByte("OE")
        }
        //mode line tracking
        buttonLI.setOnClickListener {
            busy = true
            disableButton()
            buttonS.isEnabled = true
            writeByte("LINE")
        }
        //mode move zigzag
        buttonZI.setOnClickListener {
            busy = true
            disableButton()
            buttonS.isEnabled = true
            writeByte("ZI")
        }
        //manual mode
        buttonMN.setOnClickListener {
            disableButton()
            writeByte("MN")
            val i = Intent(this@RobotController, ManualMovement::class.java)
            startActivity(i)
            enableButton()
        }
        //distance measurement
        buttonDM.setOnClickListener {
            disableButton()
            writeByte("DM")
            val i = Intent(this@RobotController, DistanceMeasurement::class.java)
            startActivity(i)
            enableButton()
        }
        //move by record
        buttonRE.setOnClickListener {
            disableButton()
            writeByte("REC")
            val i = Intent(this@RobotController, MoveByRecord::class.java)
            startActivity(i)
            enableButton()
        }
        //stop
        buttonS.setOnClickListener {
            eStop()
        }
        //disconnect or connect
        btDis.setOnClickListener {
            disableButton()
            disconnect()
        }
    }

    private fun checkStatus() {
        if(MyApp.btSocket != null) {
            if(MyApp.btSocket!!.isConnected) {
                statusText.text = when(address) {
                    "00:18:91:D8:17:30" -> {
                        button5.isVisible = true
                        button20.isVisible = true
                        buttonO.isVisible = true
                        buttonLI.isVisible = true
                        buttonZI.isVisible = true
                        buttonS.isVisible = true
                        buttonDM.isVisible = true
                        autoText.isVisible = true
                        listView.isEnabled = false
                        btDis.text = getString(R.string.Disconnect)
                        enableButton()

                        "Robot_Tohng"
                    }
                    "00:20:08:00:14:55" -> {
                        button5.isVisible = false
                        button20.isVisible = false
                        buttonO.isVisible = false
                        buttonLI.isVisible = false
                        buttonZI.isVisible = false
                        buttonS.isVisible = false
                        buttonDM.isVisible = false
                        autoText.isVisible = false
                        listView.isEnabled = false
                        btDis.text = getString(R.string.Disconnect)
                        enableButton()

                        "RC_MK2"
                    }
                    else -> {
                        disableButton()

                        "Not support device"
                    }
                }
            }
        }
    }

    private fun disconnect() {
        if(MyApp.btSocket != null) {
            MyApp.btSocket!!.close()
            MyApp.btSocket = null
            btDis.text = getString(R.string.connect)
            listView.isEnabled = true
        } else {
            if(permissionDeny) {
                finish()
            } else {
                statusText.text = when(address) {
                    "00:18:91:D8:17:30" -> {
                        val text = MyApp.setupBluetoothConnection(this,this,address)
                        if(text == "Connected.") {
                            button5.isVisible = true
                            button20.isVisible = true
                            buttonO.isVisible = true
                            buttonLI.isVisible = true
                            buttonZI.isVisible = true
                            buttonS.isVisible = true
                            buttonDM.isVisible = true
                            autoText.isVisible = true
                            listView.isEnabled = false
                            btDis.text = getString(R.string.Disconnect)
                            writeByte("EMO")
                            enableButton()
                            MyApp.address = address
                        }

                        text
                    }
                    "00:20:08:00:14:55" -> {
                        val text = MyApp.setupBluetoothConnection(this,this,address)
                        if(text == "Connected.") {
                            button5.isVisible = false
                            button20.isVisible = false
                            buttonO.isVisible = false
                            buttonLI.isVisible = false
                            buttonZI.isVisible = false
                            buttonS.isVisible = false
                            buttonDM.isVisible = false
                            autoText.isVisible = false
                            listView.isEnabled = false
                            btDis.text = getString(R.string.Disconnect)
                            writeByte("EMO")
                            enableButton()
                            MyApp.address = address
                        }

                        text
                    }
                    "" -> {
                        pairedDevicesList()

                        "Paired new device."
                    }
                    else -> "This device is not compatible with this app."
                }
            }
        }
        btDis.isEnabled = true
    }

    private fun eStop() {
        busy = false
        writeByte("EMO")
        buttonS.isEnabled = false
    }

    private fun checkReady() {
        disableButton()
        val timeoutDuration = 2_000L // in milliseconds
        val startTime = System.currentTimeMillis()

        try {
            while (true) {
                // Check for timeout
                val currentTime = System.currentTimeMillis()
                if (currentTime - startTime >= timeoutDuration) {
                    statusText.text = getString(R.string.no_data)
                    writeByte("EMO")
                    enableButton()
                    break
                }

                if (MyApp.btSocket != null) {
                    val socket = MyApp.btSocket!!
                    if (socket.isConnected) {
                        val readBuffer = ByteArray(1024)
                        dis = DataInputStream(socket.inputStream)
                        val available = dis.available()
                        if(available > 4) {
                            val bytes = dis.read(readBuffer)
                            if (bytes > 0) {
                                val receivedData = String(readBuffer, 0, bytes, Charsets.UTF_8)
                                if(receivedData.length > 4) {
                                    // Update the UI on the main thread
                                    statusText.text = receivedData
                                    if(receivedData.contains('1')||receivedData.contains('2')||receivedData.contains('3')||receivedData.contains('4')||receivedData.contains('5')||receivedData.contains('6')||receivedData.contains('7')||receivedData.contains('8')||receivedData.contains('9')||receivedData.contains('0')) {
                                        writeByte("EMO")
                                        statusText.text = getString(R.string.ready)
                                    }
                                    enableButton()
                                    break
                                }
                            } else {
                                // End of stream
                                statusText.text = getString(R.string.no_data)
                                enableButton()
                                break
                            }
                        }
                    } else {
                        // Socket is not connected
                        reconnectSocket()
                    }
                } else {
                    // No socket available
                    handleNoSocketError()
                    break
                }
            }
        } catch (e: IOException) {
            // Handle IOException
            handleReadError(e)
        }
    }

    private fun writeByte(command: String) {
        try {
            if (MyApp.btSocket != null) {
                val socket = MyApp.btSocket!!
                if (socket.isConnected) {
                    dos = DataOutputStream(socket.outputStream)
                    dos.write(command.toByteArray(Charsets.UTF_8))
                    dos.flush()
                    if(!busy) {
                        enableButton()
                    }
                } else {
                    reconnectSocket()
                }
            } else {
                handleNoSocketError()
            }
        } catch (e: IOException) {
            handleWriteError(e)
        }
    }

    // Helper functions
    @SuppressLint("MissingPermission")
    private fun reconnectSocket() {
        try {
            MyApp.btSocket?.connect()
            statusText.text = if (MyApp.btSocket?.isConnected == true) {
                enableButton()
                listView.isEnabled = false
                btDis.text = getString(R.string.Disconnect)
                "Connected."
            } else {
                disableButton()
                btDis.isEnabled = true
                listView.isEnabled = true
                btDis.text = getString(R.string.connect)
                MyApp.btSocket = null
                "Failed to connect."
            }
        } catch (e: IOException) {
            handleConnectionError(e)
        }
    }

    private fun handleNoSocketError() {
        statusText.text = getString(R.string.no_socket_error)
        disableButton()
        btDis.isEnabled = true
        listView.isEnabled = true
        btDis.text = getString(R.string.connect)
    }

    private fun handleReadError(e: IOException) {
        statusText.text = getString(R.string.error_read_byte, e.message)
        disableButton()
        btDis.isEnabled = true
        listView.isEnabled = true
        btDis.text = getString(R.string.connect)
        MyApp.btSocket = null
    }

    private fun handleWriteError(e: IOException) {
        statusText.text = getString(R.string.error_write_byte, e.message)
        disableButton()
        btDis.isEnabled = true
        listView.isEnabled = true
        btDis.text = getString(R.string.connect)
        MyApp.btSocket = null
    }

    private fun handleConnectionError(e: IOException) {
        statusText.text = getString(R.string.connection_error, e.message)
        disableButton()
        btDis.isEnabled = true
        listView.isEnabled = true
        btDis.text = getString(R.string.connect)
        MyApp.btSocket = null
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
            if (!myBluetooth.isEnabled) {
                //Ask to the user turn the bluetooth on
                val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(turnBTon)
            }
            myBluetooth.bondedDevices
            val list = ArrayList<String>()
            if (myBluetooth.bondedDevices.isNotEmpty()) {
                for (bt in myBluetooth.bondedDevices) {
                    list.add(bt.name + "\n" + bt.address) //Get the device's name and the address
                }
            } else {
                val text = "No Paired Bluetooth Devices Found."
                statusText.text = text
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
            listView.adapter = adapter
        }
    }

    private val enableBluetoothLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val text = if (result.resultCode == RESULT_OK) {
            // Bluetooth was enabled by the user
            // Handle the success case
            "Bluetooth enabled!"
        } else {
            // Bluetooth was not enabled by the user
            // Handle the failure case
            "Please enable bluetooth!"
        }
        statusText.text = text
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MyApp.REQUEST_CODE_BLUETOOTH_SCAN -> {
                // Handle the result of the BLUETOOTH_SCAN permission request
                statusText.text = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    // Proceed with Bluetooth operations
                    "Bluetooth scan success!"
                } else {
                    // Permission denied
                    // Handle appropriately
                    "Bluetooth scan fail!"
                }
            }
            MyApp.REQUEST_CODE_BLUETOOTH_CONNECT -> {
                // Handle the result of the BLUETOOTH_SCAN permission request
                statusText.text = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    // Proceed with Bluetooth operations
                    "Bluetooth connected!"
                } else {
                    // Permission denied
                    // Handle appropriately
                    permissionDeny = true
                    "Please grant Bluetooth permission for this App."
                }
            }
            // Add other request code handling here if needed
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun disableButton() {
        buttonU.isEnabled = false
        buttonD.isEnabled = false
        buttonL.isEnabled = false
        buttonR.isEnabled = false
        button5.isEnabled = false
        button20.isEnabled = false
        buttonO.isEnabled = false
        buttonLI.isEnabled = false
        buttonDM.isEnabled = false
        buttonMN.isEnabled = false
        buttonRE.isEnabled = false
        buttonZI.isEnabled = false
        btDis.isEnabled = false
    }

    private fun enableButton() {
        buttonU.isEnabled = true
        buttonD.isEnabled = true
        buttonL.isEnabled = true
        buttonR.isEnabled = true
        button5.isEnabled = true
        button20.isEnabled = true
        buttonO.isEnabled = true
        buttonLI.isEnabled = true
        buttonDM.isEnabled = true
        buttonMN.isEnabled = true
        buttonRE.isEnabled = true
        buttonZI.isEnabled = true
        btDis.isEnabled = true
    }
}
