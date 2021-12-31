package com.tm78775.retroforce.service

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

internal interface SessionRefreshService {

    @FormUrlEncoded
    @POST("/services/oauth2/token")
    suspend fun refreshSession(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String,
        @Field("client_secret") secret: String,
        @Field("refresh_token") refreshToken: String
    ): Response<RefreshTokenResponse>

}