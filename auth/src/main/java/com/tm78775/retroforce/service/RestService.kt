package com.tm78775.retroforce.service

import com.tm78775.retroforce.model.SFObject
import retrofit2.Response
import retrofit2.http.*

@PublishedApi
internal interface RestService {

    @GET("/services/data/${ApiConstants.apiVersion}/query")
    suspend fun query(
        @Query("q") soql: String
    ): Response<QueryResponse>

    /**
     * This method will fetch describe object metadata for an sobject.
     * @param ifModifiedSince This will supply a header which will pull metadata
     * that has been modified after the date that is passed in. The format of the string
     * must be: EEE, dd MMM yyyy HH:mm:ss z
     * Example: Fri, 14 Jan 2021 09:18:00 EST
     * @param objectName The name of the object on salesforce.
     */
    @GET("/services/data/${ApiConstants.apiVersion}/sobjects/{n}/describe")
    suspend fun describe(
        @Header("If-Modified-Since") ifModifiedSince: String,
        @Path("n") objectName: String
    ): Response<DescribeResponse>

    @POST("/services/data/${ApiConstants.apiVersion}/sobjects/{n}/")
    suspend fun create(
        @Path("n") objectName: String,
        @Body obj: SFObject
    ): Response<CreateResponse>

    @PATCH("/services/data/${ApiConstants.apiVersion}/sobjects/{n}/{id}")
    suspend fun update(
        @Path("n") objectName: String,
        @Path("id") sfid: String,
        obj: SFObject
    ): Response<Void>

    @DELETE("/services/data/${ApiConstants.apiVersion}/sobjects/{n}/{id}")
    suspend fun delete(
        @Path("n") objectName: String,
        @Path("id") id: String
    ): Response<Void>

}