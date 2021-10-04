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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty)

        Moralis.initialize(
            "TlygdyM0oqw39Qej6J0lAOppcrNAe2sA1FfZijQQ",
            "https://zda0u2csr0us.grandmoralis.com:2053/server", applicationContext
        )

        Moralis.authenticate(this, "Authentication") {
            Log.d("Moralis", "User: " + it?.username)
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

    override fun onStatus(status: Moralis.MoralisStatus, accounts: List<String>?) {
        when (status) {
            Moralis.MoralisStatus.Approved -> { Log.d("Moralis", "onStatus Approved")}
            Moralis.MoralisStatus.Closed -> { Log.d("Moralis", "onStatus Closed")}
            Moralis.MoralisStatus.Disconnected -> { Log.e("Moralis", "onStatus Disconnected") }
            is Moralis.MoralisStatus.Error -> {
                Log.e("Moralis", "onStatus Error:" + status.throwable.localizedMessage)
            }
        }
    }
}