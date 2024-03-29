package com.robot_v3

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_move_by_record.*
import java.io.*

class MoveByRecord : AppCompatActivity() {
    var btSocket: BluetoothSocket? = MyApp.instance?.btSocket

    @Volatile
    var havedata = false
    var content = StringBuilder()
    var busy = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_by_record)

        recordView.movementMethod = ScrollingMovementMethod()

        buttonStart.setOnClickListener {
            buttonStart.isEnabled = false
            buttonSave.isEnabled = false
            buttonBack.isEnabled = false
            recordView.isEnabled = false
            Movebyrec(this).execute() //start moving by record
        }
        buttonSave.setOnClickListener { save() }
        buttonBack.setOnClickListener { estop() }
        checkReady()
        readText()
    }

    override fun onBackPressed() {
        if (!busy) {
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

    private fun readText() {
        lateinit var fis: InputStream
        try {
            fis = openFileInput(FILE_NAME)
            val reader = BufferedReader(InputStreamReader(fis))
            var buffer: String?
            while (reader.readLine().also { buffer = it } != null) {
                content.append(buffer)
            }
            recordView!!.setText(content)
            val temp = "Ready"
            statusView!!.text = temp
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

    private fun estop() {
        onBackPressed()
    }

    private fun save() {
        val text = recordView!!.text.toString()
        var temp = ""
        if (text == temp) {
            temp = "Please fill in number for movement direction of Robot car"
            waitText!!.text = temp
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
                waitText!!.text = temp
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

        private class Movebyrec(private val moveByRecord: MoveByRecord) : AsyncTask<String?, Int?, String>() {
            var temp: String? = null
            var p = 0
            override fun onPreExecute() {
                temp = "Please wait while Robot car is moving!"
                moveByRecord.waitText!!.text = temp
                moveByRecord.busy = true
                p = 0
            }

            override fun doInBackground(vararg params: String?): String {
                for (i in moveByRecord.content.indices) {
                    when {
                        moveByRecord.content[i] == '1' -> {
                            try {
                                moveByRecord.btSocket!!.outputStream.write("fw".toByteArray())
                                moveByRecord.checkReady()
                                temp = "Move forward"
                                p = 100 * (i + 1) / moveByRecord.content.length
                                publishProgress(p)
                            } catch (e: IOException) {
                                MyApp.instance?.msg("Error on move forward")
                                moveByRecord.estop()
                            }
                        }
                        moveByRecord.content[i] == '2' -> {
                            try {
                                moveByRecord.btSocket!!.outputStream.write("bw".toByteArray())
                                moveByRecord.checkReady()
                                temp = "Reverse"
                                p = 100 * (i + 1) / moveByRecord.content.length
                                publishProgress(p)
                            } catch (e: IOException) {
                                MyApp.instance?.msg("Error on move backward")
                                moveByRecord.estop()
                            }
                        }
                        moveByRecord.content[i] == '3' -> {
                            try {
                                moveByRecord.btSocket!!.outputStream.write("lt".toByteArray())
                                moveByRecord.checkReady()
                                temp = "Turn left"
                                p = 100 * (i + 1) / moveByRecord.content.length
                                publishProgress(p)
                            } catch (e: IOException) {
                                MyApp.instance?.msg("Error on turn left")
                                moveByRecord.estop()
                            }
                        }
                        moveByRecord.content[i] == '4' -> {
                            try {
                                moveByRecord.btSocket!!.outputStream.write("rt".toByteArray())
                                moveByRecord.checkReady()
                                temp = "Turn right"
                                p = 100 * (i + 1) / moveByRecord.content.length
                                publishProgress(p)
                            } catch (e: IOException) {
                                MyApp.instance?.msg("Error on turn right")
                                moveByRecord.estop()
                            }
                        }
                    }
                }
                try {
                    moveByRecord.btSocket!!.outputStream.write("00".toByteArray())
                    moveByRecord.checkReady()
                } catch (e: IOException) {
                    MyApp.instance?.msg("Error")
                    moveByRecord.estop()
                }
                temp = "Done"
                return temp!!
            }

            override fun onProgressUpdate(vararg values: Int?) {
                //called when background task calls publishProgress
                //in doInBackground
                moveByRecord.statusView!!.text = temp
                moveByRecord.progressBar!!.progress = values[0]!!
            }

            override fun onPostExecute(result: String) {
                MyApp.alreadyshow = false
                MyApp.instance?.msg(result)
                moveByRecord.busy = false
                temp = "Complete moving."
                moveByRecord.waitText!!.text = temp
                temp = "Stop"
                moveByRecord.statusView!!.text = temp
                moveByRecord.buttonStart!!.isEnabled = true
                moveByRecord.buttonSave!!.isEnabled = true
                moveByRecord.buttonBack!!.isEnabled = true
                moveByRecord.recordView!!.isEnabled = true
            }

            override fun onCancelled() {
                //run on UI thread if task is cancelled
                moveByRecord.estop()
            }
        }
    }
}