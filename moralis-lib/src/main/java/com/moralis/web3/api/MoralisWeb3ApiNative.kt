package com.moralis.web3.api

import com.moralis.web3.api.data.MoralisWeb3APIResult
import com.moralis.web3.restapisdk.api.NativeApi
import com.moralis.web3.restapisdk.infrastructure.ApiClient
import com.moralis.web3.restapisdk.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MoralisWeb3ApiNative {

    private fun getRetrofitService(): NativeApi {
        val apiClient = ApiClient()
        return apiClient.createService(NativeApi::class.java)
    }

    suspend fun getBlock(
        blockNumberOrHash: String,
        chain: ChainList? = null,
        subdomain: String? = null
    ): MoralisWeb3APIResult<Block> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.getBlock(
                blockNumberOrHash,
                chain,
                subdomain,
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

    suspend fun getContractEvents(
        address: String,
        topic: String,
        chain: ChainList? = null,
        subdomain: String? = null,
        providerUrl: String? = null,
        fromBlock: Int? = null,
        toBlock: Int? = null,
        fromDate: String? = null,
        toDate: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        body: Any? = null
    ): MoralisWeb3APIResult<List<LogEvent>> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.getContractEvents(
                address,
                topic,
                chain,
                subdomain,
                providerUrl,
                fromBlock,
                toBlock,
                fromDate,
                toDate,
                offset,
                limit,
                body
            )
            // TODO: simplify
            if (response.isSuccessful) {
                val bodyResponse = response.body()
                if (bodyResponse != null) {
                    MoralisWeb3APIResult.Success(bodyResponse)
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }

    suspend fun getDateToBlock(
        date: String,
        chain: ChainList? = null,
        providerUrl: String? = null
    ): MoralisWeb3APIResult<BlockDate> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.getDateToBlock(
                date,
                chain,
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

    suspend fun getLogsByAddress(
        address: String,
        chain: ChainList? = null,
        subdomain: String? = null,
        blockNumber: String? = null,
        fromBlock: String? = null,
        toBlock: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        topic0: String? = null,
        topic1: String? = null,
        topic2: String? = null,
        topic3: String? = null
    ): MoralisWeb3APIResult<LogEventByAddress> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.getLogsByAddress(
                address,
                chain,
                subdomain,
                blockNumber,
                fromBlock,
                toBlock,
                fromDate,
                toDate,
                topic0,
                topic1,
                topic2,
                topic3
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

    suspend fun getNFTTransfersByBlock(
        blockNumberOrHash: String,
        chain: ChainList? = null,
        subdomain: String? = null
    ): MoralisWeb3APIResult<List<NftTransfer>> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.getNFTTransfersByBlock(
                blockNumberOrHash, chain, subdomain
            )
            // TODO: simplify
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    MoralisWeb3APIResult.Success(body.result)
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }

    suspend fun getTransaction(
        transactionHash: String,
        chain: ChainList? = null,
        subdomain: String? = null
    ): MoralisWeb3APIResult<BlockTransaction> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.getTransaction(
                transactionHash, chain, subdomain
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

    suspend fun runContractFunction(
        address: String,
        functionName: String,
        body: Any,
        chain: ChainList? = null,
        subdomain: String? = null,
        providerUrl: String? = null
    ): MoralisWeb3APIResult<String> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.runContractFunction(
                address, functionName, body, chain, subdomain, providerUrl
            )
            // TODO: simplify
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    MoralisWeb3APIResult.Success(responseBody)
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }
}