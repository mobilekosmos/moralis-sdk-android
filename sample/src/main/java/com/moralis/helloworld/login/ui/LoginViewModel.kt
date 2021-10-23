package com.moralis.helloworld.login.ui

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moralis.helloworld.R
import com.moralis.helloworld.login.data.LoginRepository
import com.moralis.helloworld.login.data.Result
import com.moralis.web3.MoralisUser
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    fun login(username: String, password: String) {
        if (_dataLoading.value == true) {
            return
        }
        _dataLoading.value = true
        // Create a new coroutine on the UI thread.
        viewModelScope.launch {
            val result = loginRepository.login(username, password)

            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(displayName = result.data.username))
            } else {
                _loginResult.value = LoginResult(error = result.toString())
            }
            _dataLoading.value = false
        }
    }

    fun signUp(user: MoralisUser) {
        if (_dataLoading.value == true) {
            return
        }
        _dataLoading.value = true
        // Create a new coroutine on the UI thread.
        viewModelScope.launch {
            val result = loginRepository.signUp(user)

            if (result is Result.Success) {
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(displayName = result.data.username))
            } else {
                _loginResult.value = LoginResult(error = result.toString())
            }
            _dataLoading.value = false
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}