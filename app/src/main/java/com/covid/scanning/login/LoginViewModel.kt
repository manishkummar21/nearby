package com.covid.scanning.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.covid.scanning.ScreenState

class LoginViewModel(private val loginInteractor: LoginInteractor) : ViewModel(),
    LoginInteractor.OnLoginFinishedListener {

    val _loginState: MutableLiveData<ScreenState<LoginState>> = MutableLiveData()

    fun onLoginClicked(username: String, address: String) {
        _loginState.value = ScreenState.Loading
        loginInteractor.loginSignup(username, address, this)

    }

    override fun onError() {
        _loginState.value = ScreenState.Render(LoginState.Error)
    }

    override fun onSuccess() {
        _loginState.value = ScreenState.Render(LoginState.Success)
    }

    class LoginViewModelFactory(private val loginInteractor: LoginInteractor) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LoginViewModel(loginInteractor) as T
        }
    }

}