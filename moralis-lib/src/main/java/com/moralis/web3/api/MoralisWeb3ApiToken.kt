package com.moralis.web3.api

import com.moralis.web3.api.data.MoralisWeb3APIResult
import com.moralis.web3.restapisdk.api.TokenApi
import com.moralis.web3.restapisdk.infrastructure.ApiClient
import com.moralis.web3.restapisdk.model.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object MoralisWeb3ApiToken {

    private fun getRetrofitService(): TokenApi {
        val apiClient = ApiClient()
        return apiClient.createService(TokenApi::class.java)
    }

    suspend fun getAllTokenIds(
        address: String,
        chain: ChainList? = null,
        format: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): MoralisWeb3APIResult<NftCollection> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getAllTokenIds(
                address, chain, format, offset, limit, order
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

    suspend fun getContractNFTTransfers(
        address: String,
        chain: ChainList? = null,
        format: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): MoralisWeb3APIResult<List<NftTransfer>> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getContractNFTTransfers(
                address, chain, format, offset, limit, order
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

    suspend fun getNFTMetadata(
        address: String,
        chain: ChainList? = null
    ): MoralisWeb3APIResult<NftContractMetadata> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getNFTMetadata(
                address, chain
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

    suspend fun getNFTOwners(
        address: String,
        chain: ChainList? = null,
        format: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): MoralisWeb3APIResult<List<NftOwner>> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getNFTOwners(
                address, chain, format, offset, limit, order
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
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }

    suspend fun getTokenAddressTransfers(
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
            val response = service.getTokenAdressTransfers(
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
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }

    suspend fun getTokenAllowance(
        address: String,
        ownerAddress: String,
        spenderAddress: String,
        chain: ChainList? = null,
        providerUrl: String? = null
    ): MoralisWeb3APIResult<Erc20Allowance> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getTokenAllowance(
                address, ownerAddress, spenderAddress, chain, providerUrl
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

    suspend fun getTokenIdMetadata(
        address: String,
        tokenId: String,
        chain: ChainList? = null,
        format: String? = null
    ): MoralisWeb3APIResult<Nft> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getTokenIdMetadata(
                address, tokenId, chain, format
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

    suspend fun getTokenIdOwners(
        address: String,
        tokenId: String,
        chain: ChainList? = null,
        format: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): MoralisWeb3APIResult<List<NftOwner>> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getTokenIdOwners(
                address, tokenId, chain, format, offset, limit, order
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
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }

    suspend fun getTokenMetadata(
        addresses: List<String>,
        chain: ChainList? = null,
        subdomain: String? = null,
        providerUrl: String? = null
    ): MoralisWeb3APIResult<List<Erc20Metadata>> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getTokenMetadata(
                addresses, chain, subdomain, providerUrl
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

    suspend fun getTokenMetadataBySymbol(
        symbols: List<String>,
        chain: ChainList? = null,
        subdomain: String? = null
    ): MoralisWeb3APIResult<List<Erc20Metadata>> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getTokenMetadataBySymbol(
                symbols, chain, subdomain
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

    suspend fun getTokenPrice(
        address: String,
        chain: ChainList? = null,
        providerUrl: String? = null,
        exchange: String? = null,
        toBlock: Int? = null
    ): MoralisWeb3APIResult<Erc20Price> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getTokenPrice(
                address, chain, providerUrl, exchange, toBlock
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

    suspend fun getWalletTokenIdTransfers(
        address: String,
        tokenId: String,
        chain: ChainList? = null,
        format: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): MoralisWeb3APIResult<List<NftTransfer>> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.getWalletTokenIdTransfers(
                address, tokenId, chain, format, offset, limit, order
            )
            // TODO: simplify
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val result = body.result
                    MoralisWeb3APIResult.Success(result)
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }

    suspend fun searchNFTs(
        q: String,
        chain: ChainList? = null,
        format: String? = null,
        filter: String? = null,
        fromBlock: Int? = null,
        toBlock: Int? = null,
        fromDate: String? = null,
        toDate: String? = null,
        offset: Int? = null,
        limit: Int? = null
    ): MoralisWeb3APIResult<List<NftMetadata>> {
        return withContext(IO) {
            val service = getRetrofitService()
            val response = service.searchNFTs(
                q, chain, format, filter, fromBlock, toBlock, fromDate, toDate, offset, limit
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
                } else {
                    MoralisWeb3APIResult.Error(response.code())
                }
            } else {
                MoralisWeb3APIResult.Error(response.code())
            }
        }
    }
}