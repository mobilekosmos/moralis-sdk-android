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

class MainActivity : Activity(), Moralis.MoralisCallback {

    private val mUiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mMainBinding: MainBinding
    private val mMoralis: Moralis = Moralis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Programming is so 1.0 yet, if you for example then set the view listener using the binding
        // the listener won't work. But using setContentView(mMainBinding.root) instead has also the
        // issue that then you don't get the layout listed when pressing the left icon beside the class.
//        mMainBinding = MainBinding.inflate(layoutInflater)
//        setContentView(mMainBinding.root)

        setContentView(R.layout.main)
        mMainBinding = MainBinding.bind(findViewById(R.id.main_container))
    }

    override fun onStart() {
        super.onStart()
        mMoralis.onStart(this)

        //val button = findViewById<Button>(R.id.login_button)
        mMainBinding.signUpButton.setOnClickListener {
            mMoralis.signUp(this) {
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
            Toast.makeText(
                this@MainActivity,
                "Connecting to wallet, please wait...",
                Toast.LENGTH_SHORT
            ).show()
        }

        mMainBinding.signInButton.setOnClickListener {
            mMoralis.signIn(this) {
                if (it != null) {
                    Toast.makeText(
                        this@MainActivity,
                        "User logged in! Username: " + it.username,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error, user not logged in!", Toast.LENGTH_LONG)
                        .show()
                }
            }
            Toast.makeText(
                this@MainActivity,
                "Connecting to wallet, please wait...",
                Toast.LENGTH_SHORT
            ).show()
        }

        mMainBinding.logoutButton.setOnClickListener {
            mMoralis.logOut()
            adaptUIAfterSessionClosed()
            Toast.makeText(this@MainActivity, "Logged out", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStatus(status: Moralis.MoralisStatus, accounts: List<String>?) {
        when (status) {
            Moralis.MoralisStatus.Approved -> adaptUIAfterSessionApproved(accounts)
            Moralis.MoralisStatus.Closed -> adaptUIAfterSessionClosed()
            Moralis.MoralisStatus.Disconnected -> {
                Log.e("+++", "Disconnected")
            }
            is Moralis.MoralisStatus.Error -> {
                Log.e("+++", "Error:" + status.throwable.localizedMessage)
            }
        }
    }

    private fun adaptUIAfterSessionApproved(accounts: List<String>?) {
        mUiScope.launch {
            mMainBinding.textView.text = "Connected: $accounts"
            mMainBinding.textView.visibility = View.VISIBLE
            mMainBinding.signUpButton.visibility = View.GONE
            mMainBinding.signInButton.visibility = View.GONE
            mMainBinding.logoutButton.visibility = View.VISIBLE
//            mMainBinding.screenMainTxButton.visibility = View.VISIBLE
        }
    }

    private fun adaptUIAfterSessionClosed() {
        mUiScope.launch {
            mMainBinding.textView.visibility = View.GONE
            mMainBinding.signUpButton.visibility = View.VISIBLE
            mMainBinding.signInButton.visibility = View.VISIBLE
            mMainBinding.logoutButton.visibility = View.GONE
//            mMainBinding.screenMainTxButton.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        mMoralis.onDestroy()
        super.onDestroy()
    }
}