package com.tm78775.retroforce

import com.google.gson.JsonArray
import com.tm78775.retroforce.model.ApiNameProvider
import com.tm78775.retroforce.model.Fetchable
import com.tm78775.retroforce.service.*
import com.tm78775.retroforce.service.RestService
import com.tm78775.retroforce.service.RetroFactory
import retrofit2.Response

object RetroForce {

    /**
     * Call this to get the id of the authenticated user.
     * @return The id of the authenticated user.
     * @throws [AuthTokenException] if the user is not authenticated.
     */
    @Throws
    fun getUid(): String {
        val token = RetroConfig.getToken()
        return token.uid()
    }

    /**
     * Call this method to perform a soql query fetching n objects.
     * @param soqlQuery The query to be executed.
     * @return A [JsonArray] of fetched objects.
     * @throws SessionExpiredException If the auth credentials are in need of refreshing.
     * @throws ServerUnsuccessfulException If Salesforce returned a code that was not success.
     */
    @Throws
    suspend fun query(soqlQuery: String): JsonArray? {
        val service = RetroFactory.createAuthenticatedService(
            authToken = RetroConfig.getToken().accessToken,
            endPoint = RetroConfig.getServer().endpoint,
            RestService::class.java
        )

        val response = service.query(soqlQuery)
        return validate(response)?.records
    }

    @Throws
    suspend fun create(obj: ApiNameProvider): CreateResponse? {
        val service = RetroFactory.createAuthenticatedService(
            authToken = RetroConfig.getToken().accessToken,
            endPoint = RetroConfig.getServer().endpoint,
            RestService::class.java
        )

        service.create(obj.getApiName(), obj).let {
            return validate(it)
        }
    }

    @Throws
    suspend fun update(obj: ApiNameProvider) {
        if(obj.hasNoSfid())
            throw MissingSfidException()

        val service = RetroFactory.createAuthenticatedService(
            authToken = RetroConfig.getToken().accessToken,
            endPoint = RetroConfig.getServer().endpoint,
            RestService::class.java
        )

        service.update(obj.getApiName(), obj.sfid, obj)
    }

    @Throws
    suspend fun delete(obj: ApiNameProvider) {
        if(obj.hasNoSfid())
            return

        val service = RetroFactory.createAuthenticatedService(
            authToken = RetroConfig.getToken().accessToken,
            endPoint = RetroConfig.getServer().endpoint,
            RestService::class.java
        )

        service.delete(obj.getApiName(), obj.sfid)
    }

    /**
     * Helper method to validate the response from the server.
     * @param response The response returned by the server.
     * @return The object embedded in the [Response] object.
     */
    @Throws
    private fun <T> validate(response: Response<T>): T? {
        if (response.code() == 401)
            throw SessionExpiredException("Access denied. Response code: ${response.code()}.")

        if (response.code() in 200..299) {
            return response.body()
        } else {
            throw ServerUnsuccessfulException(
                "Server response code is unsatisfactory: ${response.code()}. " +
                        "\nError body: ${response.errorBody()}"
            )
        }
    }
}