package com.covid.scanning

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.covid.scanning.databinding.ActivityLoginBinding
import com.covid.scanning.login.LoginInteractor
import com.covid.scanning.login.LoginState
import com.covid.scanning.login.LoginViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var databinding: ActivityLoginBinding

    private var bluetoothAdapter: BluetoothAdapter? = null

    private lateinit var viewModel: LoginViewModel

    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (checkIfDeviceSupports()) {
            finish()
            return
        }

        createProgressDialog()


        viewModel = ViewModelProviders.of(
            this,
            LoginViewModel.LoginViewModelFactory(LoginInteractor())
        )[LoginViewModel::class.java]

        viewModel._loginState.observe(::getLifecycle, ::updateUI)

        databinding.login.setOnClickListener {
            onLoginClicked()
        }


    }

    fun checkIfDeviceSupports(): Boolean {
        return bluetoothAdapter == null
    }

    private fun updateUI(screenState: ScreenState<LoginState>?) {
        when (screenState) {
            ScreenState.Loading -> progressDialog?.show()
            is ScreenState.Render -> processLoginState(screenState.renderState)
        }
    }

    private fun processLoginState(loginState: LoginState) {
        progressDialog?.dismiss()
        when (loginState) {
            LoginState.Error -> println("UnknownError")
            LoginState.Success -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun onLoginClicked() {
        if (!databinding.mobileno.text.toString().isEmpty())
            viewModel.onLoginClicked(databinding.mobileno.text.toString(), bluetoothAdapter!!.address)
    }

    fun createProgressDialog() {

        progressDialog = progressDialog ?: ProgressDialog(this, R.style.ProgressBarTheme)

        progressDialog?.let {
            it.setProgressStyle(android.R.style.Widget_ProgressBar_Small)
            it.isIndeterminate = true
            it.setCanceledOnTouchOutside(false)
            it.setCancelable(false)
        }
    }
}