package com.covid.scanning

import android.content.Context
import android.content.SharedPreferences
import com.covid.scanning.login.model.User
import com.google.gson.Gson

class PreferenceManager internal constructor(context: Context) {

    private val preferences: SharedPreferences

    private val editor: SharedPreferences.Editor

    private val PRIVATE_MODE = 0

    private val PREF_NAME = "covid"

    private val KEY_UserID = "key_userID"

    private val KEY_UserDetails = "Key_UserDetails"

    private val Is_Login = "IsLogged"

    init {
        preferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = preferences.edit()
    }

    var userID: String?
        get() = preferences.getString(KEY_UserID, null)
        set(userID) {
            editor.putString(KEY_UserID, userID)
            editor.apply()
        }

    var isLogged: Boolean
        get() = preferences.getBoolean(Is_Login, false)
        set(isLogged) {
            editor.putBoolean(Is_Login, isLogged)
            editor.apply()
        }

    var userDetails: User
        get() = Gson().fromJson(preferences.getString(KEY_UserDetails, null), User::class.java)
        set(userDetails) {
            editor.putString(KEY_UserDetails, Gson().toJson(userDetails))
            editor.apply()
        }

    fun logout() {
        editor.clear()
        editor.commit()
    }
}
