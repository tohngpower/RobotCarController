package com.personal.robot_v4

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class MoveByRecord : AppCompatActivity() {
    var btSocket: BluetoothSocket? = MyApp.btSocket
    private lateinit var buttonBack: Button
    private lateinit var buttonSave: Button
    private lateinit var buttonStart: Button
    private lateinit var recordView: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var statusView: TextView
    private lateinit var waitText: TextView

    @Volatile
    var haveData = false
    var content = StringBuilder()
    var busy = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_by_record)

        buttonBack = findViewById(R.id.buttonBack)
        buttonSave = findViewById(R.id.buttonSave)
        buttonStart = findViewById(R.id.buttonStart)
        recordView = findViewById(R.id.recordView)
        progressBar = findViewById(R.id.progressBar)
        statusView = findViewById(R.id.statusView)
        waitText = findViewById(R.id.waitText)

        recordView.movementMethod = ScrollingMovementMethod()

        buttonStart.setOnClickListener {
            buttonStart.isEnabled = false
            buttonSave.isEnabled = false
            buttonBack.isEnabled = false
            recordView.isEnabled = false
            MoveByRec(this).execute() //start moving by record
        }
        buttonSave.setOnClickListener { save() }
        buttonBack.setOnClickListener { eStop() }
        checkReady()
        readText()
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

    private fun readText() {
        lateinit var fis: InputStream
        try {
            fis = openFileInput(FILE_NAME)
            val reader = BufferedReader(InputStreamReader(fis))
            var buffer: String?
            while (reader.readLine().also { buffer = it } != null) {
                content.append(buffer)
            }
            recordView.setText(content)
            val temp = "Ready"
            statusView.text = temp
        } catch (e: IOException) {
            save()
        } finally {
            try {
                fis.close()
            } catch (e: IOException) {
                MyApp.instance?.msg("Error, No file record.txt")
            }
        }
    }

    private fun eStop() {
        if (!busy) {
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

    private fun save() {
        val text = recordView.text.toString()
        var temp = ""
        if (text == temp) {
            temp = "Please fill in number for movement direction of Robot car"
            waitText.text = temp
        } else {
            var fos: FileOutputStream? = null
            try {
                fos = openFileOutput(
                    FILE_NAME,
                    Context.MODE_PRIVATE
                )
                fos.write(text.toByteArray())
                content = StringBuilder()
                content.append(text)
                temp = "Saved to $filesDir/$FILE_NAME"
                waitText.text = temp
            } catch (e: IOException) {
                MyApp.instance?.msg("Error file not found!")
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (e: IOException) {
                        MyApp.instance?.msg("Error file not found!")
                    }
                }
            }
        }
    }

    companion object {
        private const val FILE_NAME = "record.txt"

        class MoveByRec(private val moveByRecord: MoveByRecord) {

            private var p = 0

            fun execute() {
                // Launch a coroutine in the scope of the current view
                CoroutineScope(Dispatchers.Main).launch {
                    onPreExecute()
                    val result = withContext(Dispatchers.IO) {
                        doInBackground()
                    }
                    onPostExecute(result)
                }
            }

            private fun onPreExecute() {
                // UI-related initialization
                val text = "Please wait while Robot car is moving!"
                moveByRecord.waitText.text = text
                moveByRecord.busy = true
            }

            private suspend fun doInBackground(): String {
                // Perform background operations here
                for (i in moveByRecord.content.indices) {
                    when (moveByRecord.content[i]) {
                        '1' -> performAction("fw", "Move forward",i)
                        '2' -> performAction("bw", "Reverse",i)
                        '3' -> performAction("lt", "Turn left",i)
                        '4' -> performAction("rt", "Turn right",i)
                    }
                }
                performAction("00", "Done",0)
                return "Done"
            }

            private suspend fun performAction(command: String, message: String, index: Int) {
                // Handle the command and UI updates
                try {
                    withContext(Dispatchers.IO) {
                        moveByRecord.btSocket!!.outputStream.write(command.toByteArray())
                        moveByRecord.checkReady()
                        moveByRecord.statusView.text = message
                        moveByRecord.progressBar.progress = calculateProgress(index)
                    }
                } catch (e: IOException) {
                    // Handle error
                    withContext(Dispatchers.Main) {
                        MyApp.instance?.msg("Error: $message")
                        moveByRecord.eStop()
                    }
                }
            }

            private fun calculateProgress(i: Int): Int {
                // Calculate the progress as a percentage
                p = 100 * (i + 1) / moveByRecord.content.length
                return p
            }

            private fun onPostExecute(result: String) {
                // UI-related finalization
                var text = "Complete moving."
                moveByRecord.waitText.text = text
                text = "Stop"
                moveByRecord.statusView.text = text
                moveByRecord.buttonStart.isEnabled = true
                moveByRecord.buttonSave.isEnabled = true
                moveByRecord.buttonBack.isEnabled = true
                moveByRecord.recordView.isEnabled = true
                moveByRecord.busy = false
                MyApp.instance?.msg(result)
            }
        }
    }
}