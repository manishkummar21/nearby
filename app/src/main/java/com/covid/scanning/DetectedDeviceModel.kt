package com.covid.scanning

import com.google.firebase.firestore.FieldValue

data class DetectedDeviceModel(var bluetoothID: String, var timestamp: FieldValue) {
    constructor() : this("", FieldValue.serverTimestamp())
}