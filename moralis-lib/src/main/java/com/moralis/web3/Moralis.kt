package com.moralis.web3

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.util.Log
import com.parse.*
import com.parse.ParseException.INVALID_SESSION_TOKEN
import com.parse.ParseQuery
import com.parse.boltsinternal.Continuation
import com.parse.boltsinternal.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.walletconnect.Session
import org.walletconnect.nullOnThrow


typealias MoralisUser = ParseUser
typealias MoralisQuery = ParseQuery<*>
typealias MoralisObject = ParseObject

open class Moralis {

    companion object {
        private const val TAG = "Moralis"

        private lateinit var mCallback: Session.Callback
        private lateinit var mActivityAuthenticationCallback: MoralisAuthenticationCallback
        private val mUiScope = CoroutineScope(Dispatchers.Main)
        private var mTxRequest: Long? = null

        /**
         * Initializes the SDK.
         *
         * @param moralisServerAppId The Application ID of your Moralis Server.
         * @param moralisServerURL The Server URL of your Moralis Server.
         */
        fun start(moralisServerAppId: String, moralisServerURL: String) {
            initializeParse(moralisServerAppId, moralisServerURL, MoralisApplication.getContext())
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
         *
         * @param signingMessage (Optional) The signing message that the Wallet will show to the user. Optional, if not given a standard message
         * defined by Moralis will be used.
         * @param authenticationType(Optional) If not given the default authentication is on Ethereum. See {@link #Moralis.MoralisAuthentication}.
         * @param supportedWallets (Optional) Only supported on iOS. The list fo wallets to offer the user to select from.
         * @param chainId (Optional) You might want to specify the chain id that WalletConnect will use by default. Currently the this may be ignored
         * by the selected wallet.
         * @param moralisAuthCallback The lambda that receives a MoralisUser object upon successful authentication.
         */
        fun authenticate(
            signingMessage: String = getSigningData(),
            authenticationType: MoralisAuthentication = MoralisAuthentication.Ethereum,
            supportedWallets: Array<String> = emptyArray(),
            chainId: Long? = null,
            moralisAuthCallback: (moralisUser: MoralisUser?) -> Unit,
        ) {
            when (authenticationType) {
                MoralisAuthentication.Polkadot -> MoralisPolkadot.authenticate()
                MoralisAuthentication.Elrond -> MoralisElrond.authenticate()
                MoralisAuthentication.Ethereum -> authenticate(
                    signingMessage,
                    supportedWallets,
                    chainId,
                    moralisAuthCallback
                )
            }
        }

        private fun authenticate(
            signingMessage: String,
            supportedWallets: Array<String>,
            chainId: Long? = null,
            moralisAuthCallback: (moralisUser: MoralisUser?) -> Unit
        ) {
            // Starts a new connection to the bridge server and waits for a wallet to connect.
            MoralisApplication.resetSession(chainId)

            mCallback = object : Session.Callback {
                override fun onStatus(status: Session.Status) {
                    when (status) {
                        Session.Status.Approved -> {
                            Log.d(TAG, "onStatus Session Approved")
                            handleSessionApproved(
                                moralisAuthCallback,
                                signingMessage
                            )
                        }
                        Session.Status.Closed -> {
                            Log.d(TAG, "onStatus Session Closed")
                            handleSessionClosed()
                        }
                        Session.Status.Connected -> {
                            Log.d(TAG, "onStatus Session Connected")
                            requestConnectionToWallet(supportedWallets)
                        }
                        Session.Status.Disconnected -> {
                            Log.d(TAG, "onStatus Session Disconnected")
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
        fun linkWallet(
            accountToLink: String,
            signingMessage: String,
            moralisCallback: (moralisUser: MoralisUser?) -> Unit
        ) {

            val ethAddress = accountToLink.lowercase()
            // Search if the address already exists on the server.
            val builder = ParseQuery.State.Builder<MoralisObject>("_EthAddress")
            val query = MoralisQuery(builder)
            // get current user
            val user = MoralisUser.getCurrentUser()
            var ethAddressRecord: ParseObject?
            try {
                ethAddressRecord = query.get(ethAddress)
            } catch (e: ParseException) {
                if (e.code == ParseException.OBJECT_NOT_FOUND) {
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
                            moralisCallback
                        )
                    }
                    this.mTxRequest = id
                } else if (e.code == INVALID_SESSION_TOKEN) {
                    handleInvalidSession()
                }
                return
            }

            saveUser(user, ethAddress, moralisCallback)
        }

        private fun handleInvalidSession() {
            logOut()
        }

        // TODO: test and search for alternative background calls for cleaner code.
        fun unlinkWallet(
            account: String,
            moralisCallback: (moralisUser: MoralisUser?) -> Unit
        ) {
            val ethAddress = account.lowercase();
            val builder = ParseQuery.State.Builder<MoralisObject>("_EthAddress")
            val query = MoralisQuery(builder)
            var ethAddressRecord: ParseObject? = null
            try {
                ethAddressRecord = query.get(ethAddress)
                ethAddressRecord.deleteInBackground() {
                    val user = MoralisUser.getCurrentUser()
                    val accounts: ArrayList<String> = user.get("accounts") as ArrayList<String>
                    val nextAccounts = accounts.filter { it != ethAddress }
                    user?.put("accounts", nextAccounts)
                    if (nextAccounts.isEmpty()) {
                        user?.remove("ethAddress")
                    } else {
                        user?.put("ethAddress", nextAccounts.first().toString())
                    }
                    val parseUserTask = user.unlinkFromInBackground("moralisEth")
                    parseUserTask.continueWith {
                        user.saveInBackground {
                            // TODO: handle exceptions
                            Log.d(TAG, "Wallet unlinked.")
                            moralisCallback.invoke(user)
                        }
                    }
                }
            } catch (e: ParseException) {
                // If the address was not found on the server then the address is only saved locally
                // and we must update the user object.
                if (e.code == ParseException.OBJECT_NOT_FOUND) {
                    val user = MoralisUser.getCurrentUser()
                    val accounts: ArrayList<String> = user.get("accounts") as ArrayList<String>
                    val nextAccounts = accounts.filter { it != ethAddress }
                    user?.put("accounts", nextAccounts)
                    if (nextAccounts.isEmpty()) {
                        user?.remove("ethAddress")
                    } else {
                        user?.put("ethAddress", nextAccounts.first().toString())
                    }
                    user.saveInBackground {
                        // TODO: handle exceptions
                        Log.d(TAG, "Wallet unlinked.")
                        moralisCallback.invoke(user)
                    }
                } else if (e.code == INVALID_SESSION_TOKEN) {
                    handleInvalidSession()
                }
            }
        }

        private fun handleSignLinkResponse(
            response: Session.MethodCall.Response,
            ethAddress: String,
            signingMessage: String,
            user: MoralisUser,
            moralisAuthCallback: (moralisUser: MoralisUser?) -> Unit
        ) {
            mUiScope.launch {
                val signature = ((response.result as? String) ?: "Unknown response")

                val authData = mapOf(
                    "id" to ethAddress,
                    "signature" to signature,
                    "data" to signingMessage
                )

                val parseUserTask = user.linkWithInBackground("moralisEth", authData)
                parseUserTask.continueWith {
                    if (it.error != null && it.error is ParseException) {
                        if ((it.error as ParseException).code == INVALID_SESSION_TOKEN) {
                            handleInvalidSession()
                            return@continueWith
                        }
                    }
                    saveUser(user, ethAddress, moralisAuthCallback)
                }
            }
        }

        private fun saveUser(
            user: MoralisUser,
            ethAddress: String,
            moralisAuthCallback: (moralisUser: MoralisUser?) -> Unit
        ) {
            // TODO: handle exceptions
            user.addAllUnique("accounts", mutableListOf(ethAddress))
            user.put("ethAddress", ethAddress);
            user.saveInBackground {
                if (it?.code == INVALID_SESSION_TOKEN) {
                    handleInvalidSession()
                    return@saveInBackground
                }
                // TODO: handle other exceptions
                Log.d(TAG, "user saved.")
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
        private fun requestConnectionToWallet(supportedWallets: Array<String>) {
            Log.d(TAG, "requestConnectionToWallet")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(MoralisApplication.config.toWCUri())
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            try {
                MoralisApplication.getContext().startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Define what your app should do if no activity can handle the intent.
                // TODO
            }
        }

        /**
         * Starts the signing up process after a wallet approved a connection to it.
         */
        private fun handleSessionApproved(
            moralisAuthCallback: (result: MoralisUser?) -> Unit,
            signingMessage: String
        ) {
            Log.d(TAG, "handleSessionApproved")
            mUiScope.launch {
                signUpToMoralis(moralisAuthCallback, signingMessage)
                Log.d(TAG, "Connected:  ${MoralisApplication.session.approvedAccounts()}")
            }
        }

        private fun handleSessionClosed() {
            MoralisApplication.session.removeCallback(mCallback)
            mUiScope.launch {
                mActivityAuthenticationCallback.onDisconnect()
            }
        }

        private fun signUpToMoralis(
            moralisAuthCallback: (result: MoralisUser?) -> Unit,
            signingMessage: String
        ) {
            val accounts = MoralisApplication.session.approvedAccounts() ?: return
            val accountsLowercase = accounts.map { it.lowercase() }
            val ethAddress = accountsLowercase.first()

            // If there is already a moralis user present like in the case of "SignUp with Email"
            // we link the wallet to the current user instead of signing up a new user.
            MoralisUser.getCurrentUser()?.let {
                linkWallet(ethAddress, signingMessage, moralisAuthCallback)
                return
            }

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
            moralisAuthCallback: (result: MoralisUser?) -> Unit
        ) {
            Log.d(TAG, "handleResponse, response: ${response.id} mTxRequest=$mTxRequest")
            if (response.id == mTxRequest) {
                Log.d(TAG, "response.id == mTxRequest")
                mTxRequest = null
                mUiScope.launch {
                    val signature = ((response.result as? String) ?: "Unknown response")

                    val authData = mapOf(
                        "id" to ethAddress,
                        "signature" to signature,
                        "data" to signingMessage
                    )

                    val parseUserTask = MoralisUser.logInWithInBackground("moralisEth", authData)
                    parseUserTask.continueWith(object : Continuation<MoralisUser?, Void?> {
                        override fun then(task: Task<MoralisUser?>): Void? {
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
                            if (task.error != null && task.error is ParseException) {
                                if ((task.error as ParseException).code == INVALID_SESSION_TOKEN) {
                                    handleInvalidSession()
                                    return null
                                }
                            }
                            val moralisUser: MoralisUser? = task.result
                            moralisUser?.acl = ParseACL(moralisUser);
                            // TODO: if (!user) throw new Error('Could not get user');
                            moralisUser?.addAllUnique("accounts", accountsLowercase)
                            moralisUser?.put("ethAddress", ethAddress);
                            // For new users, the username will be a randomly generated alphanumeric string and the email
                            // property will not exist. This can be set or changed by the app.

                            Log.d(TAG, "call saveInBackground")
                            moralisUser?.saveInBackground {
                                // TODO: handle exceptions
                                if (it?.code == INVALID_SESSION_TOKEN) {
                                    handleInvalidSession()
                                    return@saveInBackground
                                }
                                Log.d(TAG, "user logged in.")
                                moralisAuthCallback.invoke(moralisUser)
                            }
                            return null
                        }
                    })
                }
            }
        }

        fun onStart(authenticationCallback: MoralisAuthenticationCallback) {
            mActivityAuthenticationCallback = authenticationCallback
            initialSetup()
        }

        private fun initialSetup() {
            // if Application.session is not initialized then return
            val session = nullOnThrow { MoralisApplication.session } ?: return
            session.addCallback(mCallback)
            if (session.approvedAccounts() != null) {
                mActivityAuthenticationCallback.onConnect(session.approvedAccounts())
            }
        }

        fun logOut() {
            // Usually the user gets cached, this we could have a user logged-in and this method being
            // called without session. We simply ignore the session and log out the user.
            nullOnThrow { MoralisApplication.session }?.kill()
            MoralisUser.logOut()
        }

        /**
         * The message that the signing Wallet will show to the user while connecting to it.
         *
         * @see <a href="https://docs.moralis.io/moralis-server/users/crypto-login#changing-sign-in-message">https://docs.moralis.io/moralis-server/users/crypto-login#changing-sign-in-message</a>
         */
        fun getSigningData(): String {
//            return "Authentication Interface"
            return MoralisApplication.getContext().getString(R.string.signing_message)
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

    interface MoralisAuthenticationCallback {
        fun onConnect(accounts: List<String>?)
        fun onDisconnect()
        fun onAccountsChanged(newAccountAddress: String) {
            /* Default implementation. */
        }

        fun onChainIdChanged(newChainId: Long) {
            /* Default implementation. */
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