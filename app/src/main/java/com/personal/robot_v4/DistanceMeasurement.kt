package com.personal.robot_v4

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class DistanceMeasurement : AppCompatActivity() {
    private var btSocket: BluetoothSocket? = MyApp.btSocket
    private lateinit var button5: Button
    private lateinit var button10: Button
    private lateinit var button20: Button
    private lateinit var buttonN5: Button
    private lateinit var buttonN10: Button
    private lateinit var buttonN20: Button
    private lateinit var buttonExit: Button
    private lateinit var buttonL: Button
    private lateinit var buttonR: Button
    private lateinit var textView: TextView

    @Volatile
    var haveData = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance_measurement)

        button5 = findViewById(R.id.button5)
        button10 = findViewById(R.id.button10)
        button20 = findViewById(R.id.button20)
        buttonN5 = findViewById(R.id.buttonN5)
        buttonN10 = findViewById(R.id.buttonN10)
        buttonN20 = findViewById(R.id.buttonN20)
        buttonExit = findViewById(R.id.buttonExit)
        buttonL = findViewById(R.id.buttonL)
        buttonR = findViewById(R.id.buttonR)
        textView = findViewById(R.id.textView)

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
            eStop() //method to exit
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

    private fun eStop() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("EMO".toByteArray())
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        eStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        eStop()
    }

    private fun enableButton() {
        button5.isEnabled = true
        button10.isEnabled = true
        button20.isEnabled = true
        buttonN5.isEnabled = true
        buttonN10.isEnabled = true
        buttonN20.isEnabled = true
        buttonL.isEnabled = true
        buttonR.isEnabled = true
    }

    private fun disableButton() {
        button5.isEnabled = false
        button10.isEnabled = false
        button20.isEnabled = false
        buttonN5.isEnabled = false
        buttonN10.isEnabled = false
        buttonN20.isEnabled = false
        buttonL.isEnabled = false
        buttonR.isEnabled = false
    }

    private val distance: Unit
        get() {
            disableButton()
            haveData = true
            val readBuffer = ByteArray(1024)
            var bytes: Int
            while (haveData) {
                try {
                    val available = btSocket!!.inputStream.available()
                    if (available > 6) {
                        bytes = btSocket!!.inputStream.read(readBuffer)
                        if (bytes > 0) {
                            val distance = String(readBuffer)
                            if (distance.length > 6) {
                                val i = distance.indexOf('D')
                                 textView.text = distance.substring(i + 1)
                                haveData = false
                            }
                        }
                    }
                } catch (e: IOException) {
                    MyApp.instance?.msg("Error, No data from Bluetooth module")
                    haveData = false
                }
            }
            enableButton()
        }
}