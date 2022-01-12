package com.tm78775.retroforce.service

import com.tm78775.retroforce.model.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

internal interface SessionService {

    @FormUrlEncoded
    @POST("/services/oauth2/token")
    suspend fun refreshSession(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String,
        @Field("client_secret") secret: String,
        @Field("refresh_token") refreshToken: String
    ): Response<RefreshTokenResponse>

    @FormUrlEncoded
    @POST("/services/oauth2/revoke")
    suspend fun logout(
        @Field("token") accessToken: String
    ): Response<Void>



}