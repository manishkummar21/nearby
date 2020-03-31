package com.covid.scanning

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*


class AdvertisingService : Service() {

    private val TAG = "AdvertisingService"
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "AdvertisingServiceChannel"
    private val NEARBY_SERVICE_NAME = BuildConfig.APPLICATION_ID

    // Our handle to Nearby Connections
    private var connectionsClient: ConnectionsClient? = null

    private val STRATEGY = Strategy.P2P_STAR

    private var deviceIdentifier: String? = null


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        deviceIdentifier = intent.getStringExtra("inputExtra")

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Start Advertising")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification);
        }

        //do heavy work on a background thread
        connectionsClient = Nearby.getConnectionsClient(this)

        startAdvertising()

        createBluetoothChangeListener();


        return START_NOT_STICKY
    }

    private fun createBluetoothChangeListener() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mReceiver, filter)
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()

        connectionsClient?.let {

            it.startAdvertising(
                deviceIdentifier!!,
                NEARBY_SERVICE_NAME,
                connectionLifecycleCallback,
                advertisingOptions
            )
                .addOnSuccessListener {
                    println("Successfully Started Advertising")
                }
                .addOnFailureListener {
                    println("Failed to Advertising")
                }
        }


    }


    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }

    }


    // Callbacks for connections to other devices
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.i(TAG, "onConnectionInitiated: accepting connection")
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.i(TAG, "onConnectionResult: connection successful")
            } else {
                Log.i(TAG, "onConnectionResult: connection failed")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "onDisconnected: disconnected from endpoint")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionsClient?.stopAllEndpoints()
        connectionsClient = null

        // Unregister broadcast listeners
        unregisterReceiver(mReceiver)
    }

    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent())
                        context.stopService(Intent(context, AdvertisingService::class.java))
                    }
                }
            }
        }
    }


}