package com.moralis.web3.api.data

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class MoralisWeb3APIResult<out T : Any> {

    data class Success<out T : Any>(val data: T) : MoralisWeb3APIResult<T>()

    // TODO: check type "Nothing"
    data class Error(val errorCode: Int) : MoralisWeb3APIResult<Nothing>()
}