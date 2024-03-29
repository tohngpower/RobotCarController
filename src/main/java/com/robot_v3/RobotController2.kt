package com.robot_v3

import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_robot_controller.*
import java.io.IOException

class RobotController2 : AppCompatActivity() {

    private var btSocket: BluetoothSocket? = null
    private var address: String? = null
    private var cannotmeasure = false

    @Volatile
    var havedata = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val newint = intent
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS) //receive the address of the bluetooth device
        setContentView(R.layout.activity_robot_controller2)

        MyApp.instance?.setupBluetoothConnection(address) //Call the class to connect
        if (MyApp.instance?.connectBT == false) {
            MyApp.alreadyshow = false
            MyApp.instance?.msg("Error, Cannot connect RC_MK2")
            finish()
        }
        btSocket = MyApp.instance?.btSocket

        //commands to be sent to bluetooth
        buttonU.setOnClickListener {
            moveforward() //method to move forward
        }
        buttonD.setOnClickListener {
            movebackward() //method to reverse
        }
        buttonL.setOnClickListener {
            moveleft() //method to turn left
        }
        buttonR.setOnClickListener {
            moveright() //method to turn right
        }
        buttonS.setOnClickListener {
            estop() //method to stop motor
        }
        buttonMN.setOnClickListener {
            manualmove() //method to move manually
        }
        btdis.setOnClickListener {
            disconnect() //close connection
        }
        buttonRE.setOnClickListener {
            movebyrecord() //start moving by record
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket!!.outputStream.write("EMO".toByteArray())
                cannotmeasure = false
                btSocket!!.close() //close connection
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
        finish() //return to the first layout
    }

    private fun moveforward() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("MF".toByteArray())
                cannotmeasure = true
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun movebackward() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("MB".toByteArray())
                cannotmeasure = true
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun moveleft() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("ML".toByteArray())
                cannotmeasure = true
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun moveright() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("MR".toByteArray())
                cannotmeasure = true
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun manualmove() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("MN".toByteArray())
                val i = Intent(this@RobotController2, ManualMovement::class.java)
                startActivity(i)
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun estop() {
        if (btSocket != null && cannotmeasure) {
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
        havedata = true
        val readBuffer = ByteArray(1024)
        var bytes: Int
        while (havedata) {
            try {
                val available = btSocket!!.inputStream.available()
                if (available > 4) {
                    bytes = btSocket!!.inputStream.read(readBuffer)
                    if (bytes > 0) {
                        havedata = false
                        cannotmeasure = false
                    }
                }
            } catch (e: IOException) {
                MyApp.instance?.msg("Error, No data from Bluetooth module")
                havedata = false
            }
        }
    }

    private fun movebyrecord() {
        if (btSocket != null && !cannotmeasure) {
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
}