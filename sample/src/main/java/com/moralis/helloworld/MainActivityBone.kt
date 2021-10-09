package com.moralis.helloworld

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.moralis.web3.Moralis

/**
 * This is a simple sample without UI interaction.
 * For a more elaborated sample with UI check MainActivity.
 */
class MainActivityBone : Activity(), Moralis.MoralisCallback {

    companion object {
        private const val TAG = "MainActivityBone"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty)

        Moralis.initialize(
            "TlygdyM0oqw39Qej6J0lAOppcrNAe2sA1FfZijQQ",
            "https://zda0u2csr0us.grandmoralis.com:2053/server", applicationContext
        )

        Moralis.authenticate(this, "Authentication") {
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
}