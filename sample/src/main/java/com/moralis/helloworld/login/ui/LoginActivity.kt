package com.moralis.helloworld.login.ui

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.moralis.helloworld.R
import com.moralis.helloworld.databinding.ActivityLoginBinding
import com.moralis.web3.MoralisUser

class LoginActivity : AppCompatActivity() {

    private lateinit var mLoginViewModel: LoginViewModel
    private lateinit var mBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        mBinding = ActivityLoginBinding.bind(findViewById(R.id.login_container))

        val username = mBinding.username
        val password = mBinding.password
        val login = mBinding.login
        val signUp = mBinding.signUp
        val loading = mBinding.loading

        mLoginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        mLoginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid
            signUp?.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        mLoginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                setResult(Activity.RESULT_OK)
                // Complete and destroy login activity once successful.
                finish()
            }
        })

        mLoginViewModel.dataLoading.observe(this@LoginActivity, Observer {
            if (it) {
                showProgress(loading)
            } else {
                hideProgress(loading)
            }
        })

        username.afterTextChanged {
            mLoginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                mLoginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        mLoginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                mLoginViewModel.login(username.text.toString(), password.text.toString())
            }

            signUp?.setOnClickListener {
                val user = MoralisUser().apply {
                    this.username = username.text.toString()
                    this.setPassword(password.text.toString())
                    this.email = username.text.toString()
                }
                mLoginViewModel.signUp(user)
            }
        }
    }

    private fun hideProgress(loading: ProgressBar) {
        loading.visibility = View.GONE
        setViewsVisiblity(true)
    }

    private fun setViewsVisiblity(enabled: Boolean) {
        mBinding.login.isEnabled = enabled
        mBinding.signUp?.isEnabled = enabled
        mBinding.username.isEnabled = enabled
        mBinding.password.isEnabled = enabled
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(errorMessage: String) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun showProgress(loadingProgressBar: ProgressBar) {
        loadingProgressBar.visibility = View.VISIBLE
        setViewsVisiblity(false)
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}