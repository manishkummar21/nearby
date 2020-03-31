package com.covid.scanning

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.covid.scanning.databinding.ActivityDiscoverBinding
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.HashSet

class DiscoverActivity : AppCompatActivity() {

    private val TAG = DiscoverActivity::class.java.canonicalName

    lateinit var mainBinding: ActivityDiscoverBinding

    // Our handle to Nearby Connections
    private var connectionsClient: ConnectionsClient? = null

    private val STRATEGY = Strategy.P2P_STAR

    private val NEARBY_SERVICE_NAME = BuildConfig.APPLICATION_ID

    private var database: FirebaseFirestore? = null

    private var restitutionIDS: HashSet<String> = HashSet()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_discover)

        restitutionIDS.clear()

        connectionsClient = Nearby.getConnectionsClient(this)

        mainBinding.pulsator.setAvators(getAvatars())

        mainBinding.pulsator.post {
            mainBinding.pulsator.start()
        }

        database = Firebase.firestore

        database?.let {

            it.collection("restitutionIDS")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d("Tag", "${document.id} => ${document.data}")
                        restitutionIDS.add(document.data.getValue("bluetoothID") as String)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Tag", "Error getting documents.", exception)
                }

            it.collection("restitutionIDS")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }

                    for (dc in snapshots!!.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.d(TAG, "New value: ${dc.document.data}")
                                restitutionIDS.add(dc.document.data.getValue("bluetoothID") as String)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                Log.d(TAG, "Modified city: ${dc.document.data}")
                                restitutionIDS.add(dc.document.data.getValue("bluetoothID") as String)
                            }
                            DocumentChange.Type.REMOVED -> {
                                Log.d(TAG, "Removed city: ${dc.document.data}")
                                restitutionIDS.remove(dc.document.data.getValue("bluetoothID") as String)

                            }
                        }
                    }
                }

        }

        startDiscovery()

    }

    private fun startDiscovery() {

        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()

        connectionsClient?.let {

            it.startDiscovery(
                NEARBY_SERVICE_NAME,
                endpointDiscoveryCallback,
                discoveryOptions
            )
                .addOnSuccessListener {
                    Log.d("Tag", "Looking for other device")
                }
                .addOnFailureListener {
                    Log.d("Tag", "Failed to Discover")
                    mainBinding.pulsator.stop()
                }
        }


    }

    // Callbacks for finding other devices
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "onEndpointFound: endpoint found")
            Log.d(TAG, "Endpoint ID: $endpointId")
            Log.d(TAG, "Endpoint Name:" + info.endpointName)
            if (restitutionIDS.contains(info.endpointName))
                mainBinding.pulsator.addDetecteddevice(info)
        }

        override fun onEndpointLost(endpointId: String) {

        }
    }


    public override fun onDestroy() {
        super.onDestroy()
        connectionsClient?.stopAllEndpoints()
        connectionsClient = null
    }

    fun getAvatars(): ArrayList<*> {
        return ArrayList<Any>(
            Arrays.asList(
                R.drawable.icon1,
                R.drawable.icon2,
                R.drawable.icon3,
                R.drawable.icon4,
                R.drawable.icon5,
                R.drawable.icon6,
                R.drawable.icon7,
                R.drawable.icon8,
                R.drawable.icon9,
                R.drawable.icon10,
                R.drawable.icon11,
                R.drawable.icon12
            )
        )

    }

}