package com.moralis.web3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.parse.Parse
import com.parse.ParseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.walletconnect.Session
import org.walletconnect.nullOnThrow


typealias MoralisUser = ParseUser

class Moralis : MoralisWeb3() {

    private lateinit var mCallback: Session.Callback
    private lateinit var mActivityCallback: MoralisCallback

    companion object {
        private const val TAG = "Moralis"

        fun initialize(appId: String, serverURL: String, context: Context) {
            Parse.initialize(
                Parse.Configuration.Builder(context)
                    .applicationId("TlygdyM0oqw39Qej6J0lAOppcrNAe2sA1FfZijQQ") // if desired
                    .server("https://zda0u2csr0us.grandmoralis.com:2053/server")
                    .build()
            )
        }
    }

    /**
     * Signs in an existing user onto the Moralis Server.
     */
    fun signIn(
        context: Context,
        moralisAuthCallback: (result: MoralisUser?) -> Unit
    ) {
        MoralisApplication.resetSession()
        mCallback = object : Session.Callback {
            override fun onStatus(status: Session.Status) {
                when (status) {
                    Session.Status.Approved -> sessionApprovedSignIn(moralisAuthCallback)
                    Session.Status.Closed -> sessionClosed()
                    Session.Status.Connected -> {
                        requestConnectionToWallet(context)
                    }
                    Session.Status.Disconnected -> {
                        Log.e("+++", "Disconnected")
                        sessionClosed()
                    }
                    is Session.Status.Error -> {
                        Log.e("+++", "Error:" + status.throwable.localizedMessage)
                    }
                }
            }

            override fun onMethodCall(call: Session.MethodCall) {
                Log.d("+++", "onMethodCall: " + call.id())
            }
        }
        MoralisApplication.session.addCallback(mCallback)
    }

    /**
     * Creates a new user on the Moralis Server using a wallet as identity provider.
     */
    fun signUp(
        context: Context,
        moralisAuthCallback: (result: MoralisUser?) -> Unit
    ) {
        MoralisApplication.resetSession()
        mCallback = object : Session.Callback {
            override fun onStatus(status: Session.Status) {
                when (status) {
                    Session.Status.Approved -> sessionApprovedSignUp(moralisAuthCallback)
                    Session.Status.Closed -> sessionClosed()
                    Session.Status.Connected -> {
                        requestConnectionToWallet(context)
                    }
                    Session.Status.Disconnected -> {
                        Log.e("+++", "Disconnected")
                        sessionClosed()
                    }
                    is Session.Status.Error -> {
                        Log.e("+++", "Error:" + status.throwable.localizedMessage)
                    }
                }
            }

            override fun onMethodCall(call: Session.MethodCall) {
                Log.d("+++", "onMethodCall: " + call.id())
            }
        }
        MoralisApplication.session.addCallback(mCallback)
    }

    interface MoralisCallback {
        fun onStatus(status: MoralisStatus, accounts: List<String>?)
    }

    sealed class MoralisStatus {
        object Approved : MoralisStatus()
        object Disconnected : MoralisStatus()
        object Closed : MoralisStatus()
        data class Error(val throwable: Throwable) : MoralisStatus()
    }

    fun logOut() {
        MoralisApplication.session.kill()
        MoralisUser.logOut()
    }

    private fun signUpToMoralis(moralisAuthCallback: (result: MoralisUser?) -> Unit) {
        val user = MoralisUser()
        // We use the wallet ID as username
        user.username = MoralisApplication.session.approvedAccounts()?.first()
        user.setPassword("pass")
        user.signUpInBackground { e ->
            if (e == null) {
                Log.d(TAG, "authenticateToMoralis() ALL OK")
                // Hooray! Let them use the app now.
                moralisAuthCallback.invoke(user)
            } else {
                Log.e(TAG, "failed to login: " + e.message)
                moralisAuthCallback.invoke(user)
                // Sign up didn't succeed. Look at the ParseException
                // to figure out what went wrong
            }
        }
    }

    private fun signInToMoralis(moralisAuthCallback: (result: MoralisUser?) -> Unit) {
        ParseUser.logInInBackground(MoralisApplication.session.approvedAccounts()?.first(), "pass") { user, e ->
            if (user != null) {
                // Hooray! The user is logged in.
                moralisAuthCallback.invoke(user)
            } else {
                Log.e(TAG, "failed to login: " + e.message)
                moralisAuthCallback.invoke(user)
                // Signup failed. Look at the ParseException to see what happened.
            }
        }
    }

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private fun sessionApprovedSignUp(moralisAuthCallback: (result: MoralisUser?) -> Unit) {
        uiScope.launch {
            signUpToMoralis(moralisAuthCallback)
            Log.d("+++", "Connected:  ${MoralisApplication.session.approvedAccounts()}")
        }
    }

    private fun sessionApprovedSignIn(moralisAuthCallback: (result: MoralisUser?) -> Unit) {
        uiScope.launch {
            signInToMoralis(moralisAuthCallback)
            Log.d("+++", "Connected:  ${MoralisApplication.session.approvedAccounts()}")
        }
    }

    private fun sessionClosed() {
        uiScope.launch {
            Log.d("+++", "Disconnected")
            mActivityCallback.onStatus(MoralisStatus.Closed, null)
        }
    }

    private fun requestConnectionToWallet(context: Context) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(MoralisApplication.config.toWCUri())
        context.startActivity(i)
    }

    fun onStart(callback: MoralisCallback) {
        mActivityCallback = callback
        initialSetup()
    }

    private fun initialSetup() {
        // if Application.session is not initialized then return
        val session = nullOnThrow { MoralisApplication.session } ?: return
        session.addCallback(mCallback)
        mActivityCallback.onStatus(MoralisStatus.Approved, session.approvedAccounts())
    }

    fun onDestroy() {
        MoralisApplication.session.removeCallback(mCallback)
    }

    fun getCurrentUser(): MoralisUser? {
        return MoralisUser.getCurrentUser()
    }

}