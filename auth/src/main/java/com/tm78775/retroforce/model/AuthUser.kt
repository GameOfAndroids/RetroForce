package com.tm78775.retroforce.model

import com.google.gson.annotations.SerializedName

data class AuthUser(
    @SerializedName("attributes") val attributes: Attributes,
    @SerializedName("FirstName") val firstName: String?,
    @SerializedName("LastName") val lastName: String,
    @SerializedName("Email") val email: String
)