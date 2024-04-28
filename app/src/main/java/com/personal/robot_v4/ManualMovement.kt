package com.personal.robot_v4

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.*

class ManualMovement : AppCompatActivity() {
    private var btSocket: BluetoothSocket? = MyApp.btSocket
    private lateinit var buttonBW: Button
    private lateinit var buttonFW: Button
    private lateinit var buttonLT: Button
    private lateinit var buttonRT: Button
    private lateinit var buttonBack: Button
    private lateinit var statusText: TextView
    @Volatile
    var haveData = false

    @SuppressLint("ClickableViewAccessibility")
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
                        eStop()
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
                        eStop()
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
                        eStop()
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
                        eStop()
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
                        eStop()
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
                        eStop()
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
                        eStop()
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
                        eStop()
                    }
                }
            }
            true
        }
        buttonBack.setOnClickListener { eStop() }
        checkReady()
        val temp = "Ready"
        statusText.text = temp
    }

    override fun onPause() {
        super.onPause()
        eStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        eStop()
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
                eStop()
            }
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
        }
    }
}