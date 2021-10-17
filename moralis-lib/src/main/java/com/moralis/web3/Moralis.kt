package com.moralis.web3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.parse.*
import com.parse.boltsinternal.Continuation
import com.parse.boltsinternal.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.walletconnect.Session
import org.walletconnect.nullOnThrow

typealias User = ParseUser
typealias Query = ParseQuery<*>

open class Moralis {

    companion object {
        private const val TAG = "Moralis"

        private lateinit var mCallback: Session.Callback
        private lateinit var mActivityCallback: MoralisCallback
        private val uiScope = CoroutineScope(Dispatchers.Main)
        private var mTxRequest: Long? = null

        fun start(appId: String, serverURL: String, applicationContext: Context) {
            initializeParse(appId, serverURL, applicationContext)
        }

        private fun initializeParse(appId: String, serverURL: String, applicationContext: Context) {
            Parse.initialize(
                Parse.Configuration.Builder(applicationContext)
                    .applicationId(appId)
                    .server(serverURL)
                    .build()
            )
        }

        /**
         * Signs in or up a user onto the Moralis Server.
         */
        fun authenticate(
            context: Context,
            signingMessage: String?,
            authenticationType: MoralisAuthentication = MoralisAuthentication.Ethereum,
            supportedWallets: Array<String> = emptyArray(),
            chainId: Int? = 1,
            moralisAuthCallback: (user: User?) -> Unit,
        ) {
            // TODO: use chainId and supportedWallets
            when (authenticationType) {
                MoralisAuthentication.Polkadot -> MoralisPolkadot.authenticate()
                MoralisAuthentication.Elrond -> MoralisElrond.authenticate()
                MoralisAuthentication.Ethereum -> authenticate(
                    signingMessage,
                    context,
                    supportedWallets,
                    chainId,
                    moralisAuthCallback
                )
            }
        }

        private fun authenticate(
            signingMessage: String?,
            context: Context,
            supportedWallets: Array<String>,
            chainId: Int?,
            moralisAuthCallback: (user: User?) -> Unit
        ) {
            // Starts a new connection to the bridge server and waits for a wallet to connect.
            MoralisApplication.resetSession(supportedWallets, chainId)

            // If a custom signing message for the wallet signature prompt was not provided use
            // a default one.
            val data = signingMessage ?: getSigningData()

            mCallback = object : Session.Callback {
                override fun onStatus(status: Session.Status) {
                    when (status) {
                        Session.Status.Approved -> handleSessionApproved(
                            moralisAuthCallback,
                            context,
                            data
                        )
                        Session.Status.Closed -> {
                            Log.e(TAG, "onStatus Session Closed")
                            handleSessionClosed()
                        }
                        Session.Status.Connected -> {
                            requestConnectionToWallet(context)
                        }
                        Session.Status.Disconnected -> {
                            Log.e(TAG, "onStatus Session Disconnected")
                            handleSessionClosed()
                        }
                        is Session.Status.Error -> {
                            Log.e(TAG, "onStatus Error:" + status.throwable.localizedMessage)
                            // TODO
                        }
                    }
                }

                override fun onMethodCall(call: Session.MethodCall) {
                    Log.d(TAG, "onMethodCall id: ${call.id()}")
                    when (call) {
                        is Session.MethodCall.SessionUpdate -> {
                            val newAccountAddress = call.params.accounts?.first()
                            Log.d(TAG, "SessionUpdate account: $newAccountAddress")
                        }
                        else -> {
                            // Ignore for now. TODO
                        }
                    }
                }
            }
            MoralisApplication.session.addCallback(mCallback)
        }

        // TODO: test and search for alternative background calls for cleaner code.
        fun link(
            account: String,
            signingMessage: String,
            moralisAuthCallback: (user: User?) -> Unit
        ) {

            val ethAddress = account.lowercase()
            // Search if the address already exists on the server.
            val EthAddress = ParseObject.create("_EthAddress")
            val query = ParseQuery(EthAddress.javaClass)
            val ethAddressRecord = query.get(ethAddress)
            // get current user
            val user = User.getCurrentUser()
            if (ethAddressRecord != null) {

                val id = System.currentTimeMillis()
                // We must explicitly specify the parameters names otherwise the compiler for some
                // reason doesn't respect the order of passed parameters and may link address with message
                // and message with address.
                // TODO: for now we sent the address as message and the message as address,
                // it's strange but sending the parameters in the "right way" doesn't work,
                // neither with Metamask nor with TrustWallet.
                val signMessage = Session.MethodCall.PersonalSignMessage(
                    id = id,
                    message = ethAddress,
                    address = signingMessage
                )
                // TODO: maybe use Sign Typed Data v4 instead?
                MoralisApplication.session.performMethodCall(signMessage) {
                    handleSignLinkResponse(
                        it,
                        ethAddress,
                        signingMessage,
                        user,
                        moralisAuthCallback
                    )
                }
                this.mTxRequest = id
            } else {
                saveUser(user, ethAddress, moralisAuthCallback)
            }
        }

        // TODO: test and search for alternative background calls for cleaner code.
        fun unlink(
            account: String,
            moralisAuthCallback: (user: User?) -> Unit
        ) {
            val accountsLower = account.lowercase();
            val EthAddress = ParseObject.create("_EthAddress")
            val query = ParseQuery(EthAddress.javaClass)
            val ethAddressRecord = query.get(accountsLower)
            ethAddressRecord.deleteInBackground() {
                val user = User.getCurrentUser()
                val accounts = (user.get("accounts") as Array<*>)
                val nextAccounts = accounts.filter { it != accountsLower }
                user?.put("accounts", nextAccounts)
                user?.put("ethAddress", nextAccounts.first().toString())
                val parseUserTask = user.unlinkFromInBackground("moralisEth")
                parseUserTask.continueWith {
                    user.saveInBackground {
                        // TODO: handle exceptions
                        Log.d(TAG, "User unlinked.")
                        moralisAuthCallback.invoke(user)
                    }
                }
            }
        }

        private fun handleSignLinkResponse(
            response: Session.MethodCall.Response,
            ethAddress: String,
            signingMessage: String,
            user: ParseUser,
            moralisAuthCallback: (user: User?) -> Unit
        ) {
            uiScope.launch {
                val signature = ((response.result as? String) ?: "Unknown response")

                val authData = mapOf(
                    "id" to ethAddress,
                    "signature" to signature,
                    "data" to signingMessage
                )


                val parseUserTask = user.linkWithInBackground("moralisEth", authData)
                parseUserTask.continueWith {
                    saveUser(user, ethAddress, moralisAuthCallback)
                }
            }
        }

        private fun saveUser(
            user: ParseUser,
            ethAddress: String,
            moralisAuthCallback: (user: User?) -> Unit
        ) {
            // TODO: handle exceptions
            user.addAllUnique("accounts", mutableListOf(ethAddress))
            user.put("ethAddress", ethAddress);
            user.saveInBackground {
                // TODO: handle exceptions
                Log.d(TAG, "user logged in.")
                moralisAuthCallback.invoke(user)
            }
        }

        fun onDestroy() {
            // If session is not initialized this method may be called anyways, so return.
            val session = nullOnThrow { MoralisApplication.session } ?: return

            session.removeCallback(mCallback)
        }

        /**
         * Sends an intent to the OS with the intention of opening a wallet that can handle the
         * authentication.
         */
        private fun requestConnectionToWallet(context: Context) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(MoralisApplication.config.toWCUri())
            context.startActivity(i)
        }

        /**
         * Starts the signing up process after a wallet approved a connection to it.
         */
        private fun handleSessionApproved(
            moralisAuthCallback: (result: User?) -> Unit,
            context: Context,
            signingMessage: String
        ) {
            uiScope.launch {
                signUpToMoralis(moralisAuthCallback, context, signingMessage)
                Log.d(TAG, "Connected:  ${MoralisApplication.session.approvedAccounts()}")
            }
        }

        private fun handleSessionClosed() {
            MoralisApplication.session.removeCallback(mCallback)
            uiScope.launch {
                mActivityCallback.onDisconnect()
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

            // We must explicitly specify the parameters names otherwise the compiler for some
            // reason doesn't respect the order of passed parameters and may link address with message
            // and message with address.
            // TODO: for now we sent the address as message and the message as address,
            // it's strange but sending the parameters in the "right way" doesn't work,
            // neither with Metamask nor with TrustWallet.
            val signMessage = Session.MethodCall.PersonalSignMessage(
                id = id,
                message = ethAddress,
                address = signingMessage
            )
            // TODO: maybe use Sign Typed Data v4 instead
            MoralisApplication.session.performMethodCall(signMessage) {
                handleSignAuthenticationResponse(
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

        private fun handleSignAuthenticationResponse(
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

                    val authData = mapOf(
                        "id" to ethAddress,
                        "signature" to signature,
                        "data" to signingMessage
                    )

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
                            // For new users, the username will be a randomly generated alphanumeric string and the email
                            // property will not exist. This can be set or changed by the app.

                            Log.d(TAG, "call saveInBackground")
                            user?.saveInBackground {
                                // TODO: handle exceptions
                                Log.d(TAG, "user logged in.")
                                moralisAuthCallback.invoke(user)
                            }
                            return null
                        }
                    })
                }
            }
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
                mActivityCallback.onConnect(session.approvedAccounts())
            }
        }

        fun logOut() {
            // Usually the user gets cached, this we could have a user logged-in and this method being
            // called without session. We simply ignore the session and log out the user.
            nullOnThrow { MoralisApplication.session }?.kill()
            User.logOut()
        }

        fun getSigningData(): String {
            return "Authentication Interface"
        }

        fun handleMoralisError(err: Errors) {
            when (err) {
                Errors.INVALID_SESSION_TOKEN -> {
                    logOut()
                }
                // TODO: Other Moralis API errors that you want to explicitly handle
            }
        }
    }

    interface MoralisCallback {
        fun onConnect(accounts: List<String>?)
        fun onDisconnect()

        fun onAccountsChanged(newAccountAddress: String) { /* Default implementation. */
        }

        fun onChainIdChanged(newChainId: Long) { /* Default implementation. */
        }

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

    enum class Errors {
        INVALID_SESSION_TOKEN
    }
}