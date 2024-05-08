package com.personal.robot_v4

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

class MoveByRecord : AppCompatActivity() {
    private lateinit var buttonBack: Button
    private lateinit var buttonSave: Button
    private lateinit var buttonStart: Button
    private lateinit var recordView: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var dos: DataOutputStream
    private lateinit var dis: DataInputStream
    private var content = StringBuilder()
    private val fileName = "record.txt"
    private var fis: FileInputStream? = null
    private var p = 0
    private val movingHandler = Handler(Looper.getMainLooper())
    private var actionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_by_record)

        buttonBack = findViewById(R.id.buttonBack)
        buttonSave = findViewById(R.id.buttonSave)
        buttonStart = findViewById(R.id.buttonStart)
        recordView = findViewById(R.id.recordView)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusView)

        recordView.movementMethod = ScrollingMovementMethod()
        progressBar.progress = p
        checkReady()
        readText()

        buttonStart.setOnClickListener {
            buttonStart.isEnabled = false
            buttonStart.isClickable = false
            buttonSave.isEnabled = false
            buttonSave.isClickable = false
            buttonBack.isEnabled = false
            buttonBack.isClickable = false
            recordView.isEnabled = false
            movingHandler.post(startMoving)
        }
        buttonSave.setOnClickListener {
            save()
        }
        buttonBack.setOnClickListener {
            eStop()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if(actionIndex == 0) {
            super.onBackPressed()
            eStop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        movingHandler.removeCallbacks(startMoving)
    }

    private fun checkReady() {
        val timeoutDuration = 500L // in milliseconds
        val startTime = System.currentTimeMillis()

        try {
            while (true) {
                // Check for timeout
                val currentTime = System.currentTimeMillis()
                if (currentTime - startTime >= timeoutDuration) {
                    statusText.text = getString(R.string.no_data)
                    writeByte("00")

                    break
                }

                if (MyApp.btSocket != null) {
                    val socket = MyApp.btSocket!!
                    if (socket.isConnected) {
                        val readBuffer = ByteArray(1024)
                        dis = DataInputStream(socket.inputStream)
                        val available: Int = dis.available()
                        if(available > 4) {
                            val bytes:Int = dis.read(readBuffer)
                            if (bytes > 0) {
                                val receivedData = String(readBuffer, 0, bytes, Charsets.UTF_8)
                                if(receivedData.length > 4) {
                                    statusText.text = receivedData

                                    break
                                }
                            } else {
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

    private fun save() {
        val text = recordView.text.toString()
        if (text.isEmpty()) {
            statusText.text = getString(R.string.please_fill_number)
        } else {
            var fos: FileOutputStream? = null
            try {
                fos = openFileOutput(
                    fileName,
                    Context.MODE_PRIVATE
                )
                fos.write(text.toByteArray())
                content = StringBuilder()
                content.append(text)
                statusText.text = getString(R.string.saved_to, filesDir, fileName)
            } catch (e: IOException) {
                statusText.text = e.message.toString()
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (e: IOException) {
                        statusText.text = e.message.toString()
                    }
                } else {
                    statusText.text = getString(R.string.ready)
                }
            }
        }
    }

    private fun readText() {
        try {
            if(openFileInput(fileName) == null) {
                save()
            } else {
                fis = openFileInput(fileName)
                val reader = BufferedReader(InputStreamReader(fis))
                var buffer: String?
                while (reader.readLine().also { buffer = it } != null) {
                    content.append(buffer)
                }
                recordView.setText(content)
                statusText.text = getString(R.string.ready)
            }
        } catch (e: IOException) {
            statusText.text = e.message.toString()
        } finally {
            try {
                if(fis != null) {
                    fis!!.close()
                } else {
                    statusText.text = getString(R.string.ready)
                }
            } catch (e: IOException) {
                statusText.text = e.message.toString()
            }
        }
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
        val i = Intent(this@MoveByRecord, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private fun handleReadError(e: IOException) {
        statusText.text = getString(R.string.error_read_byte, e.message)
        MyApp.btSocket = null
        val i = Intent(this@MoveByRecord, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private fun handleWriteError(e: IOException) {
        statusText.text = getString(R.string.error_write_byte, e.message)
        MyApp.btSocket = null
        val i = Intent(this@MoveByRecord, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private fun handleConnectionError(e: IOException) {
        statusText.text = getString(R.string.connection_error, e.message)
        MyApp.btSocket = null
        val i = Intent(this@MoveByRecord, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private val startMoving = object : Runnable {
        override fun run() {
            when (content[actionIndex]) {
                '1' -> writeByte("fw")
                '2' -> writeByte("bw")
                '3' -> writeByte("lt")
                '4' -> writeByte("rt")
            }
            // After each command, check readiness
            checkReady()
            p = 100 * (actionIndex + 1) / content.length
            val text = "Now moving $p%"
            statusText.text = text
            progressBar.progress = p
            // Increment action index and post the next action if needed
            actionIndex++
            if (actionIndex < content.length) {
                movingHandler.postDelayed(this, 100)
            } else {
                actionIndex = 0
                writeByte("00")
                checkReady()
                buttonStart.isEnabled = true
                buttonStart.isClickable = true
                buttonSave.isEnabled = true
                buttonSave.isClickable = true
                buttonBack.isEnabled = true
                buttonBack.isClickable = true
                recordView.isEnabled = true
                movingHandler.removeCallbacks(this)
            }
        }
    }
}