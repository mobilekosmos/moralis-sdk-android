package com.moralis.web3.api

import com.moralis.web3.api.data.MoralisWeb3APIResult
import com.moralis.web3.restapisdk.api.StorageApi
import com.moralis.web3.restapisdk.infrastructure.ApiClient
import com.moralis.web3.restapisdk.model.IpfsFile
import com.moralis.web3.restapisdk.model.IpfsFileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MoralisWeb3ApiStorage {

    private fun getRetrofitService(): StorageApi {
        val apiClient = ApiClient()
        return apiClient.createService(StorageApi::class.java)
    }

    suspend fun uploadFolder(ipfsFileRequest: List<IpfsFileRequest>? = null): MoralisWeb3APIResult<List<IpfsFile>> {
        return withContext(Dispatchers.IO) {
            val service = getRetrofitService()
            val response = service.uploadFolder(
                ipfsFileRequest
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