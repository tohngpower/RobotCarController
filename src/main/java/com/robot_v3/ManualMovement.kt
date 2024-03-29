package com.robot_v3

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_manual_movement.*
import java.io.*

class ManualMovement : AppCompatActivity() {
    private var btSocket: BluetoothSocket? = MyApp.instance?.btSocket
    @Volatile
    var havedata = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_movement)

        buttonFW.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    v.performClick()
                    buttonBW.isEnabled = false
                    buttonLT.isEnabled = false
                    buttonRT.isEnabled = false
                    try {
                        btSocket!!.outputStream.write("fw".toByteArray())
                        checkReady()
                        val temp = "Moving forward"
                        statusText.text = temp
                    } catch (e: IOException) {
                        MyApp.instance?.msg("Error on moving forward")
                        estop()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    buttonBW.isEnabled = true
                    buttonLT.isEnabled = true
                    buttonRT.isEnabled = true
                    try {
                        btSocket!!.outputStream.write("00".toByteArray())
                        checkReady()
                        val temp = "Ready"
                        statusText.text = temp
                    } catch (e: IOException) {
                        MyApp.instance?.msg("Error on stop")
                        estop()
                    }
                }
            }
            true
        }
        buttonBW.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    v.performClick()
                    buttonFW.isEnabled = false
                    buttonLT.isEnabled = false
                    buttonRT.isEnabled = false
                    try {
                        btSocket!!.outputStream.write("bw".toByteArray())
                        checkReady()
                        val temp = "Moving backward"
                        statusText.text = temp
                    } catch (e: IOException) {
                        MyApp.instance?.msg("Error on moving backward")
                        estop()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    buttonFW.isEnabled = true
                    buttonLT.isEnabled = true
                    buttonRT.isEnabled = true
                    try {
                        btSocket!!.outputStream.write("00".toByteArray())
                        checkReady()
                        val temp = "Ready"
                        statusText.text = temp
                    } catch (e: IOException) {
                        MyApp.instance?.msg("Error on stop")
                        estop()
                    }
                }
            }
            true
        }
        buttonLT.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    v.performClick()
                    buttonFW.isEnabled = false
                    buttonBW.isEnabled = false
                    buttonRT.isEnabled = false
                    try {
                        btSocket!!.outputStream.write("lt".toByteArray())
                        checkReady()
                        val temp = "Turning left"
                        statusText.text = temp
                    } catch (e: IOException) {
                        MyApp.instance?.msg("Error on turning left")
                        estop()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    buttonFW.isEnabled = true
                    buttonBW.isEnabled = true
                    buttonRT.isEnabled = true
                    try {
                        btSocket!!.outputStream.write("00".toByteArray())
                        checkReady()
                        val temp = "Ready"
                        statusText.text = temp
                    } catch (e: IOException) {
                        MyApp.instance?.msg("Error on stop")
                        estop()
                    }
                }
            }
            true
        }
        buttonRT.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    v.performClick()
                    buttonFW.isEnabled = false
                    buttonBW.isEnabled = false
                    buttonLT.isEnabled = false
                    try {
                        btSocket!!.outputStream.write("rt".toByteArray())
                        checkReady()
                        val temp = "Turning right"
                        statusText.text = temp
                    } catch (e: IOException) {
                        MyApp.instance?.msg("Error on turning right")
                        estop()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    buttonFW.isEnabled = true
                    buttonBW.isEnabled = true
                    buttonLT.isEnabled = true
                    try {
                        btSocket!!.outputStream.write("00".toByteArray())
                        checkReady()
                        val temp = "Ready"
                        statusText.text = temp
                    } catch (e: IOException) {
                        MyApp.instance?.msg("Error on stop")
                        estop()
                    }
                }
            }
            true
        }
        buttonBack.setOnClickListener { estop() }
        checkReady()
        val temp = "Ready"
        statusText.text = temp
    }

    override fun onBackPressed() {
        if (btSocket != null) {
            try {
                btSocket!!.outputStream.write("EMO".toByteArray())
                checkReady()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error")
            }
        }
        super.onBackPressed()
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
                    }
                }
            } catch (e: IOException) {
                MyApp.instance?.msg("Error, No data from Bluetooth module")
                havedata = false
                estop()
            }
        }
    }

    private fun estop() {
        onBackPressed()
    }
}