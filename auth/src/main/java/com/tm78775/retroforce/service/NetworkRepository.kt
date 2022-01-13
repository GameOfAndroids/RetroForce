package com.tm78775.retroforce.service

import android.util.Log
import com.google.gson.Gson
import com.tm78775.retroforce.model.AuthToken
import com.tm78775.retroforce.model.Server
import javax.inject.Inject

internal class NetworkRepository @Inject constructor() {

    @Throws
    suspend inline fun <reified T> queryForOne(
        soql: String,
        server: Server,
        token: AuthToken
    ): T? {
        val restService = RetroFactory.createAuthenticatedService(
            token.accessToken,
            server.endpoint,
            RestService::class.java
        )

        val resp = restService.query(soql)
        if(resp.code() == 401)
            throw SessionExpiredException("Access denied. Response code: ${resp.code()}.")

        if(resp.code() in 200..299) {
            return resp.body()?.let {
                var obj: T? = null
                if(it.size > 0) {
                    try {
                        obj = Gson().fromJson(it.records.first(), T::class.java)
                    } catch (ex: Exception) {
                        Log.e(
                            this::class.simpleName,
                            "Cannot convert json to object: ${ex.localizedMessage}",
                            ex
                        )
                    }
                }

                return obj
            }
        } else {
            throw ServerUnsuccessfulException("Server response code is unsatisfactory: ${resp.code()}. " +
                    "\nError body: ${resp.errorBody()}")
        }
    }

}