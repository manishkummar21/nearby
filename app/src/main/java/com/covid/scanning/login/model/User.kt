package com.covid.scanning.login.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class User(var user_id: String, var mobile_number: String, var bluetoothID: String) {

    constructor() : this("", "", "")

}
