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

        fun transfer(transferObject: TransferObject, context: Context) {

            val user = User.getCurrentUser()
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
                        nonce = null,
                        gasPrice = null,
                        gasLimit = null,
                        value = obj.mAmount,
                        data = ""
                    )
                    // TODO: maybe use Sign Typed Data v4 instead?
                    MoralisApplication.session.performMethodCall(
                        transaction,
                        ::handleTransferResponse
                    )
                    mTxRequest = id
                    navigateToWallet(context)
                }

                TransferType.ERC20 -> {
                    // TODO: wait for walletconnect 2.0
                }
                TransferType.ERC721 -> {
                    // TODO: wait for walletconnect 2.0
                }
                TransferType.ERC1155 -> {
                    // TODO: wait for walletconnect 2.0
                }
            }

//            if (awaitReceipt) return transferOperation;
//
//            transferOperation
//                .on('transactionHash', hash => {
//                    transferEvents.emit('transactionHash', hash);
//                })
//            .on('receipt', receipt => {
//                transferEvents.emit('receipt', receipt);
//            })
//            .on('confirmation', (confirmationNumber, receipt) => {
//                transferEvents.emit('confirmation', (confirmationNumber, receipt));
//            })
//            .on('error', error => {
//                transferEvents.emit('error', error);
//                throw error;
//            });
//
//            return transferEvents;
        }

        private fun handleTransferResponse(
            response: Session.MethodCall.Response,
        ) {
            if (response.id == mTxRequest) {
                mTxRequest = null
                Log.d(TAG, "TRANSFER done")
                if (response.error != null) {
                    Log.d(TAG, "Transaction error: ${response.error}")
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
        val mAwaitReceipt: Boolean = true,
        val mTransactionReceiver: String
    ) {
        class TransferObjectNATIVE(
            system: String = "evm",
            awaitReceipt: Boolean = true,
            amountToTransfer: String,
            receiver: String
        ) : TransferObject(TransferType.NATIVE, system, awaitReceipt, receiver) {
            val mAmount: String = UnitConverter.convertETHToGwei(amountToTransfer)
        }

        class TransferObjectERC20(
            system: String = "evm",
            awaitReceipt: Boolean = true,
            val contractAddress: String,
            receiver: String,
        ) : TransferObject(TransferType.ERC20, system, awaitReceipt, receiver)

        class TransferObjectERC721(
            system: String = "evm",
            awaitReceipt: Boolean = true,
            val contractAddress: String,
            receiver: String,
        ) : TransferObject(TransferType.ERC721, system, awaitReceipt, receiver)

        class TransferObjectERC1155(
            system: String = "evm",
            awaitReceipt: Boolean = true,
            val contractAddress: String,
            receiver: String,
            val tokenId: String,
        ) : TransferObject(TransferType.ERC1155, system, awaitReceipt, receiver)
    }


}