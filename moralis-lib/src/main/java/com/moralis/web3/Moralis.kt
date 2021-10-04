package com.moralis.web3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.parse.Parse
import com.parse.ParseACL
import com.parse.ParseUser
import com.parse.boltsinternal.Continuation
import com.parse.boltsinternal.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.walletconnect.Session
import org.walletconnect.nullOnThrow

typealias User = ParseUser

open class Moralis {

    companion object {
        private const val TAG = "Moralis"
        private lateinit var mCallback: Session.Callback
        private val uiScope = CoroutineScope(Dispatchers.Main)
        private lateinit var mActivityCallback: MoralisCallback
        private var mTxRequest: Long? = null

        fun initialize(appId: String, serverURL: String, applicationContext: Context) {
            Parse.initialize(
                Parse.Configuration.Builder(applicationContext)
                    .applicationId("TlygdyM0oqw39Qej6J0lAOppcrNAe2sA1FfZijQQ") // if desired
                    .server("https://zda0u2csr0us.grandmoralis.com:2053/server")
                    .build()
            )
        }

        /**
         * Signs in an existing user onto the Moralis Server.
         */
        fun authenticate(
            context: Context,
            signingMessage: String?,
            authenticationType: MoralisAuthentication = MoralisAuthentication.Ethereum,
            moralisAuthCallback: (user: User?) -> Unit,
        ) {
            MoralisApplication.resetSession()

            when (authenticationType) {
                // TODO
                MoralisAuthentication.Polkadot -> return MoralisPolkadot.authenticate()
                MoralisAuthentication.Elrond -> return MoralisElrond.authenticate()
            }

            val data = signingMessage ?: getSigningData()

            mCallback = object : Session.Callback {
                override fun onStatus(status: Session.Status) {
                    when (status) {
                        Session.Status.Approved -> sessionApprovedSignUp(
                            moralisAuthCallback,
                            context,
                            data
                        )
                        Session.Status.Closed -> sessionClosed()
                        Session.Status.Connected -> {
                            requestConnectionToWallet(context)
                        }
                        Session.Status.Disconnected -> {
                            Log.e(TAG, "onStatus Disconnected")
                            sessionClosed()
                        }
                        is Session.Status.Error -> {
                            Log.e(TAG, "onStatus Error:" + status.throwable.localizedMessage)
                        }
                    }
                }

                override fun onMethodCall(call: Session.MethodCall) {
                    Log.d(TAG, "onMethodCall: " + call.id())
                }
            }
            MoralisApplication.session.addCallback(mCallback)
        }

        fun onDestroy() {
            MoralisApplication.session.removeCallback(mCallback)
        }

        private fun requestConnectionToWallet(context: Context) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(MoralisApplication.config.toWCUri())
            context.startActivity(i)
        }

        private fun sessionApprovedSignUp(
            moralisAuthCallback: (result: User?) -> Unit,
            context: Context,
            signingMessage: String
        ) {
            uiScope.launch {
                signUpToMoralis(moralisAuthCallback, context, signingMessage)
                Log.d(TAG, "Connected:  ${MoralisApplication.session.approvedAccounts()}")
            }
        }

        private fun sessionClosed() {
            MoralisApplication.session.removeCallback(mCallback)
            uiScope.launch {
                Log.d(TAG, "Disconnected")
                mActivityCallback.onStatus(MoralisStatus.Closed, null)
            }
        }

        private fun signUpToMoralis(
            moralisAuthCallback: (result: User?) -> Unit,
            context: Context,
            signingMessage: String
        ) {
            val accounts = MoralisApplication.session.approvedAccounts() ?: return
            val accountsLowercase = accounts.map { it.lowercase() }
            val ethAddress = accountsLowercase.first()
            val id = System.currentTimeMillis()
            Log.d(TAG, "accountsLowercase: $accountsLowercase")
            Log.d(TAG, "ethAddress: $ethAddress")
            Log.d(TAG, "id: $id")
            Log.d(TAG, "signingMessage: $signingMessage")

            // TODO: maybe use Sign Typed Data v4 instead
            MoralisApplication.session.performMethodCall(
                Session.MethodCall.PersonalSignMessage(
                    id,
                    signingMessage,
                    ethAddress
                )
            ) {
                handleResponse(
                    it,
                    ethAddress,
                    signingMessage,
                    accountsLowercase,
                    moralisAuthCallback
                )
            }
            this.mTxRequest = id
            // TODO: send intent anyways but with FLAG to avoid restart in case of already being
            // visible.
//            navigateToWallet(context)
        }

        private fun handleResponse(
            response: Session.MethodCall.Response,
            ethAddress: String,
            signingMessage: String,
            accountsLowercase: List<String>,
            moralisAuthCallback: (result: User?) -> Unit
        ) {
            Log.d(TAG, "handleResponse, response: ${response.id} mTxRequest=$mTxRequest")
            if (response.id == mTxRequest) {
                Log.d(TAG, "response.id == mTxRequest")
                mTxRequest = null
                uiScope.launch {
                    val signature = ((response.result as? String) ?: "Unknown response")

                    val authData = mapOf("id" to ethAddress, "signature" to signature, "data" to signingMessage)

                    val parseUserTask = User.logInWithInBackground("moralisEth", authData)
                    parseUserTask.continueWith(object : Continuation<User?, Void?> {
                        override fun then(task: Task<User?>): Void? {
                            if (task.isCancelled()) {
                                Log.d(TAG, "then: task.isCancelled()")
//                                // TODO showError()
                                return null
                            }
                            if (task.isFaulted()) {
                                Log.d(TAG, "then: ask.isFaulted()")
//                                // TODO showError()
                                return null
                            }
                            val user: User? = task.result
                            user?.acl = ParseACL(user);
                            // TODO: if (!user) throw new Error('Could not get user');
                            user?.addAllUnique("accounts", accountsLowercase)
                            user?.put("ethAddress", ethAddress);
                            Log.d(TAG, "call saveInBackground")
                            user?.saveInBackground {
                                Log.d(TAG, "user logged in.")
                                moralisAuthCallback.invoke(user)
                            }
                            return null
                        }
                    })
                }
            }
        }

        private fun navigateToWallet(context: Context) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("wc:")
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
            if (session.approvedAccounts() != null) {
                mActivityCallback.onStatus(MoralisStatus.Approved, session.approvedAccounts())
            }
        }

        fun logOut() {
            MoralisApplication.session.kill()
            User.logOut()
        }

        fun getSigningData(): String {
            return "Authentication Interface"
        }

        private fun isDotAuth(options: String): Boolean {
            return when (options) {
                "dot", "polkadot", "kusama" -> {
                    true
                }
                else -> {
                    false
                }
            }
        }

        private fun isElrondAuth(options: String): Boolean {
            return when (options) {
                "erd", "elrond" -> {
                    true
                }
                else -> {
                    false
                }
            }
        }
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

    sealed class MoralisAuthentication {
        object Ethereum : MoralisAuthentication()
        object Polkadot : MoralisAuthentication()
        object Elrond : MoralisAuthentication()
    }

    enum class EthereumEvents {
        CONNECT, DISCONNECT, ACCOUNTS_CHANGED, CHAIN_CHANGED
    }
}