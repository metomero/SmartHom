package com.mete.akillihom

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlinx.android.synthetic.main.activity_connected.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*

@ExperimentalStdlibApi
class ConnectedActivity : AppCompatActivity() {

    enum class STATUS(val idx : Int) {
        LAMP(0), RGB(1), OFF(2)
    }

    private lateinit var lamp_img : ImageView
    private lateinit var rgb_img : ImageView

    private lateinit var lampButton : Button
    private lateinit var rgb_button : Button
    private lateinit var sleep_button : Button

    private lateinit var user_socket : Socket
    private lateinit var m_is : InputStream
    private lateinit var m_os : OutputStream

    private lateinit var user_room_info : TextView
    private lateinit var user_name :String

    private var backPressedNumber = 0

    private var status = arrayListOf(0, 0, 0) // LAMP, RGB, OFF

    private fun initData(){
        user_socket = MainActivity.sh_socket
        m_is = user_socket.getInputStream()
        m_os = user_socket.getOutputStream()
        user_name = intent.getStringExtra("USERNAME")

        user_room_info.text = "$user_name's Room"
    }

    private fun initControl(){
        lampButton = findViewById(R.id.LAMP_BUTTON)
        sleep_button = findViewById(R.id.SLEEP_BUTTON)
        rgb_button = findViewById(R.id.RGB_BUTTON)
        lamp_img = findViewById(R.id.LAMP_IMG)
        rgb_img = findViewById(R.id.RGB_IMG)

        user_room_info = findViewById(R.id.user_room)

        lampButton.setOnClickListener {
            sendOrder().execute("Lamp_on")
            lamp_img.setImageResource(R.drawable.lamp_open)
            rgb_img.setImageResource(R.drawable.rgb_off)
        }

        rgb_button.setOnClickListener {
            sendOrder().execute("RGB_on")
            lamp_img.setImageResource(R.drawable.lamp_close)
            rgb_img.setImageResource(R.drawable.rgb_on)
        }

        sleep_button.setOnClickListener{
            sendOrder().execute("Close_all")
            lamp_img.setImageResource(R.drawable.lamp_close)
            rgb_img.setImageResource(R.drawable.rgb_off)
        }

    }

    private fun initialize(){
        initControl()
        initData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connected)


        initialize()

    }

    inner class sendOrder() : AsyncTask<String, Void, Void>(){
        override fun doInBackground(vararg params: String?): Void? {

            val order_str = params[0]
            m_os.write(order_str!!.toByteArray())
            return null
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Thread{
            m_os.write("Quit".toByteArray())
            m_os.flush()
            m_is.close()
            m_os.close()
            user_socket.close()

        }.start()

    }

    override fun onBackPressed() {
        if(backPressedNumber >= 1)
            finish()

        Toast.makeText(this, "Press back again to leave", Toast.LENGTH_SHORT).show()
        backPressedNumber++

        Thread(Runnable {
            Thread.sleep(4000)
            backPressedNumber = 0
        }).start()
    }

}
