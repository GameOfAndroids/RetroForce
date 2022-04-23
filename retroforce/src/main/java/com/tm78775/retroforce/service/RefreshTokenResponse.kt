package com.tm78775.retroforce.service

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("id") val id : String,
    @SerializedName("issued_at") val issuedAt : String,
    @SerializedName("instance_url") val instanceUrl : String,
    @SerializedName("signature") val signature : String,
    @SerializedName("access_token") val accessToken : String,
    @SerializedName("token_type") val tokenType : String,
    @SerializedName("scope") val scope : String
)
