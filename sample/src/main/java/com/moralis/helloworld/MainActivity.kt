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

/**
 * This is a more elaborated example with more UI elements.
 */
class MainActivity : Activity(), Moralis.MoralisCallback {

    private val mUiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mMainBinding: MainBinding
    private val TAG = "MainActivity"

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

            // TODO: think about best signingMessage.
            Moralis.authenticate(this, "Wallet Authentication Interface") {
                if (it != null && it.isNew) {
                    Toast.makeText(
                        this@MainActivity,
                        "User logged in! Username: " + it.username,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "User not logged in! Maybe user already exists.",
                        Toast.LENGTH_LONG
                    ).show()
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
        mUiScope.launch {
            mMainBinding.textView.text = "Connected: $accounts"
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