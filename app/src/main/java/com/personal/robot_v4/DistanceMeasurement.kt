package com.personal.robot_v4

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class DistanceMeasurement : AppCompatActivity() {
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
    private lateinit var statusText: TextView
    private lateinit var dos: DataOutputStream
    private lateinit var dis: DataInputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance_measurement)

        button5 = findViewById(R.id.dm_button5)
        button10 = findViewById(R.id.dm_button10)
        button20 = findViewById(R.id.dm_button20)
        buttonN5 = findViewById(R.id.buttonN5)
        buttonN10 = findViewById(R.id.buttonN10)
        buttonN20 = findViewById(R.id.buttonN20)
        buttonExit = findViewById(R.id.buttonExit)
        buttonL = findViewById(R.id.dm_buttonL)
        buttonR = findViewById(R.id.dm_buttonR)
        textView = findViewById(R.id.textView)
        statusText = findViewById(R.id.dm_statusText)

        distance()

        button5.setOnClickListener {
            disableButton()
            writeByte("M05")
            distance()
        }
        button10.setOnClickListener {
            disableButton()
            writeByte("M10")
            distance()
        }
        button20.setOnClickListener {
            disableButton()
            writeByte("M20")
            distance()
        }
        buttonN5.setOnClickListener {
            disableButton()
            writeByte("N05")
            distance()
        }
        buttonN10.setOnClickListener {
            disableButton()
            writeByte("N10")
            distance()
        }
        buttonN20.setOnClickListener {
            disableButton()
            writeByte("N20")
            distance()
        }
        buttonL.setOnClickListener {
            disableButton()
            writeByte("ML")
            distance()
        }
        buttonR.setOnClickListener {
            disableButton()
            writeByte("MR")
            distance()
        }
        buttonExit.setOnClickListener {
            eStop() //method to exit
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
        } finally {
            // Re-enable button after operations are complete
            enableButton()
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
        val i = Intent(this@DistanceMeasurement, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private fun handleReadError(e: IOException) {
        statusText.text = getString(R.string.error_read_byte, e.message)
        MyApp.btSocket = null
        val i = Intent(this@DistanceMeasurement, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private fun handleWriteError(e: IOException) {
        statusText.text = getString(R.string.error_write_byte, e.message)
        MyApp.btSocket = null
        val i = Intent(this@DistanceMeasurement, RobotController::class.java)
        startActivity(i)
        finish()
    }

    private fun handleConnectionError(e: IOException) {
        statusText.text = getString(R.string.connection_error, e.message)
        MyApp.btSocket = null
        val i = Intent(this@DistanceMeasurement, RobotController::class.java)
        startActivity(i)
        finish()
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

    private fun distance() {
        disableButton()
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
                        if(available > 6) {
                            val bytes = dis.read(readBuffer)
                            if (bytes > 0) {
                                val receivedData = String(readBuffer, 0, bytes, Charsets.UTF_8)
                                if(receivedData.length > 6) {
                                    // Update the UI on the main thread

                                    if(receivedData.contains('R')||receivedData.contains('e')||receivedData.contains('a')||receivedData.contains('y')) {
                                        textView.text = getString(R.string.cm)
                                    } else {
                                        val c = receivedData.indexOf(" c")
                                        val i = receivedData.indexOf("D")
                                        if(c == -1) {
                                            val cm = "${receivedData.substring(i + 1)} cm"
                                            textView.text = cm
                                        } else {
                                            textView.text = receivedData.substring(i+1)
                                        }
                                    }

                                    statusText.text = ""
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
        } finally {
            // Re-enable button after operations are complete
            enableButton()
        }
    }
}