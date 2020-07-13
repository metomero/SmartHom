package com.mete.akillihom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.net.*
import java.util.*


@ExperimentalStdlibApi
class MainActivity : AppCompatActivity() {

    private lateinit var sp: SharedPreferences

    lateinit var pin_text: TextView
    lateinit var password: EditText
    lateinit var login_group: Group
    lateinit var connection_button: Button
    lateinit var login_button: Button
    lateinit var connection_info: TextView
    lateinit var connection_tip: TextView

    private lateinit var m_os: OutputStream
    private lateinit var m_is: InputStream

    private var isConnected = false

    private var backPressedNumber = 0

    companion object {
        lateinit var sh_socket: Socket
    }


    lateinit var progress_bar: ProgressBar

    private var user_name = ""
    private var user_hak = 3


    private var myHandler: Handler =
        @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)

                if (msg!!.what == 0) {
                    password.text.clear()

                    if (--user_hak != 0) {
                        Toast.makeText(
                            applicationContext,
                            "Invalid password, ${user_hak} attempts left",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please login again.",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }

                if (msg!!.what == 1) {
                    Toast.makeText(
                        applicationContext,
                        "${user_name}, welcome",
                        Toast.LENGTH_LONG
                    ).show()

                    isConnected = true
                    var intent = Intent(this@MainActivity, ConnectedActivity::class.java)
                    intent.putExtra("USERNAME", user_name)
                    startActivity(intent)
                    finish()
                }

            }
        }

    fun initControl() {
        pin_text = findViewById(R.id.pin_text)
        password = findViewById(R.id.PASSWORD)
        login_button = findViewById(R.id.login_button)

        connection_button = findViewById(R.id.connection_button)
        connection_info = findViewById(R.id.connection_info)
        connection_tip = findViewById(R.id.connection_tip)
        progress_bar = findViewById(R.id.progressBar)

    }

    fun initData() {

        sp = getPreferences(Context.MODE_PRIVATE)


        connection_button.setOnClickListener {
            connection_info.text = resources.getString(R.string.connection_trying)
            it.visibility = View.INVISIBLE
            progress_bar.visibility = View.VISIBLE
            connection_tip.visibility = View.VISIBLE

            findSmartHomeServer().execute()
        }

        connection_info.text = resources.getString(R.string.connection_trying)
        connection_button.visibility = View.INVISIBLE
        connection_tip.visibility = View.VISIBLE


        login_button.visibility = View.INVISIBLE
        pin_text.visibility = View.INVISIBLE
        password.visibility = View.INVISIBLE

        login_button.setOnClickListener {

            Thread {

                m_os.write(password.text.toString().toByteArray())
                var arr = ByteArray(10)
                m_is.read(arr)

                var builder = java.lang.StringBuilder()

                for (c in arr) {
                    if (c.toInt() != 0)
                        builder.append(c.toChar())
                }

                var result = builder.toString().trim()

                if (result == "denied") {
                    myHandler.sendEmptyMessage(0)

                } else {
                    user_name = result
                    myHandler.sendEmptyMessage(1)
                }

                Thread.sleep(100)


            }.start()

        }

    }

    fun initialize() {
        initControl()
        initData()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
        findSmartHomeServer().execute()

    }


    inner class findSmartHomeServer : AsyncTask<Void, Void, Boolean>() {

        private var mContextRef: WeakReference<Context>? = null
        private var ipString: String = ""
        private var prefix: String = ""


        override fun onPreExecute() {
            super.onPreExecute()


            mContextRef = WeakReference<Context>(applicationContext)
        }

        override fun doInBackground(vararg params: Void?): Boolean {

            val m_context = mContextRef!!.get()
            ipString = getIP()


            prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)

            if (m_context != null) {

                val saved_ip = sp.getString("IP", "NOIP")

                if ((saved_ip != "NOIP") && (checkIP(saved_ip!!))) {
                        return true
                } else {
                    for (i in 2..255) {

                        val testIp = prefix + i.toString()
                        Log.i("TEST", "Testing... ->>: $testIp")

                        if (checkIP(testIp)) {
                            return true
                        }
                    }
                }

            }

            return false
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)

            if (result!!) {
                Toast.makeText(applicationContext, "Connection completed", Toast.LENGTH_SHORT)
                    .show()

                pin_text.visibility = View.VISIBLE
                password.visibility = View.VISIBLE
                login_button.visibility = View.VISIBLE


                connection_button.visibility = View.INVISIBLE
                progress_bar.visibility = View.INVISIBLE
                connection_info.visibility = View.INVISIBLE
                connection_tip.visibility = View.INVISIBLE


            } else {
                Toast.makeText(
                    applicationContext,
                    "Connection lost, please try again",
                    Toast.LENGTH_SHORT
                ).show()

                progress_bar.visibility = View.INVISIBLE

                connection_button.visibility = View.VISIBLE
                connection_info.text = resources.getString(R.string.connection_error)
                connection_tip.visibility = View.INVISIBLE


            }

        }

        private fun checkIP(testIp: String): Boolean {


            var arr = ByteArray(3)



            try {

                val socket = Socket()
                socket.connect(InetSocketAddress(testIp, 23456), 100)

                m_os = socket.getOutputStream()
                m_is = socket.getInputStream()

                m_os.write("AUSH".toByteArray()) // AUSH = r u smart home
                m_os.flush()

                val res = m_is.read(arr)
                if (res != -1) {
                    if (arr[1].toChar() == 'e') {
                        sh_socket = socket
                        val editor = sp.edit()
                        editor.putString("IP", testIp)
                        editor.apply()
                    }
                    return true


                } else {
                    m_is.close()
                    m_os.close()
                    socket.close()
                }
            } catch (ex: IOException) {

            }

            return false
        }


    }

    private fun getIP(): String {
        val wifiMan = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInf = wifiMan.connectionInfo
        val ipAddress = wifiInf.ipAddress

        val ip = String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )

        return ip.trim()
    }


    override fun onDestroy() {
        super.onDestroy()

        if (!isConnected) {
            Thread {
                m_os.write("Quit".toByteArray())
                m_os.flush()
                m_is.close()
                m_os.close()
                sh_socket.close()

            }.start()
        }

    }

    override fun onBackPressed() {
        if (backPressedNumber >= 1)
            finish()

        Toast.makeText(this, "Press back again to leave", Toast.LENGTH_SHORT).show()
        backPressedNumber++

        Thread(Runnable {
            Thread.sleep(4000)
            backPressedNumber = 0
        }).start()
    }
}

