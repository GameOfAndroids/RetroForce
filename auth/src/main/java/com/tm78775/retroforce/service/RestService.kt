package com.tm78775.retroforce.service

import com.tm78775.retroforce.model.SFObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

@PublishedApi
internal interface RestService {

    @GET("/services/data/${ApiConstants.apiVersion}/query")
    suspend fun query(@Query("q") soql: String): Response<QueryResponse>

    suspend fun create(obj: SFObject)

    suspend fun update(obj: SFObject)

    suspend fun delete(obj: SFObject)

}