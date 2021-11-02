package com.moralis.web3.api

import com.moralis.web3.api.data.MoralisWeb3APIResult
import com.moralis.web3.restapisdk.api.DefiApi
import com.moralis.web3.restapisdk.infrastructure.ApiClient
import com.moralis.web3.restapisdk.model.ChainList
import com.moralis.web3.restapisdk.model.ReservesCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MoralisWeb3ApiDefi {

    private fun getRetrofitService(): DefiApi {
        val apiClient = ApiClient()
        return apiClient.createService(DefiApi::class.java)
    }

    suspend fun getPairAddress(
        token0Address: String,
        token1Address: String,
        exchange: String,
        chain: ChainList? = null,
        toBlock: String? = null,
        toDate: String? = null
    ): MoralisWeb3APIResult<ReservesCollection> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.getPairAddress(
                token0Address,
                token1Address,
                exchange,
                chain,
                toBlock,
                toDate
            )
            // TODO: simplify
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    MoralisWeb3APIResult.Success(body)
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }

    suspend fun getPairReserves(
        pairAddress: String,
        chain: ChainList? = null,
        toBlock: String? = null,
        toDate: String? = null,
        providerUrl: String? = null
    ): MoralisWeb3APIResult<ReservesCollection> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.getPairReserves(
                pairAddress,
                chain,
                toBlock,
                toDate,
                providerUrl
            )
            // TODO: simplify
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    MoralisWeb3APIResult.Success(body)
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }
}