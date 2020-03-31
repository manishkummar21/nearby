package com.covid.scanning

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.view.View
import java.lang.reflect.InvocationTargetException


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.canonicalName

    private var bluetoothAdapter: BluetoothAdapter? = null

    val REQUEST_ENABLE_BT = 2


    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )

    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


        findViewById<View>(R.id.startdiscovering).setOnClickListener {

            if (hasPermissions(this, REQUIRED_PERMISSIONS) && bluetoothAdapter!!.isEnabled())
                startActivity(Intent(this, DiscoverActivity::class.java))
            else
                enableBluetooth()
        }

        if (checkIfDeviceSupports())
            return

        enableBluetooth()

    }

    fun checkIfDeviceSupports(): Boolean {
        return bluetoothAdapter == null
    }

    fun checkPermission() {
        if (hasPermissions(this, REQUIRED_PERMISSIONS)) {
            startService(getBluetoothMacAddress())
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS)
        }
    }

    fun enableBluetooth() {
        if (!bluetoothAdapter!!.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else
            checkPermission()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return
        }

        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }

        checkPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            checkPermission()
        } else
            finish()
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun startService(name: String) {

        val serviceIntent = Intent(this, AdvertisingService::class.java)
        serviceIntent.putExtra("inputExtra", name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }


    @SuppressLint("HardwareIds")
    private fun getBluetoothMacAddress(): String {
        return bluetoothAdapter!!.address
    }
}
