package co.humanz.rcontroller

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import com.erz.joysticklibrary.JoyStick
import java.lang.Math.abs
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity(), JoyStick.JoyStickListener
{
    lateinit var socket: DatagramSocket
    lateinit var throttle: JoyStick
    lateinit var steering: JoyStick
    lateinit var addressText: EditText
    lateinit var server: String
    var steeringVal: Int = 0
    var throttleVal: Int = 0
    val step: Int = 10

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.throttle = findViewById<JoyStick>(R.id.throttle)
        this.steering = findViewById<JoyStick>(R.id.steering)
        this.addressText = findViewById<EditText>(R.id.address)

        this.throttle.type = JoyStick.TYPE_2_AXIS_UP_DOWN
        this.steering.type = JoyStick.TYPE_2_AXIS_LEFT_RIGHT

        this.throttle.setListener(this)
        this.steering.setListener(this)
    }

    override fun onTap()
    {

    }

    override fun onDoubleTap()
    {

    }

    override fun onMove(joyStick: JoyStick?, angle: Double, power: Double, direction: Int)
    {
        val jytval = joyStick!!.power.toInt()
        if (
                (abs(this.throttleVal) <= (jytval - step) || abs(this.throttleVal) >= (jytval + step) && this.throttle == joyStick) ||
                (abs(this.steeringVal) <= (jytval - step) || abs(this.steeringVal) >= (jytval + step) && this.steering == joyStick) || jytval == 0
        ) {
            Log.d("Joy", this.throttleVal.toString())
            if (this.throttle == joyStick) {
                this.throttleVal = jytval * if (joyStick.direction == JoyStick.DIRECTION_UP) 1 else -1
            } else if (this.steering == joyStick) {
                this.steeringVal = jytval * if (joyStick.direction == JoyStick.DIRECTION_RIGHT) 1 else -1
            }

            val thr = abs(this.throttleVal).toByte()
            val str = abs(this.steeringVal).toByte()

            val message: ByteArray = ByteArray(4)
            message[0] = if (this.throttleVal < 0) 0 else 1
            message[1] = thr
            message[2] = if (this.steeringVal < 0) 0 else 1
            message[3] = str
            val msg = Message()
            val packet = DatagramPacket(message, message.size)
            msg.socket = this.socket
            msg.datagram = packet
            SendMessage().execute(msg)
        }
    }


    fun initClient(view: View)
    {
        this.socket = DatagramSocket()
        Log.d("smarcar", "Init")
    }

    fun connect(view: View)
    {
        val msg = Message()
        msg.socket = this.socket
        msg.server = this.addressText.text.toString()
        Connection().execute(msg)
    }

    fun sendTestMessage(view: View)
    {
        val message = "Test"
        val msg = Message()
        val packet = DatagramPacket(message.toByteArray(), message.length)


        msg.socket = this.socket
        msg.datagram = packet
        SendMessage().execute(msg)
    }

    private class Message
    {
        var port: Int = 8059
        var server: String = "172.20.10.4"
        lateinit var socket: DatagramSocket
        lateinit var datagram: DatagramPacket
    }

    private class Connection: AsyncTask<Message, Void, Int>()
    {
        override fun doInBackground(vararg params: Message?): Int
        {
            Log.d("smarcar", "Connecting")
            val msg = params.first()!!
            msg.socket.connect(InetAddress.getByName(msg.server), msg.port)
            Log.d("smarcar", "Connected")
            return 0
        }
    }

    private class SendMessage: AsyncTask<Message, Void, Int>() {
        override fun doInBackground(vararg params: Message?): Int {
            Log.d("smarcar", "Sending")
            val msg = params.first()!!
            msg.socket.send(msg.datagram)
            Log.d("smarcar", "Sent")
            return 0
        }
    }


    }
