package com.moralis.helloworld

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.moralis.web3.Moralis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.moralis.helloworld.databinding.MainBinding
import com.moralis.web3.MoralisApplication
import com.moralis.web3.User

/**
 * This is a more elaborated example with more UI elements.
 */
class MainActivity : Activity(), Moralis.MoralisCallback {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val mUiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mMainBinding: MainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        // Programming is so 1.0 yet, if you for example then set the view listener using the binding
        // the listener won't work. But using setContentView(mMainBinding.root) instead has also the
        // issue that then you don't get the layout listed when pressing the left icon beside the class.
//        mMainBinding = MainBinding.inflate(layoutInflater)
//        setContentView(mMainBinding.root)

        setContentView(R.layout.main)
        mMainBinding = MainBinding.bind(findViewById(R.id.main_container))
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
        Moralis.onStart(this)

        //val button = findViewById<Button>(R.id.login_button)
        mMainBinding.signUpButton.setOnClickListener {

            // TODO: think about best signingMessage. Get string from res.
            // Press sign to authenticate with your wallet.
            // Press the sign button to create a new account using the wallet as ID.
            Moralis.authenticate(this, "Press") {
                if (it != null) {
                    adaptUIAfterSessionApproved(it)
                }
            }
        }

        mMainBinding.logoutButton.setOnClickListener {
            Moralis.logOut()
            adaptUIAfterSessionClosed()
            Toast.makeText(this@MainActivity, "Logged out", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStatus(status: Moralis.MoralisStatus, accounts: List<String>?) {
        Log.d(TAG, "onStatus ${status.toString()}")
        when (status) {
            Moralis.MoralisStatus.Approved -> adaptUIAfterSessionApproved(accounts)
            Moralis.MoralisStatus.Closed -> adaptUIAfterSessionClosed()
            Moralis.MoralisStatus.Disconnected -> {
                Log.e(TAG, "Disconnected")
            }
            is Moralis.MoralisStatus.Error -> {
                Log.e(TAG, "Error:" + status.throwable.localizedMessage)
            }
        }
    }

    private fun adaptUIAfterSessionApproved(accounts: List<String>?) {
        Log.d(TAG, "adaptUIAfterSessionApproved")
        // ignore for now TODO
        return
//        mUiScope.launch {
//            mMainBinding.textView.text = "Connected: $accounts"
//            mMainBinding.textView.visibility = View.VISIBLE
//            mMainBinding.signUpButton.visibility = View.GONE
//            mMainBinding.logoutButton.visibility = View.VISIBLE
////            mMainBinding.screenMainTxButton.visibility = View.VISIBLE
//        }
    }

    private fun adaptUIAfterSessionApproved(user: User) {
        Log.d(TAG, "adaptUIAfterSessionApproved")
        mUiScope.launch {
            if (user.isNew) {
                Toast.makeText(this@MainActivity, "Welcome!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Welcome back!", Toast.LENGTH_SHORT).show()
            }
            val ethAddress = user.get("ethAddress")
            mMainBinding.textView.text = "Connected address: $ethAddress"
            mMainBinding.textView.visibility = View.VISIBLE
            mMainBinding.signUpButton.visibility = View.GONE
            mMainBinding.logoutButton.visibility = View.VISIBLE
//            mMainBinding.screenMainTxButton.visibility = View.VISIBLE
        }
    }

    private fun adaptUIAfterSessionClosed() {
        Log.d(TAG, "adaptUIAfterSessionClosed")
        mUiScope.launch {
            mMainBinding.textView.visibility = View.GONE
            mMainBinding.signUpButton.visibility = View.VISIBLE
            mMainBinding.logoutButton.visibility = View.GONE
//            mMainBinding.screenMainTxButton.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        Moralis.onDestroy()
        super.onDestroy()
    }
}