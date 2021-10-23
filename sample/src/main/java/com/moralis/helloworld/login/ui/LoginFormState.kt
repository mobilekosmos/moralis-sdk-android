package com.moralis.helloworld.login.ui

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
//    val running: Boolean = false,
    val isDataValid: Boolean = false
)