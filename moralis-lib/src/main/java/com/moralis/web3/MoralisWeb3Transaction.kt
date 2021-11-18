package com.moralis.web3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import org.walletconnect.Session

class MoralisWeb3Transaction {
    companion object {
        private const val TAG = "Transaction"
        private var mTxRequest: Long? = null

        fun transfer(
            transferObject: TransferObject,
            context: Context,
            moralisAuthCallback: MoralisTransferCallback
        ) {

            val user = MoralisUser.getCurrentUser()
            val accounts = (user.get("accounts") as ArrayList<*>)
            val sender = accounts[0]?.toString() ?: return

//            val transferOperation
//            val customToken
//
//            if (transferObject.mType != TransferType.NATIVE) {
//                customToken = new web3 . eth . Contract (TransferUtils.abi[type], contractAddress)
//            }

            val id = System.currentTimeMillis()

            when (transferObject.mType) {
                TransferType.NATIVE -> {
                    val obj = transferObject as TransferObject.TransferObjectNATIVE
                    val transaction = Session.MethodCall.SendTransaction(
                        id = id,
                        from = sender,
                        to = obj.mTransactionReceiver,
                        nonce = null, // Optional
                        gasPrice = null, // Optional
                        gasLimit = null, // Optional
                        value = obj.mAmount,
                        data = "" // Required
                    )
                    // TODO: maybe use Sign Typed Data v4 instead?
                    MoralisApplication.session.performMethodCall(
                        transaction
                    ) {
                        handleTransferResponse(it, moralisAuthCallback)
                    }
                    mTxRequest = id
                    navigateToWallet(context)
                }

                TransferType.ERC20 -> {
                    val obj = transferObject as TransferObject.TransferObjectERC20

                    // Sends the "data" to the contractAddress, so we send the destination and amount
                    // to the token contract.
                    val transaction = Session.MethodCall.SendTransaction(
                        id = id,
                        from = sender,
                        to = obj.contractAddress,
                        nonce = null, // Optional
                        gasPrice = null, // Optional
                        gasLimit = null, // Optional
                        value = "0",
                        data = Web3TransactionUtils.encodeTransferData(
                            obj.mTransactionReceiver,
                            obj.mAmount.toBigInteger()
                        )
                    )
                    // TODO: maybe use Sign Typed Data v4 instead?
                    MoralisApplication.session.performMethodCall(
                        transaction
                    ) {
                        handleTransferResponse(it, moralisAuthCallback)
                    }
                    mTxRequest = id
                    navigateToWallet(context)
                }
                TransferType.ERC721 -> {
                    val obj = transferObject as TransferObject.TransferObjectERC721
                    val transaction = Session.MethodCall.SendTransaction(
                        id = id,
                        from = sender,
                        to = obj.contractAddress,
                        nonce = null, // Optional
                        gasPrice = null, // Optional
                        gasLimit = null, // Optional
                        value = "1",
                        data = Web3TransactionUtils.encodeTransferERC721Data(
                            sender,
                            obj.mTransactionReceiver,
                            obj.mTokenId
                        )
                    )
                    // TODO: maybe use Sign Typed Data v4 instead?
                    MoralisApplication.session.performMethodCall(
                        transaction
                    ) {
                        handleTransferResponse(it, moralisAuthCallback)
                    }
                    mTxRequest = id
                    navigateToWallet(context)
                }
                TransferType.ERC1155 -> {
                    val obj = transferObject as TransferObject.TransferObjectERC1155
                    val transaction = Session.MethodCall.SendTransaction(
                        id = id,
                        from = sender,
                        to = obj.contractAddress,
                        nonce = null, // Optional
                        gasPrice = null, // Optional
                        gasLimit = null, // Optional
                        value = "1",
                        data = Web3TransactionUtils.encodeTransferERC1155Data(
                            sender,
                            obj.mTransactionReceiver,
                            obj.mTokenId,
                            obj.mAmount.toBigInteger()
                        )
                    )
                    // TODO: maybe use Sign Typed Data v4 instead?
                    MoralisApplication.session.performMethodCall(
                        transaction
                    ) {
                        handleTransferResponse(it, moralisAuthCallback)
                    }
                    mTxRequest = id
                    navigateToWallet(context)
                }
            }
        }

        private fun handleTransferResponse(
            response: Session.MethodCall.Response,
            moralisTransferCallback: MoralisTransferCallback,
        ) {
            if (response.id == mTxRequest) {
                mTxRequest = null
                Log.d(TAG, "Transfer done")
                if (response.error != null) {
                    // metamask when running moralis web boilerplate: Error: execution reverted: Uni::_transferTokens: transfer amount exceeds balance
                    // metamask on Android with Android SDK and USDT: message=invalid opcode: opcode 0xfe not defined)
                    // metamask on Android with Android SDK: DAI: error: Error(code=-32000, message=execution reverted: Dai/insufficient-balance)
                    Log.e(TAG, "Transaction error: ${response.error}")
                    // TODO: analyze if ".error!!" is right.
                    moralisTransferCallback.onError(response.error!!.message)
                } else {
                    moralisTransferCallback.onResponse(response.result)
//                    moralisAuthCallback.onConfirmation()
//                    moralisAuthCallback.onReceipt()
//                    moralisAuthCallback.onTransactionHash(null)
                }
            }
        }

        private fun navigateToWallet(context: Context) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("wc:")
            context.startActivity(i)
        }
    }

    enum class TransferType {
        NATIVE, ERC20, ERC721, ERC1155
    }

    sealed class TransferObject(
        val mType: TransferType,
        val mSystem: String = "evm",
        val mTransactionReceiver: String
    ) {
        class TransferObjectNATIVE(
            system: String = "evm",
            amountToTransfer: String,
            receiver: String
        ) : TransferObject(TransferType.NATIVE, system, receiver) {
            // does not work on TrustWallet if not converting to BigInteger.
            val mAmount: String = MoralisUnitConverter.convertETHToWei(amountToTransfer)
//            val mAmount : String = MoralisUnitConverter.convertETHToWeiHex(amountToTransfer) // works on TrustWallet
//            val mAmount : String = "0x5AF3107A4000" // works on TrustWallet
            //                        val mAmount = "5500000000000000000" // works on TrustWallet
//            val mAmount = "0x4C53ECDC18A60000" // works but freezes in metamask, works on TrustWallet
        }

        class TransferObjectERC20(
            system: String = "evm",
            amountToTransfer: String,
            val contractAddress: String,
            receiver: String,
        ) : TransferObject(TransferType.ERC20, system, receiver) {
            val mAmount: String = MoralisUnitConverter.convertETHToWei(amountToTransfer)
        }

        class TransferObjectERC721(
            system: String = "evm",
            val contractAddress: String,
            receiver: String,
            tokenId: Long
        ) : TransferObject(TransferType.ERC721, system, receiver) {
            val mTokenId: Long = tokenId
        }

        class TransferObjectERC1155(
            system: String = "evm",
            val contractAddress: String,
            amountToTransfer: String,
            receiver: String,
            tokenId: Long,
        ) : TransferObject(TransferType.ERC1155, system, receiver) {
            val mTokenId: Long = tokenId
            val mAmount = amountToTransfer
        }
    }

    interface MoralisTransferCallback {
        //        fun onTransactionHash(accounts: List<String>?)
//        fun onReceipt()
//        fun onConfirmation()
        fun onError(message: String)
        fun onResponse(result: Any?)
    }

}