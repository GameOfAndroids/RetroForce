package com.tm78775.retroforce

import com.google.gson.Gson
import com.tm78775.retroforce.model.SFObject
import com.tm78775.retroforce.service.*
import com.tm78775.retroforce.service.RestService
import com.tm78775.retroforce.service.RetroFactory
import retrofit2.Response

class RetroForce {

    companion object {

        /**
         * Call this method to perform a soql query that is expected to fetch a single object.
         * @param soqlQuery The query to be executed.
         * @param forClazz The object to which the results should be converted.
         * @return The object fetched from Salesforce.
         * @throws SessionExpiredException If the auth credentials are in need of refreshing.
         * @throws ServerUnsuccessfulException If Salesforce returned a code that was not success.
         */
        @Throws
        suspend fun <T> queryForOne(soqlQuery: String, forClazz: Class<T>): T? {
            return query(soqlQuery, forClazz).let {
                if(it.isEmpty()) null else it.first()
            }
        }

        /**
         * Call this method to perform a soql query fetching n objects.
         * @param soqlQuery The query to be executed.
         * @param forClazz The object to which the results should be converted.
         * @return The list of objects fetched from Salesforce.
         * @throws SessionExpiredException If the auth credentials are in need of refreshing.
         * @throws ServerUnsuccessfulException If Salesforce returned a code that was not success.
         */
        @Throws
        suspend fun <T> query(soqlQuery: String, forClazz: Class<T>): List<T> {
            val service = RetroFactory.createAuthenticatedService(
                authToken = RetroConfig.getToken().accessToken,
                endPoint = RetroConfig.getServer().endpoint,
                RestService::class.java
            )

            val response = service.query(soqlQuery)
            return validate(response)?.let { queryResponse ->
                if (queryResponse.size == 0)
                    return listOf()
                else {
                    try {
                        val gson = Gson()
                        queryResponse.records.map { gson.fromJson(it, forClazz) }
                    } catch (e: Exception) {
                        // TODO: CRASHLYTICS
                        throw e
                    }
                }
            } ?: listOf()
        }

        @Throws
        suspend fun create(obj: SFObject): CreateResponse? {
            val service = RetroFactory.createAuthenticatedService(
                authToken = RetroConfig.getToken().accessToken,
                endPoint = RetroConfig.getServer().endpoint,
                RestService::class.java
            )

            service.create(obj.apiName, obj).let {
                return validate(it)
            }
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
}