package com.moralis.web3.api

import com.moralis.web3.api.data.MoralisWeb3APIResult
import com.moralis.web3.restapisdk.api.ResolveApi
import com.moralis.web3.restapisdk.infrastructure.ApiClient
import com.moralis.web3.restapisdk.model.Resolve
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MoralisWeb3ApiResolve {

    private fun getRetrofitService(): ResolveApi {
        val apiClient = ApiClient()
        return apiClient.createService(ResolveApi::class.java)
    }

    suspend fun resolveDomain(
        domain: String,
        currency: String? = null
    ): MoralisWeb3APIResult<Resolve> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.resolveDomain(
                domain, currency
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