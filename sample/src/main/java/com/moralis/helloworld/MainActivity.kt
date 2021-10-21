package com.moralis.helloworld

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.moralis.helloworld.databinding.MainBinding
import com.moralis.web3.Moralis
import com.moralis.web3.MoralisApplication
import com.moralis.web3.MoralisUser
import com.moralis.web3.MoralisWeb3Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.walletconnect.nullOnThrow

/**
 * This is a more elaborated example with more UI elements.
 */
class MainActivity : Activity(), Moralis.MoralisAuthenticationCallback {

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

        MoralisUser.getCurrentUser()?.let {
            adaptUIAfterSessionApproved(it)
        }

        //val button = findViewById<Button>(R.id.login_button)
        mMainBinding.signUpButton.setOnClickListener {

            // TODO: think about best signingMessage. Get string from res.
            // Press sign to authenticate with your wallet.
            // Press the sign button to create a new account using the wallet as ID.
            Moralis.authenticate(
                this@MainActivity,
                "Press sign to authenticate with your wallet."
            ) {
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

        mMainBinding.transferButton.setOnClickListener {
            val transferObj = MoralisWeb3Transaction.TransferObject.TransferObjectNATIVE(
                amountToTransfer = "5.5",
                receiver = "0x24EdA4f7d0c466cc60302b9b5e9275544E5ba552"
            )
            val session = nullOnThrow { MoralisApplication.session }
            if (session == null) {
                Log.d(TAG, "Session expired!")
                Toast.makeText(this@MainActivity, "Session expired!", Toast.LENGTH_LONG).show()
                Moralis.authenticate(
                    this@MainActivity,
                    "Press sign to authenticate with your wallet."
                ) {
                    if (it != null) {
                        adaptUIAfterSessionApproved(it)
                        startTransfer(transferObj)
                    }
                }
            } else {
                startTransfer(transferObj)
            }
        }
    }

    private fun startTransfer(transferObj: MoralisWeb3Transaction.TransferObject.TransferObjectNATIVE) {
        MoralisWeb3Transaction.transfer(
            transferObj,
            this@MainActivity,
            object : MoralisWeb3Transaction.MoralisTransferCallback {
                override fun onError() {
                    Log.d(TAG, "onError")
                    //TODO("Not yet implemented")
                }

                override fun onResponse(result: Any?) {
                    Log.d(TAG, "onResponse")
                    //TODO("Not yet implemented")
                }

            })
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

    private fun adaptUIAfterSessionApproved(moralisUser: MoralisUser) {
        Log.d(TAG, "adaptUIAfterSessionApproved")
        mUiScope.launch {
            if (moralisUser.isNew) {
                Toast.makeText(this@MainActivity, "Welcome!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Welcome back!", Toast.LENGTH_SHORT).show()
            }
            val ethAddress = moralisUser.get("ethAddress")
            mMainBinding.textView.text =
                "Connected address:\n $ethAddress \n\n Username:\n ${moralisUser.username}"
            mMainBinding.textView.visibility = View.VISIBLE
            mMainBinding.signUpButton.visibility = View.GONE
            mMainBinding.logoutButton.visibility = View.VISIBLE
            mMainBinding.transferButton.visibility = View.VISIBLE
        }
    }

    private fun adaptUIAfterSessionClosed() {
        Log.d(TAG, "adaptUIAfterSessionClosed")
        mUiScope.launch {
            mMainBinding.textView.visibility = View.GONE
            mMainBinding.signUpButton.visibility = View.VISIBLE
            mMainBinding.logoutButton.visibility = View.GONE
            mMainBinding.transferButton.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        Moralis.onDestroy()
        super.onDestroy()
    }

    override fun onConnect(accounts: List<String>?) {
        Log.d(TAG, "onConnect")
        adaptUIAfterSessionApproved(accounts)
    }

    override fun onDisconnect() {
        Log.d(TAG, "onDisconnect")
        adaptUIAfterSessionClosed()
    }

    override fun onAccountsChanged(newAccountAddress: String) {
        Log.d(TAG, "onAccountsChanged")
        val linkNewAddress = false
        // TODO: here you could ask the user if she wants to link the new address
        // to the current account.
        if (linkNewAddress) {
            val signingMessage = "Press sign to authenticate with your wallet."
            Moralis.link(newAccountAddress, signingMessage) {
                Log.d(TAG, "New address linked to current user.")
            }
        }
    }

    /**
     * Sign-Up example.
     */
    fun signUp() {
        val user = MoralisUser()
        user.username = "username"
        user.setPassword("password")
        user.email = "username@moralismagician.com"
        user.signUpInBackground { e ->
            if (e == null) {
                Log.d(TAG, "authenticateToMoralis() ALL OK")
                // Hooray! Let them use the app now.
            } else {
                Log.e(TAG, "failed to login: " + e.message)
                // Sign up didn't succeed. Look at the ParseException
                // to figure out what went wrong
            }
        }
    }

//    is Moralis.MoralisStatus.Error -> {
//        Log.e(TAG, "Error:" + status.throwable.localizedMessage)
//    }
}