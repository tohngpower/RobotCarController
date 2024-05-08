package com.personal.robot_v4

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.*

class ManualMovement : AppCompatActivity() {
    private lateinit var buttonBW: ImageButton
    private lateinit var buttonFW: ImageButton
    private lateinit var buttonLT: ImageButton
    private lateinit var buttonRT: ImageButton
    private lateinit var buttonBack: Button
    private lateinit var statusText: TextView
    private lateinit var dos: DataOutputStream
    private lateinit var dis: DataInputStream

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_movement)

        buttonFW = findViewById(R.id.buttonFW)
        buttonBW = findViewById(R.id.buttonBW)
        buttonLT = findViewById(R.id.buttonLT)
        buttonRT = findViewById(R.id.buttonRT)
        buttonBack = findViewById(R.id.mn_buttonBack)
        statusText = findViewById(R.id.mn_text)

        checkReady()
        statusText.text = getString(R.string.ready)

        buttonFW.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    v.performClick()
                    buttonBW.isEnabled = false
                    buttonLT.isEnabled = false
                    buttonRT.isEnabled = false
                    writeByte("fw")
                    checkReady()
                    statusText.text = getString(R.string.moving_forward)
                }
                MotionEvent.ACTION_UP -> {
                    buttonBW.isEnabled = true
                    buttonLT.isEnabled = true
                    buttonRT.isEnabled = true
                    writeByte("00")
                    checkReady()
                    statusText.text = getString(R.string.ready)
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
                    writeByte("bw")
                    checkReady()
                    statusText.text = getString(R.string.moving_backward)
                }
                MotionEvent.ACTION_UP -> {
                    buttonFW.isEnabled = true
                    buttonLT.isEnabled = true
                    buttonRT.isEnabled = true
                    writeByte("00")
                    checkReady()
                    statusText.text = getString(R.string.ready)
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
                    writeByte("lt")
                    checkReady()
                    statusText.text = getString(R.string.turning_left)
                }
                MotionEvent.ACTION_UP -> {
                    buttonFW.isEnabled = true
                    buttonBW.isEnabled = true
                    buttonRT.isEnabled = true
                    writeByte("00")
                    checkReady()
                    statusText.text = getString(R.string.ready)
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
                    writeByte("rt")
                    checkReady()
                    statusText.text = getString(R.string.turning_right)
                }
                MotionEvent.ACTION_UP -> {
                    buttonFW.isEnabled = true
                    buttonBW.isEnabled = true
                    buttonLT.isEnabled = true
                    writeByte("00")
                    checkReady()
                    statusText.text = getString(R.string.ready)
                }
            }
            true
        }
        buttonBack.setOnClickListener {
            eStop()
        }
    }

    private fun checkReady() {
        val timeoutDuration = 1_000L // in milliseconds
        val startTime = System.currentTimeMillis()
        try {
            while (true) {
                // Check for timeout
                val currentTime = System.currentTimeMillis()
                if (currentTime - startTime >= timeoutDuration) {
                    statusText.text = getString(R.string.no_data)
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
                                    break
                                }
                            } else {
                                // End of stream
                                statusText.text = getString(R.string.no_data)
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

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
        eStop()
    }

    private fun eStop() {
        writeByte("EMO")
        finish()
    }

    private fun writeByte(command: String) {
        try {
            if (MyApp.btSocket != null) {
                val socket = MyApp.btSocket!!
                if (socket.isConnected) {
                    dos = DataOutputStream(socket.outputStream)
                    dos.write(command.toByteArray(Charsets.UTF_8))
                    dos.flush()
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
                "Connected."
            } else {
                "Failed to connect."
            }
        } catch (e: IOException) {
            handleConnectionError(e)
        }
    }

    private fun handleNoSocketError() {
        statusText.text = getString(R.string.no_socket_error)
        val i = Intent(this@ManualMovement, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private fun handleReadError(e: IOException) {
        statusText.text = getString(R.string.error_read_byte, e.message)
        MyApp.btSocket = null
        val i = Intent(this@ManualMovement, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private fun handleWriteError(e: IOException) {
        statusText.text = getString(R.string.error_write_byte, e.message)
        MyApp.btSocket = null
        val i = Intent(this@ManualMovement, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private fun handleConnectionError(e: IOException) {
        statusText.text = getString(R.string.connection_error, e.message)
        MyApp.btSocket = null
        val i = Intent(this@ManualMovement, RobotController::class.java)
        startActivity(i)
        finish()
    }
}