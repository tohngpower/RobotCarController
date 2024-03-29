package com.robot_v3

import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_robot_controller.*
import java.io.IOException

class RobotController : AppCompatActivity() {

    private var btSocket: BluetoothSocket? = null
    private var address: String? = null
    private var cannotmeasure = false

    @Volatile
    var havedata = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val newint = intent
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS) //receive the address of the bluetooth device
        setContentView(R.layout.activity_robot_controller)

        MyApp.instance?.setupBluetoothConnection(address) //Call the class to connect
        if (MyApp.instance?.connectBT == false) {
            MyApp.alreadyshow = false
            MyApp.instance?.msg("Error, Cannot connect Robot_Tohng")
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
        buttonO.setOnClickListener {
            objectevasion() //method to evade object
        }
        buttonLI.setOnClickListener {
            linetracking() //method to track black line
        }
        buttonS.setOnClickListener {
            estop() //method to stop motor
        }
        button5.setOnClickListener {
            move5() //method to move 5 steps
        }
        buttonMN.setOnClickListener {
            manualmove() //method to move manually
        }
        button20.setOnClickListener {
            move20() //method to move 20 steps
        }
        btdis.setOnClickListener {
            disconnect() //close connection
        }
        buttonZI.setOnClickListener {
            moveZigZag() //move zigzag
        }
        buttonDM.setOnClickListener {
            measuredistance() //measure distance
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

    private fun move5() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("M05".toByteArray())
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
                val i = Intent(this@RobotController, ManualMovement::class.java)
                startActivity(i)
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun move20() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("M20".toByteArray())
                cannotmeasure = true
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun objectevasion() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("OE".toByteArray())
                cannotmeasure = true
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun linetracking() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("LINE".toByteArray())
                cannotmeasure = true
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

    private fun moveZigZag() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("ZI".toByteArray())
                cannotmeasure = true
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun measuredistance() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("DM".toByteArray())
                val i = Intent(this@RobotController, DistanceMeasurement::class.java)
                startActivity(i)
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }

    private fun checkReady() {
        buttonDM!!.isEnabled = false
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
                        buttonDM!!.isEnabled = true
                    }
                }
            } catch (e: IOException) {
                MyApp.instance?.msg("Error, No data from Bluetooth module")
                havedata = false
                buttonDM!!.isEnabled = true
            }
        }
    }

    private fun movebyrecord() {
        if (btSocket != null && !cannotmeasure) {
            try {
                btSocket!!.outputStream.write("REC".toByteArray())
                val i = Intent(this@RobotController, MoveByRecord::class.java)
                startActivity(i)
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Press STOP button first!", true)
        }
    }
}