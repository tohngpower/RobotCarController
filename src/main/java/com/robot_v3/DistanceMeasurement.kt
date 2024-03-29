package com.robot_v3

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_distance_measurement.*
import java.io.IOException

class DistanceMeasurement : AppCompatActivity() {
    private var btSocket: BluetoothSocket? = MyApp.instance?.btSocket

    @Volatile
    var havedata = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance_measurement)

        button5.setOnClickListener {
            move5() //method to move 5 steps
        }
        button10.setOnClickListener {
            move10() //method to move 10 steps
        }
        button20.setOnClickListener {
            move20() //method to move 20 steps
        }
        buttonN5.setOnClickListener {
            moveN5() //method to move -5 steps
        }
        buttonN10.setOnClickListener {
            moveN10() //method to move -10 steps
        }
        buttonN20.setOnClickListener {
            moveN20() //method to move -20 steps
        }
        buttonL.setOnClickListener {
            left() //method to move left
        }
        buttonR.setOnClickListener {
            right() //method to move right
        }
        buttonExit.setOnClickListener {
            estop() //method to exit
        }
        distance
    }

    private fun move5() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("M05".toByteArray())
                distance
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        } else {
            MyApp.instance?.msg("Please turn on bluetooth module.")
        }
    }

    private fun move10() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("M10".toByteArray())
                distance
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
    }

    private fun move20() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("M20".toByteArray())
                distance
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
    }

    private fun moveN5() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("N05".toByteArray())
                distance
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
    }

    private fun moveN10() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("N10".toByteArray())
                distance
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
    }

    private fun moveN20() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("N20".toByteArray())
                distance
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
    }

    private fun left() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("ML".toByteArray())
                distance
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
    }

    private fun right() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("MR".toByteArray())
                distance
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
    }

    private fun estop() {
        onBackPressed()
    }

    override fun onBackPressed() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("EMO".toByteArray())
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
        super.onBackPressed()
    }

    private fun enableButton() {
        button5!!.isEnabled = true
        button10!!.isEnabled = true
        button20!!.isEnabled = true
        buttonN5!!.isEnabled = true
        buttonN10!!.isEnabled = true
        buttonN20!!.isEnabled = true
        buttonL!!.isEnabled = true
        buttonR!!.isEnabled = true
    }

    private fun disableButton() {
        button5!!.isEnabled = false
        button10!!.isEnabled = false
        button20!!.isEnabled = false
        buttonN5!!.isEnabled = false
        buttonN10!!.isEnabled = false
        buttonN20!!.isEnabled = false
        buttonL!!.isEnabled = false
        buttonR!!.isEnabled = false
    }

    private val distance: Unit
        get() {
            disableButton()
            havedata = true
            val readBuffer = ByteArray(1024)
            var bytes: Int
            while (havedata) {
                try {
                    val available = btSocket!!.inputStream.available()
                    if (available > 6) {
                        bytes = btSocket!!.inputStream.read(readBuffer)
                        if (bytes > 0) {
                            val distance = String(readBuffer)
                            if (distance.length > 6) {
                                val i = distance.indexOf('D')
                                 textView!!.text = distance.substring(i + 1)
                                havedata = false
                            }
                        }
                    }
                } catch (e: IOException) {
                    MyApp.instance?.msg("Error, No data from Bluetooth module")
                    havedata = false
                }
            }
            enableButton()
        }
}