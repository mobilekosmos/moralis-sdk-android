package com.moralis.helloworld.login.data

import android.util.Log
import com.moralis.web3.Moralis
import com.moralis.web3.MoralisUser
import com.parse.ParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    companion object {
        private const val TAG = "LoginDataSource"
    }

    suspend fun signUp(moralisUser: MoralisUser): Result<MoralisUser> {
        return withContext(Dispatchers.IO) {
            try {
                moralisUser.signUp()
                Result.Success(moralisUser)
            } catch (e: ParseException) {
                Log.e(TAG, "failed to login: " + e.message)
                Result.Error(e)
                // SignIn failed. Look at the ParseException to see what happened.
            }
        }
    }

    suspend fun login(username: String, password: String): Result<MoralisUser> {
        return withContext(Dispatchers.IO) {
            try {
                Result.Success(MoralisUser.logIn(username, password))
            } catch (e: ParseException) {
                Log.e(TAG, "Failed to login: " + e.message)
                Result.Error(e)
                // SignIn failed. Look at the ParseException to see what happened.
            }
        }
    }

    fun logout() {
        Moralis.logOut()
    }
}