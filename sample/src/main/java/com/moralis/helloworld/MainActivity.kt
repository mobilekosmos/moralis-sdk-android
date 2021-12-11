package com.moralis.helloworld

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.moralis.helloworld.databinding.ActivityMainBinding
import com.moralis.helloworld.login.ui.LoginActivity
import com.moralis.web3.Moralis
import com.moralis.web3.MoralisApplication
import com.moralis.web3.MoralisUser
import com.moralis.web3.MoralisWeb3Transaction
import com.moralis.web3.api.MoralisWeb3ApiAccount
import com.moralis.web3.api.data.MoralisWeb3APIResult
import com.moralis.web3.restapisdk.api.AccountApi
import com.moralis.web3.restapisdk.auth.ApiKeyAuth
import com.moralis.web3.restapisdk.infrastructure.ApiClient
import kotlinx.coroutines.*
import org.walletconnect.nullOnThrow

/**
 * This is a more elaborated example with more UI elements.
 */
class MainActivity : Activity(), Moralis.MoralisAuthenticationCallback {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val mUiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        // Programming is so 1.0 yet, if you for example then set the view listener using the binding
        // the listener won't work. But using setContentView(mMainBinding.root) instead has also the
        // issue that then you don't get the layout listed when pressing the left icon beside the class.
//        mMainBinding = MainBinding.inflate(layoutInflater)
//        setContentView(mMainBinding.root)

        setContentView(R.layout.activity_main)
        mMainBinding = ActivityMainBinding.bind(findViewById(R.id.main_container))
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
        Moralis.onStart(this)

        MoralisUser.getCurrentUser()?.let {
            setLoggedInUI(it)
        }

        //val button = findViewById<Button>(R.id.login_button)
        mMainBinding.connectWithWalletButton.setOnClickListener {

            // TODO: think about best signingMessage. Get string from res.
            // Press sign to authenticate with your wallet.
            // Press the sign button to create a new account using the wallet as ID.
            Moralis.authenticate(
                "Press sign to authenticate with your wallet.",
            ) { user ->
                if (user != null) {
                    adaptUIAfterSessionApproved(user)
                }
            }
        }

        mMainBinding.signUpEmailButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        mMainBinding.linkWalletButton.setOnClickListener {
            Moralis.authenticate(
                "Press sign to authenticate with your wallet."
            ) {
                if (it != null) {
                    adaptUIAfterWalletLinked(it)
                }
            }
        }

        mMainBinding.unlinkWalletButton.setOnClickListener {
            MoralisUser.getCurrentUser()?.let { currentUser ->
                val ethAddress = currentUser.get("ethAddress")?.toString() ?: return@let
                Moralis.unlinkWallet(ethAddress) { newMoralisUser ->
                    if (newMoralisUser != null) {
                        adaptUIAfterWalletUnlinked(newMoralisUser)
                    }
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
                amountToTransfer = "1",
                receiver = "0x24EdA4f7d0c466cc60302b9b5e9275544E5ba552"
            )

            val session = nullOnThrow { MoralisApplication.session }
            if (session == null) {
                Log.d(TAG, "Session expired!")
                Toast.makeText(this@MainActivity, "Session expired!", Toast.LENGTH_LONG).show()
                Moralis.authenticate(
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

        mMainBinding.transferErc20Button.setOnClickListener {
            // Using Ropsen Testnet and based on https://ethereum.stackexchange.com/questions/72388/does-rinkeby-have-a-faucet-where-i-can-fill-a-wallet-with-dai/80204
            val transferObj = MoralisWeb3Transaction.TransferObject.TransferObjectERC20(
                amountToTransfer = "0.0000001",
                receiver = "0x24EdA4f7d0c466cc60302b9b5e9275544E5ba552",
//                contractAddress = "0xdac17f958d2ee523a2206206994597c13d831ec7" // USDT
//                contractAddress = "0x6b175474e89094c44da98b954eedeac495271d0f" // DAI
                contractAddress = "0x1fe24f25b1cf609b9c4e7e12d802e3640dfa5e43" // CGG
            )

            val session = nullOnThrow { MoralisApplication.session }
            if (session == null) {
                Log.d(TAG, "Session expired!")
                Toast.makeText(this@MainActivity, "Session expired!", Toast.LENGTH_LONG).show()
                Moralis.authenticate(
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

        mMainBinding.getBalanceButton.setOnClickListener {
            MoralisUser.getCurrentUser()?.let {
                getNativeBalance(it.get("ethAddress").toString())
//                getTransactions(it.get("ethAddress").toString())
            }

        }
    }

    private fun startTransfer(transferObj: MoralisWeb3Transaction.TransferObject) {
        MoralisWeb3Transaction.transfer(
            transferObj,
            this@MainActivity,
            object : MoralisWeb3Transaction.MoralisTransferCallback {
                override fun onError(message: String) {
                    Log.e(TAG, "MoralisWeb3Transaction onError")
                    // TODO: call launch directly in the Moralis class?
                    mUiScope.launch {
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(result: Any?) {
                    Log.d(TAG, "MoralisWeb3Transaction onResponse")
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

    private fun setLoggedInUI(moralisUser: MoralisUser) {
        // TODO: create an API for getting the address without requiring parameter.
        var ethAddress = moralisUser.get("ethAddress")

        if (ethAddress == null) {
            ethAddress = "No wallet linked yet"
            mMainBinding.linkWalletButton.visibility = View.VISIBLE
            mMainBinding.unlinkWalletButton.visibility = View.GONE
            mMainBinding.getBalanceButton.visibility = View.GONE
            mMainBinding.transferButton.visibility = View.GONE
            mMainBinding.transferErc20Button.visibility = View.GONE
        } else {
            mMainBinding.linkWalletButton.visibility = View.GONE
            mMainBinding.unlinkWalletButton.visibility = View.VISIBLE
            mMainBinding.getBalanceButton.visibility = View.VISIBLE
            mMainBinding.transferButton.visibility = View.VISIBLE
            mMainBinding.transferErc20Button.visibility = View.VISIBLE
        }
        mMainBinding.textView.text =
            "Connected wallet:\n $ethAddress \n\n Username:\n ${moralisUser.username}"
        mMainBinding.textView.visibility = View.VISIBLE
        mMainBinding.connectWithWalletButton.visibility = View.GONE
        mMainBinding.logoutButton.visibility = View.VISIBLE
        mMainBinding.signUpEmailButton.visibility = View.GONE
    }

    private fun adaptUIAfterSessionApproved(moralisUser: MoralisUser) {
        Log.d(TAG, "adaptUIAfterSessionApproved")
        mUiScope.launch {
            if (moralisUser.isNew) {
                Toast.makeText(this@MainActivity, "Welcome!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Welcome back!", Toast.LENGTH_SHORT).show()
            }
            setLoggedInUI(moralisUser)
        }
    }

    private fun getTransactions(address: String) {
        mUiScope.launch {
            when (val result = MoralisWeb3ApiAccount.getTransactions(address)) {
                is MoralisWeb3APIResult.Success -> {
                    Log.d(TAG, "getTransactions: Success")
                    // TODO: implement
                }
                is MoralisWeb3APIResult.Error -> {
                    Log.d(TAG, "getTransactions error code: $result.errorCode")
                }
            }
        }
    }

    // Example how to fetch the Moralis REST API directly.
    // Dependent on moralis-web3api-client-kotlin-1.0.0.jar, moshi, okhttp and retrofit libs.
    // Never store your API_KEY on a client or/and unsecured.
    // You can get your API_KEY on the Moralis Admin console (https://admin.moralis.io/servers).
    private fun getNativeBalance(address: String) {
        val client = ApiClient()
        val apiKey = "TODO"
        val authorization = ApiKeyAuth("header", "X-API-Key", apiKey)
        client.addAuthorization("ApiKeyAuth", authorization)
        val service = client.createService(AccountApi::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getNativeBalance(address)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Balance: ${response.body()?.balance}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Error getting balance: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun adaptUIAfterWalletLinked(moralisUser: MoralisUser) {
        Log.d(TAG, "adaptUIAfterSessionApproved")
        mUiScope.launch {
            Toast.makeText(this@MainActivity, "Wallet linked", Toast.LENGTH_SHORT).show()
            setLoggedInUI(moralisUser)
        }
    }

    private fun adaptUIAfterWalletUnlinked(moralisUser: MoralisUser) {
        Log.d(TAG, "adaptUIAfterSessionApproved")
        mUiScope.launch {
            Toast.makeText(this@MainActivity, "Wallet unlinked", Toast.LENGTH_SHORT).show()
            setLoggedInUI(moralisUser)
        }
    }

    private fun adaptUIAfterSessionClosed() {
        Log.d(TAG, "adaptUIAfterSessionClosed")
        mUiScope.launch {
            mMainBinding.textView.visibility = View.GONE
            mMainBinding.connectWithWalletButton.visibility = View.VISIBLE
            mMainBinding.logoutButton.visibility = View.GONE
            mMainBinding.transferButton.visibility = View.GONE
            mMainBinding.transferErc20Button.visibility = View.GONE
            mMainBinding.getBalanceButton.visibility = View.GONE
            mMainBinding.unlinkWalletButton.visibility = View.GONE
            mMainBinding.signUpEmailButton.visibility = View.VISIBLE
            mMainBinding.linkWalletButton.visibility = View.GONE
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
            Moralis.linkWallet(newAccountAddress, signingMessage) {
                Log.d(TAG, "New address linked to current user.")
            }
        }
    }

    /**
     * Reset password example.
     */
    fun resetPassword() {
        MoralisUser.requestPasswordResetInBackground("email@example.com") {
            if (it == null) {
                Log.d(TAG, "resetPassword: no account associated with email.")
                // no account associated with email.
                return@requestPasswordResetInBackground
            }
            Log.d(TAG, "resetPassword error: ${it.message}")
        }
    }

//    is Moralis.MoralisStatus.Error -> {
//        Log.e(TAG, "Error:" + status.throwable.localizedMessage)
//    }
}