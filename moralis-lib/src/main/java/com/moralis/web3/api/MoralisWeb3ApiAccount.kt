package com.moralis.web3.api

import com.moralis.web3.api.data.MoralisWeb3APIResult
import com.moralis.web3.restapisdk.api.AccountApi
import com.moralis.web3.restapisdk.infrastructure.ApiClient
import com.moralis.web3.restapisdk.model.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.math.BigDecimal

open class MoralisWeb3ApiAccount {

    companion object {
        private fun getRetrofitService(): AccountApi {
            val apiClient = ApiClient()
            return apiClient.createService(AccountApi::class.java)
        }

        suspend fun getTransactions(
            address: String,
            chain: ChainList? = null,
            subdomain: String? = null,
            fromBlock: Int? = null,
            toBlock: Int? = null,
            fromDate: String? = null,
            toDate: String? = null,
            offset: Int? = null,
            limit: Int? = null
        ): MoralisWeb3APIResult<List<Transaction>> {
            return withContext(IO) {
                val service = getRetrofitService()
                val response = service.getTransactions(
                    address,
                    chain,
                    subdomain,
                    fromBlock,
                    toBlock,
                    fromDate,
                    toDate,
                    offset,
                    limit
                )
                // TODO: simplify
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = body.result
                        if (result != null) {
                            MoralisWeb3APIResult.Success(result)
                        } else {
                            MoralisWeb3APIResult.Error(response.code())
                        }
                    }
                    MoralisWeb3APIResult.Error(response.code())
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            }
        }

        suspend fun getNFTTransfers(
            address: String,
            chain: ChainList? = null,
            format: String? = null,
            direction: String? = null,
            offset: Int? = null,
            limit: Int? = null,
            order: String? = null
        ): MoralisWeb3APIResult<List<NftTransfer>> {
            return withContext(IO) {
                val service = getRetrofitService()
                val response =
                    service.getNFTTransfers(address, chain, format, direction, offset, limit, order)
                // TODO: simplify
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = body.result
                        MoralisWeb3APIResult.Success(result)
                    }
                    MoralisWeb3APIResult.Error(response.code())
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            }
        }

        suspend fun getNFTs(
            address: String,
            chain: ChainList? = null,
            format: String? = null,
            offset: Int? = null,
            limit: Int? = null,
            order: String? = null
        ): MoralisWeb3APIResult<List<NftOwner>> {
            return withContext(IO) {
                val service = getRetrofitService()
                val response = service.getNFTs(address, chain, format, offset, limit, order)
                // TODO: simplify
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = body.result
                        if (result != null) {
                            MoralisWeb3APIResult.Success(result)
                        } else {
                            MoralisWeb3APIResult.Error(response.code())
                        }
                    }
                    MoralisWeb3APIResult.Error(response.code())
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            }
        }

        suspend fun getNFTsForContract(
            address: String,
            tokenAddress: String,
            chain: ChainList? = null,
            format: String? = null,
            offset: Int? = null,
            limit: Int? = null,
            order: String? = null
        ): MoralisWeb3APIResult<List<NftOwner>> {
            return withContext(IO) {
                val service = getRetrofitService()
                val response = service.getNFTsForContract(
                    address,
                    tokenAddress,
                    chain,
                    format,
                    offset,
                    limit,
                    order
                )
                // TODO: simplify
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = body.result
                        if (result != null) {
                            MoralisWeb3APIResult.Success(result)
                        } else {
                            MoralisWeb3APIResult.Error(response.code())
                        }
                    }
                    MoralisWeb3APIResult.Error(response.code())
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            }
        }

        suspend fun getNativeBalance(
            address: String,
            chain: ChainList? = null,
            providerUrl: String? = null,
            toBlock: BigDecimal? = null
        ): MoralisWeb3APIResult<NativeBalance> {
            return withContext(IO) {
                val service = getRetrofitService()
                val response = service.getNativeBalance(address, chain, providerUrl, toBlock)
                // TODO: simplify
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = body.balance
                        MoralisWeb3APIResult.Success(result)
                    }
                    MoralisWeb3APIResult.Error(response.code())
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            }
        }

        suspend fun getTokenBalances(
            address: String,
            chain: ChainList? = null,
            subdomain: String? = null,
            toBlock: BigDecimal? = null
        ): MoralisWeb3APIResult<List<Erc20TokenBalance>> {
            return withContext(IO) {
                val service = getRetrofitService()
                val response = service.getTokenBalances(address, chain, subdomain, toBlock)
                // TODO: simplify
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        MoralisWeb3APIResult.Success(body)
                    }
                    MoralisWeb3APIResult.Error(response.code())
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            }
        }

        suspend fun getTokenTransfers(
            address: String,
            chain: ChainList? = null,
            subdomain: String? = null,
            fromBlock: Int? = null,
            toBlock: Int? = null,
            fromDate: String? = null,
            toDate: String? = null,
            offset: Int? = null,
            limit: Int? = null
        ): MoralisWeb3APIResult<List<Erc20Transaction>> {
            return withContext(IO) {
                val service = getRetrofitService()
                val response = service.getTokenTransfers(
                    address,
                    chain,
                    subdomain,
                    fromBlock,
                    toBlock,
                    fromDate,
                    toDate,
                    offset,
                    limit
                )
                // TODO: simplify
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        MoralisWeb3APIResult.Success(body)
                    }
                    MoralisWeb3APIResult.Error(response.code())
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            }
        }
    }
}