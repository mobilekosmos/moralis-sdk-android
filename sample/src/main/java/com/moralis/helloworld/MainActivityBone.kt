package com.moralis.helloworld

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.moralis.web3.Moralis
import com.moralis.web3.MoralisUser

/**
 * This is a simple sample without UI interaction.
 * For a more elaborated sample with UI check MainActivity.
 */
class MainActivityBone : Activity(), Moralis.MoralisAuthenticationCallback {

    companion object {
        private const val TAG = "MainActivityBone"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_bone)

        Moralis.start(
            "TlygdyM0oqw39Qej6J0lAOppcrNAe2sA1FfZijQQ",
            "https://zda0u2csr0us.grandmoralis.com:2053/server"
        )

        Moralis.authenticate() {
            Log.d(TAG, "User: " + it?.username)
        }

        Moralis.logOut()
    }

    override fun onStart() {
        super.onStart()
        Moralis.onStart(this)
    }

    override fun onDestroy() {
        Moralis.onDestroy()
        super.onDestroy()
    }

    // TODO Log.e("Moralis", "onStatus Error:" + status.throwable.localizedMessage)

    override fun onConnect(accounts: List<String>?) {
        Log.d(TAG, "onConnect")
        TODO("Not yet implemented")
    }

    override fun onDisconnect() {
        Log.d(TAG, "onDisconnect")
        TODO("Not yet implemented")
    }

    /**
     * SignIn example.
     */
    fun signIn() {
        MoralisUser.logInInBackground("username", "pass") { user, e ->
            if (user != null) {
                Log.d(TAG, "signIn() ALL OK")
                // Hooray! The user is logged in.
            } else {
                Log.e(TAG, "failed to login: " + e.message)
                // SignIn failed. Look at the ParseException to see what happened.
            }
        }
    }

    /**
     * Sign-Up example.
     */
    fun signUp() {
        val user = MoralisUser().apply {
            username = "username"
            setPassword("password")
            email = "username@moralismagician.com"

            signUpInBackground { e ->
                if (e == null) {
                    Log.d(TAG, "signUp() ALL OK")
                    // Hooray! Let them use the app now.
                } else {
                    Log.e(TAG, "failed to login: " + e.message)
                    // Sign-Up failed. Look at the ParseException to figure out what went wrong.
                }
            }
        }
    }
}